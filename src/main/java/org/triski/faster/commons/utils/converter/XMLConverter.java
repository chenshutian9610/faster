package org.triski.faster.commons.utils.converter;

/**
 * @author triski
 * @date 2019/7/20
 */
public class XMLConverter implements StringConverter {
    @Override
    public String convert(String value) {
        if (value.contains("&")) {
            value = value.replace("&", "&amp;");
        }
        if (value.contains(">")) {
            value = value.replace(">", "&gt;");
        }
        if (value.contains("<")) {
            value = value.replace("<", "&lt;");
        }
        if (value.contains("'")) {
            value = value.replace("'", "&apos;");
        }
        if (value.contains("\"")) {
            value = value.replace("\"", "&quot;");
        }
        return value;
    }
}
