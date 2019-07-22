package org.triski.faster.commons.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author chenshutian
 * @date 2019/7/16
 * @placeholder {}, ${var}, ${var:value}
 * @export process
 */
@UtilityClass
public class PlaceHolderUtils {

    public String process(String msg, Object... params) {
        int i = 0;
        while (msg.contains("{}") && i < params.length) {
            msg = msg.replaceFirst("\\{}", params[i++] + "");
        }
        return msg;
    }

    public String process(String msg, Properties properties) {
        Matcher matcher = Pattern.compile("\\$\\{.\\w+[:]?\\w+}").matcher(msg);
        while (matcher.find()) {
            // ${var:value}
            if (matcher.group().contains(":")) {
                String value = properties.getProperty(matcher.group().substring(2, matcher.group().indexOf(":")));
                if (StringUtils.isBlank(value)) {
                    value = matcher.group().substring(matcher.group().indexOf(":") + 1, matcher.group().length() - 1);
                }
                msg = msg.replace(matcher.group(), value);
            }
            // ${var}
            else {
                String value = properties.getProperty(matcher.group().substring(2, matcher.group().length() - 1));
                if (StringUtils.isNotBlank(value)) {
                    msg = msg.replace(matcher.group(), value);
                }
            }
        }
        return msg;
    }
}
