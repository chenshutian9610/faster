package org.triski.faster.mybatis.generator.reverse;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.triski.faster.commons.FasterProperties;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenshutian
 * @date 2019/7/18
 */
@Data
public class MybatisGeneratorExt {
    private static final Logger logger = LoggerFactory.getLogger(MybatisGeneratorExt.class);

    private static final String MYBATIS_GENERATOR_CONFIG = "mybatis/generatorConfig.xml";

    private FasterProperties fasterProperties;
    private InputStream inputStream;

    public MybatisGeneratorExt(String propertiesClasspath) {
        fasterProperties = FasterProperties.load(propertiesClasspath);
        if (StringUtils.isBlank(fasterProperties.getProperty(FasterProperties.ROOT_PACKAGE))) {
            String className = new RuntimeException().getStackTrace()[1].getClassName();
            String packageName = className.substring(0, className.lastIndexOf("."));
            fasterProperties.setProperty(FasterProperties.ROOT_PACKAGE, packageName);
        }
    }

    public void generate(TableList tableList) {
        if (tableList.getTableInfos().size() == 0) {
            logger.warn("tableList's size is 0");
            return;
        }
        // 逆向工程开始
        try (InputStream in = inputStream != null ? inputStream : MybatisXmlUtils.process(fasterProperties)) {
            List<String> warnings = new ArrayList<>();
            Configuration config = new ConfigurationParser(warnings).parseConfiguration(in);
            DefaultShellCallback callback = new DefaultShellCallback(true);
            MyBatisGenerator generator = new MyBatisGenerator(config, callback, warnings);
            generator.setXmlMerge(false);
            generator.generate(toTableConfigurations(tableList));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<TableConfiguration> toTableConfigurations(TableList tableList) {
        List<TableConfiguration> tableConfigurations = new ArrayList<>();
        Context context = new Context(ModelType.CONDITIONAL);
        tableList.getTableInfos().forEach(tableInfo -> {
            TableConfiguration tc = new TableConfiguration(context);
            tc.setDomainObjectName(tableInfo.getClassName());
            tc.setTableName(tableInfo.getTableName());
            tc.setMapperName(tableInfo.getClassName() + "Mapper");
            tableConfigurations.add(tc);
        });
        return tableConfigurations;
    }
}
