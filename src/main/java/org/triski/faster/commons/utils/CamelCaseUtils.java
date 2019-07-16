package org.triski.faster.commons.utils;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.triski.faster.commons.annotation.MainMethod;

/**
 * @author triski
 * @date 2019/7/14
 * @describe 下划线和驼峰式字符串的转换
 * @export toUnderline, toCamel, toCapitalizeCamel, toUnCapitalizeCamel
 */
@UtilityClass
public class CamelCaseUtils {

    private final Logger logger = LoggerFactory.getLogger(CamelCaseUtils.class);

    @MainMethod
    public String toUnderline(String str) {
        StringBuilder sb = new StringBuilder();
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (Character.isUpperCase(chars[i])) {
                sb.append("_").append(Character.toLowerCase(chars[i]));
            } else {
                sb.append(Character.toLowerCase(chars[i]));
            }
        }

        String result = sb.toString();
        if (logger.isDebugEnabled()) {
            logger.debug("input string is '{}' and output string is '{}'", str, result);
        }
        return result;
    }

    @MainMethod
    public String toCamel(String str, boolean capitalize) {
        StringBuilder sb = new StringBuilder();
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i == 0) {
                sb.append(capitalize ? Character.toUpperCase(chars[0]) : Character.toLowerCase(chars[0]));
            } else {
                char c = chars[i] == '_' ? Character.toUpperCase(chars[++i]) : chars[i];
                sb.append(c);
            }
        }

        String result = sb.toString();
        if (logger.isDebugEnabled()) {
            logger.debug("input string is '{}' and output string is '{}'", str, result);
        }
        return result;
    }

    public String toCapitalizeCamel(String str) {
        return toCamel(str, true);
    }

    public String toUnCapitalizeCamel(String str) {
        return toCamel(str, false);
    }
}
