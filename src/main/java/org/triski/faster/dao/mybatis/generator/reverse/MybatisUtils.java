package org.triski.faster.dao.mybatis.generator.reverse;

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

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author chenshutian
 * @date 2019/7/18
 */
@UtilityClass
public class MybatisUtils {
    private static final Logger logger = LoggerFactory.getLogger(MybatisUtils.class);

    public void generateModel(InputStream inputStream) {
        try {
            List<String> warnings = new ArrayList<>();
            Configuration configuration = new ConfigurationParser(warnings).parseConfiguration(inputStream);
            DefaultShellCallback callback = new DefaultShellCallback(true);
            MyBatisGenerator generator = new MyBatisGenerator(configuration, callback, warnings);
            generator.generate(null);
        } catch (IOException | SQLException | InterruptedException | InvalidConfigurationException | XMLParserException e) {
            throw new FasterException(e);
        }
    }

    public void generateModel(String propertiesClasspath, Map<String, String> tables) {
        if (tables.size() == 0) {
            return;
        }
        FasterProperties fasterProperties = config(propertiesClasspath);
        // 逆向工程开始
        try (InputStream in = MybatisXmlUtils.process(fasterProperties)) {
            List<String> warnings = new ArrayList<>();
            List<TableConfiguration> tableConfigurations = toTableConfigurations(tables);
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

    private FasterProperties config(String propertiesClasspath) {
        FasterProperties fasterProperties = FasterProperties.load(propertiesClasspath);
        if (StringUtils.isBlank(fasterProperties.getProperty(FasterProperties.ROOT_PACKAGE))) {
            String className = new RuntimeException().getStackTrace()[1].getClassName();
            String packageName = className.substring(0, className.lastIndexOf("."));
            fasterProperties.setProperty(FasterProperties.ROOT_PACKAGE, packageName);
        }
        return fasterProperties;
    }

    private List<TableConfiguration> toTableConfigurations(Map<String, String> tables) {
        List<TableConfiguration> tableConfigurations = new ArrayList<>();
        Context context = new Context(ModelType.CONDITIONAL);
        tables.forEach((tableName, className) -> {
            TableConfiguration tc = new TableConfiguration(context);
            tc.setDomainObjectName(className);
            tc.setTableName(tableName);
            tc.setMapperName(className + "Mapper");
            tableConfigurations.add(tc);
        });
        return tableConfigurations;
    }
}
