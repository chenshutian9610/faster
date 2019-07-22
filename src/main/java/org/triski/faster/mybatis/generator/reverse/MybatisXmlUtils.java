package org.triski.faster.mybatis.generator.reverse;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.triski.faster.commons.FasterProperties;
import org.triski.faster.commons.utils.ClasspathUtils;
import org.triski.faster.commons.utils.PlaceHolderUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author chenshutian
 * @date 2019/7/19
 */
@UtilityClass
public class MybatisXmlUtils {

    private static final String MYBATIS_GENERATOR_CONFIG = "mybatis/generatorConfig.xml";

    public InputStream process(FasterProperties fasterProperties) throws IOException {
        File file = ClasspathUtils.getFile(MYBATIS_GENERATOR_CONFIG);
        String xml = FileUtils.readFileToString(file, "utf-8");
        xml = processVariable(xml, fasterProperties);
        xml = processPlugin(xml, fasterProperties);
        return new ByteArrayInputStream(xml.getBytes("utf-8"));
    }

    private String processVariable(String xml, FasterProperties fasterProperties) {
        return PlaceHolderUtils.process(xml, fasterProperties.getProperties());
    }

    private String processPlugin(String xml, FasterProperties fasterProperties) {
        final String placeHolder = "<!--generator.define.plugins-->";
        final String template = "<plugin type=\"%s\"/>";
        String pluginStr = fasterProperties.getProperty(FasterProperties.MYBATIS_GENERATOR_PLUGIN);
        if (StringUtils.isNotBlank(pluginStr)) {
            StringBuilder sb = new StringBuilder();
            String[] pluginArr = pluginStr.split(",");
            for (String plugin : pluginArr) {
                sb.append(String.format(template, plugin)).append("\n");
            }
            return xml.replace(placeHolder, sb.toString());
        }
        return xml;
    }
}
