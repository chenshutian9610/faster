package org.triski.faster.dao.mybatis;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.triski.faster.commons.FasterProperties;
import org.triski.faster.commons.exception.FasterException;
import org.triski.faster.commons.utils.PackageUtils;
import org.triski.faster.dao.mybatis.utils.GeneratorConfigXmlUtils;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author chenshutian
 * @date 2019/7/18
 * @export generateModel
 */
@UtilityClass
public class MybatisUtils {
    private static final Logger logger = LoggerFactory.getLogger(MybatisUtils.class);

    /** 逆向工程: 使用原生的 generatorConfig.xml 配置 */
    public void generateModel(InputStream inputStream) {
        try {
            List<String> warnings = new ArrayList<>();
            Configuration configuration = new ConfigurationParser(warnings).parseConfiguration(inputStream);
            DefaultShellCallback callback = new DefaultShellCallback(true);
            MyBatisGenerator generator = new MyBatisGenerator(configuration, callback, warnings);
            generator.generate(null);
            inputStream.close();
        } catch (IOException | SQLException | InterruptedException | InvalidConfigurationException | XMLParserException e) {
            throw new FasterException(e);
        }
    }

    /** 逆向工程, 需要配置 generator.hibernate.packageToScan */
    public void generateModel(String propertiesClasspath) {
        generateModel(propertiesClasspath, null);
    }

    /** 逆向工程 */
    public void generateModel(String propertiesClasspath, Map<String, String> tables) {
        if (tables != null && tables.size() == 0) {
            return;
        }
        FasterProperties fasterProperties = config(propertiesClasspath);
        String packageToScan = fasterProperties.getProperty(fasterProperties.HBM2DDL_PACKAGE_TO_SCAN);
        try (InputStream in = GeneratorConfigXmlUtils.process(fasterProperties)) {
            List<String> warnings = new ArrayList<>();
            List<TableConfiguration> tableConfigurations = toTableConfigurations(tables, packageToScan);
            Configuration configuration = new ConfigurationParser(warnings).parseConfiguration(in);
            configuration.getContexts().forEach(context -> {
                tableConfigurations.forEach(tableConfiguration -> context.addTableConfiguration(tableConfiguration));
            });
            DefaultShellCallback callback = new DefaultShellCallback(true);
            MyBatisGenerator generator = new MyBatisGenerator(configuration, callback, warnings);
            generator.generate(null);
        } catch (IOException | SQLException | InterruptedException | InvalidConfigurationException | XMLParserException e) {
            throw new FasterException(e);
        }
    }

    /** 初始化配置 */
    private FasterProperties config(String propertiesClasspath) {
        FasterProperties fasterProperties = FasterProperties.load(propertiesClasspath);
        if (StringUtils.isBlank(fasterProperties.getProperty(FasterProperties.MYBATIS_GENERATOR_ROOT_PACKAGE))) {
            StackTraceElement[] stackTraceElements = new RuntimeException().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                if (Objects.equals(stackTraceElement.getClassName(), MybatisUtils.class.getCanonicalName()) == false) {
                    String className = stackTraceElement.getClassName();
                    String packageName = className.substring(0, className.lastIndexOf("."));
                    fasterProperties.setProperty(FasterProperties.MYBATIS_GENERATOR_ROOT_PACKAGE, packageName);
                    break;
                }
            }
        }
        return fasterProperties;
    }

    /** 将 Map 转化为 TableConfiguration 列表 */
    private List<TableConfiguration> toTableConfigurations(Map<String, String> tables, String packageToScan) {
        List<TableConfiguration> tableConfigurations = new ArrayList<>();
        Context context = new Context(ModelType.CONDITIONAL);
        if (tables != null && tables.size() != 0) {
            tables.forEach((tableName, className) -> {
                TableConfiguration tc = new TableConfiguration(context);
                tc.setDomainObjectName(className);
                tc.setTableName(tableName);
                tc.setMapperName(className + "Mapper");
                tableConfigurations.add(tc);
            });
        }
        List<Class> classes = PackageUtils.scan(packageToScan);
        classes.forEach(clazz -> {
            if (clazz.isAnnotationPresent(Entity.class)) {
                String className = clazz.getSimpleName();
                Table table = (Table) clazz.getAnnotation(Table.class);
                String tableName = table != null && StringUtils.isNotBlank(table.name()) ? table.name() : className;
                TableConfiguration tc = new TableConfiguration(context);
                tc.setDomainObjectName(className);
                tc.setTableName(tableName);
                tc.setMapperName(className + "Mapper");
                tableConfigurations.add(tc);
            }
        });
        return tableConfigurations;
    }
}
