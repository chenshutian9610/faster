package org.triski.faster.dao.hibernate;

import com.mysql.jdbc.Driver;
import lombok.experimental.UtilityClass;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.cfgxml.spi.LoadedConfig;
import org.hibernate.boot.cfgxml.spi.MappingReference;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.triski.faster.commons.FasterProperties;
import org.triski.faster.commons.exception.FasterException;
import org.triski.faster.commons.utils.PackageUtils;

import javax.persistence.Entity;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chenshutian
 * @date 2019/7/25
 * @export generateTable, doInTransaction
 */
@UtilityClass
public class HibernateUtils {
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtils.class);

    private FasterProperties properties;

    /** 正向工程 */
    public void generateTable(String propertiesClasspath) {
        LoadedConfig loadedConfig = config(propertiesClasspath);
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().configure(loadedConfig).build();
        Metadata metadata = new MetadataSources(serviceRegistry).buildMetadata();
        SchemaUpdate schemaUpdate = new SchemaUpdate();
        schemaUpdate.execute(EnumSet.of(TargetType.DATABASE), metadata);
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

    /** 初始化 hibernate 配置 */
    private LoadedConfig config(String propertiesClasspath) {
        properties = FasterProperties.load(propertiesClasspath);
        LoadedConfig loadedConfig = LoadedConfig.baseline();

        // 基本配置
        Map<String, String> config = loadedConfig.getConfigurationValues();
        config.put(AvailableSettings.DIALECT, MySQL5Dialect.class.getCanonicalName());
        config.put(AvailableSettings.DRIVER, Driver.class.getCanonicalName());
        config.put(AvailableSettings.URL, properties.getProperty(FasterProperties.DATABASE_URL));
        config.put(AvailableSettings.USER, properties.getProperty(FasterProperties.DATABASE_USERNAME));
        config.put(AvailableSettings.PASS, properties.getProperty(FasterProperties.DATABASE_PASSWORD));
        config.put(AvailableSettings.SHOW_SQL, "true");
        config.put(AvailableSettings.FORMAT_SQL, "true");
        if (logger.isDebugEnabled()) {
            logger.debug("base hibernate config is {}", config);
        }

        // 映射配置
        String packageToScan = properties.getProperty(FasterProperties.HIBERNATE_PACKAGE_TO_SCAN);
        List<Class> classes = PackageUtils.scan(packageToScan);
        List<MappingReference> mappingReferences = classes.stream()
                .filter(clazz -> clazz.isAnnotationPresent(Entity.class))
                .map(clazz -> new MappingReference(MappingReference.Type.CLASS, clazz.getCanonicalName()))
                .collect(Collectors.toList());
        if (mappingReferences.size() == 0) {
            throw new FasterException("{} has not class with @Entity", classes);
        }
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

        return loadedConfig;
    }

    public interface Runner {
        void run(Session session);
    }

}
