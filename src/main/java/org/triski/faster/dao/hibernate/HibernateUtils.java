package org.triski.faster.dao.hibernate;

import com.mysql.jdbc.Driver;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.cfgxml.spi.LoadedConfig;
import org.hibernate.boot.cfgxml.spi.MappingReference;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.mapping.Column;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.triski.faster.commons.FasterProperties;
import org.triski.faster.commons.annotation.MainMethod;
import org.triski.faster.commons.exception.FasterException;
import org.triski.faster.commons.utils.PackageUtils;
import org.triski.faster.dao.hibernate.utils.MySQL5DialectExt;
import org.triski.faster.dao.hibernate.utils.TableAnnotationUtils;

import javax.persistence.Entity;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author chenshutian
 * @date 2019/7/25
 * @export generateTable, doInTransaction
 */
@UtilityClass
public class HibernateUtils {
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtils.class);

    /** 正向工程: hibernate.hbm2ddl.auto = create */
    public void createTable(String propertiesClasspath, EnumSet<TargetType> targetTypes) {
        operateTable(propertiesClasspath, metadata -> {
            SchemaExport schemaExport = new SchemaExport().setOutputFile("import.sql");
            schemaExport.create(targetTypes, metadata);
        });
    }

    /** 正向工程: hibernate.hbm2ddl.auto = create */
    public void createTable(String propertiesClasspath) {
        operateTable(propertiesClasspath, metadata -> {
            SchemaExport schemaExport = new SchemaExport();
            schemaExport.create(EnumSet.of(TargetType.DATABASE), metadata);
        });
    }

    /** 正向工程: hibernate.hbm2ddl.auto = update */
    public void updateTable(String propertiesClasspath, EnumSet<TargetType> targetTypes) {
        operateTable(propertiesClasspath, metadata -> {
            SchemaUpdate schemaUpdate = new SchemaUpdate().setOutputFile("import.sql");
            schemaUpdate.execute(targetTypes, metadata);
        });
    }

    /** 正向工程: hibernate.hbm2ddl.auto = update */
    public void updateTable(String propertiesClasspath) {
        operateTable(propertiesClasspath, metadata -> {
            SchemaUpdate schemaUpdate = new SchemaUpdate();
            schemaUpdate.execute(EnumSet.of(TargetType.DATABASE), metadata);
        });
    }

    @MainMethod
    private void operateTable(String propertiesClasspath, Consumer<Metadata> consumer) {
        FasterProperties properties = FasterProperties.load(propertiesClasspath);
        LoadedConfig loadedConfig = LoadedConfig.baseline();
        // 基本配置
        Map<String, String> config = loadedConfig.getConfigurationValues();
        putIntoMap(config, AvailableSettings.DIALECT, MySQL5DialectExt.class.getCanonicalName(), properties.getProperty(FasterProperties.HBM2DDL_DATABASE_DIALECT));
        putIntoMap(config, AvailableSettings.DRIVER, Driver.class.getCanonicalName(), FasterProperties.DATABASE_DRIVER_CLASS_NAME);
        putIntoMap(config, AvailableSettings.URL, properties.getProperty(FasterProperties.DATABASE_URL));
        putIntoMap(config, AvailableSettings.USER, properties.getProperty(FasterProperties.DATABASE_USERNAME));
        putIntoMap(config, AvailableSettings.PASS, properties.getProperty(FasterProperties.DATABASE_PASSWORD));
        putIntoMap(config, AvailableSettings.HBM2DDL_CHARSET_NAME, properties.getProperty(FasterProperties.HBM2DDL_CHARSET));
        putIntoMap(config, AvailableSettings.SHOW_SQL, "true");
        putIntoMap(config, AvailableSettings.FORMAT_SQL, "true");
        if (logger.isDebugEnabled()) {
            logger.debug("base hibernate config is {}", config);
        }
        // 映射配置
        String packageToScan = properties.getProperty(FasterProperties.HBM2DDL_PACKAGE_TO_SCAN);
        List<Class> classes = PackageUtils.scan(packageToScan);
        List<Class> entityClasses = classes.stream()
                .filter(clazz -> clazz.isAnnotationPresent(Entity.class))
                .collect(Collectors.toList());
        if (entityClasses.size() == 0) {
            throw new FasterException("{} has not class with @Entity", classes);
        }
        List<MappingReference> mappingReferences = entityClasses.stream()
                .map(clazz -> new MappingReference(MappingReference.Type.CLASS, clazz.getCanonicalName()))
                .collect(Collectors.toList());
        if (logger.isDebugEnabled()) {
            logger.debug("find {} class: {}", mappingReferences.size(), classes);
        }
        try {
            Field mappingReferencesField = LoadedConfig.class.getDeclaredField("mappingReferences");
            mappingReferencesField.setAccessible(true);
            mappingReferencesField.set(loadedConfig, mappingReferences);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new FasterException("fail to add hibernate mapping reference");
        }
        // 自定义注解处理: @Comment, @DefaultValue
        Map<String, Object> annotationValueMap = TableAnnotationUtils.process(entityClasses);
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().configure(loadedConfig).build();
        Metadata metadata = new MetadataSources(serviceRegistry).buildMetadata();
        metadata.getDatabase().getNamespaces().forEach(namespace -> {
            namespace.getTables().forEach(table -> {
                String tableComment = (String) annotationValueMap.get(table.getName() + "#comment");
                if (StringUtils.isNotBlank(tableComment)) {
                    table.setComment(tableComment);
                }
                Iterator<Column> columnIterator = table.getColumnIterator();
                while (columnIterator.hasNext()) {
                    Column column = columnIterator.next();
                    String columnComment = (String) annotationValueMap.get(String.format("%s#%s#comment", table.getName(), column.getName()));
                    if (StringUtils.isNotBlank(columnComment)) {
                        column.setComment(columnComment);
                    }
                    String columnDefaultValue = (String) annotationValueMap.get(String.format("%s#%s#defaultValue", table.getName(), column.getName()));
                    if (StringUtils.isNotBlank(columnDefaultValue)) {
                        column.setDefaultValue(columnDefaultValue);
                    }
                }
            });
        });
        // 操作数据库
        consumer.accept(metadata);
        serviceRegistry.close();
    }

    /** 使用 session 操作数据库 */
    public static void doInTransaction(SessionFactory factory, Runner runner) {
        Session session = factory.getCurrentSession();
        session.beginTransaction();

        // 执行操作
        runner.run(session);

        session.getTransaction().commit();
        session.close();
    }

    private void putIntoMap(Map<String, String> map, String key, String value) {
        putIntoMap(map, key, value, null);
    }

    private void putIntoMap(Map<String, String> map, String key, String value, String defaultValue) {
        if (StringUtils.isNotBlank(value)) {
            map.put(key, value);
        } else if (defaultValue != null) {
            map.put(key, value);
        }
    }

    public interface Runner {
        void run(Session session);
    }

}
