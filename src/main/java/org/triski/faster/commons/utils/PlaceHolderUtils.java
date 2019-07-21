package org.triski.faster.commons.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.triski.faster.commons.annotation.MainMethod;
import org.triski.faster.commons.utils.converter.StringConverter;
import java.util.Map;
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

    public String process(String msg, Object[] params) {
        int i = 0;
        while (msg.contains("{}") && i < params.length) {
            msg = msg.replaceFirst("\\{}", params[i++] + "");
        }
        return msg;
    }

    public String process(String msg, Map map) {
        return process(msg, map, value -> value);
    }

    @MainMethod
    public String process(String msg, Map map, StringConverter converter) {
        StringBuilder sb = new StringBuilder(msg);
        Matcher matcher = Pattern.compile("\\$\\{[^}]+}").matcher(msg);
        while (matcher.find()) {
            // ${var:value}
            if (matcher.group().contains(":")) {
                Object value = map.get(matcher.group().substring(2, matcher.group().indexOf(":")));
                if (value == null) {
                    value = matcher.group().substring(matcher.group().indexOf(":") + 1, matcher.group().length() - 1);
                }
                // 如果 StringBuilder 替换超过两次, 则更新 msg 的值, 避免正则表达式对同一个词的重复匹配, 下同
                if (StringBuilderUtils.replace(sb, matcher.group(), converter.convert(value.toString())) > 1) {
                    msg = sb.toString();
                }
            }
            // ${var}
            else {
                Object value = map.get(matcher.group().substring(2, matcher.group().length() - 1));
                if (value != null) {
                    if (StringBuilderUtils.replace(sb, matcher.group(), converter.convert(value.toString())) > 1) {
                        msg = sb.toString();
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * @reason 每次替换都会产生一个新的 string
     * @new process(String, Properties)
     */
    @Deprecated
    private String process_old(String msg, Properties properties) {
        Matcher matcher = Pattern.compile("\\$\\{[^}]+}").matcher(msg);
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
