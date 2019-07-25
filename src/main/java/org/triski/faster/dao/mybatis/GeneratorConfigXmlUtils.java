package org.triski.faster.dao.mybatis;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.triski.faster.commons.FasterProperties;
import org.triski.faster.commons.utils.ClasspathUtils;
import org.triski.faster.commons.utils.PlaceHolderUtils;
import org.triski.faster.commons.utils.converter.XMLConverter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author chenshutian
 * @date 2019/7/19
 */
@UtilityClass
class GeneratorConfigXmlUtils {

    private static final String MYBATIS_GENERATOR_CONFIG = "mybatis/generatorConfig.xml";
    private static final String PLUGIN_PLACE_HOLDER = "<!--generator.define.plugins-->";
    private static final String PLUGIN_TAG_TEMPLATE = "<plugin type=\"%s\"/>";

    public InputStream process(FasterProperties fasterProperties) throws IOException {
        File file = ClasspathUtils.getFile(MYBATIS_GENERATOR_CONFIG);
        String xml = FileUtils.readFileToString(file, "utf-8");
        xml = processVariable(xml, fasterProperties);
        xml = processPlugin(xml, fasterProperties);
        return new ByteArrayInputStream(xml.getBytes("utf-8"));
    }

    private String processVariable(String xml, FasterProperties fasterProperties) {
        return PlaceHolderUtils.process(xml, fasterProperties, new XMLConverter());
    }

    private String processPlugin(String xml, FasterProperties fasterProperties) {
        String pluginStr = fasterProperties.getProperty(FasterProperties.MYBATIS_GENERATOR_PLUGIN);
        if (StringUtils.isNotBlank(pluginStr)) {
            StringBuilder sb = new StringBuilder();
            String[] pluginArr = pluginStr.split(",");
            for (String plugin : pluginArr) {
                sb.append(String.format(PLUGIN_TAG_TEMPLATE, plugin)).append("\n");
            }
            return xml.replace(PLUGIN_PLACE_HOLDER, sb.toString());
        }
        return xml;
    }
}
