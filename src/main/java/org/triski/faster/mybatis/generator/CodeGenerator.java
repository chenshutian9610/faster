package org.triski.faster.mybatis.generator;

import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.triski.faster.commons.FasterConfiguration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author chenshutian
 * @date 2019/7/18
 */
public class CodeGenerator {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerator.class);

    private static final String MYBATIS_GENERATOR_CONFIG = "mybatis/generatorConfig.xml";

    private FasterConfiguration configuration;

    public CodeGenerator(FasterConfiguration configuration) {
        this.configuration = configuration;
    }

    public void generate(Map<String, String> class2table) {
        if (class2table.size() == 0) {
            logger.warn("class2table's size is 0");
            return;
        }
        try {
            // todo: get input stream
            InputStream in = null;
            // 逆向工程开始
            List<String> warnings = new ArrayList<>();
            Configuration config = new ConfigurationParser(warnings).parseConfiguration(in);
            DefaultShellCallback callback = new DefaultShellCallback(true);
            MyBatisGeneratorExtension generator = new MyBatisGeneratorExtension(config, callback, warnings);
            generator.setXmlMerge(false);
            generator.generate(toTableConfigurations(class2table));
            in.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<TableConfiguration> toTableConfigurations(Map<String, String> class2table) {
        List<TableConfiguration> tableConfigurations = new ArrayList<>();
        Context context = new Context(ModelType.CONDITIONAL);
        for (Map.Entry<String, String> entry : class2table.entrySet()) {
            TableConfiguration tc = new TableConfiguration(context);
            tc.setDomainObjectName(entry.getKey());
            tc.setMapperName(entry.getKey() + "Mapper");
            tc.setTableName(entry.getValue());
            tableConfigurations.add(tc);
        }
        return tableConfigurations;
    }
}
