package org.triski.faster.commons.utils;

import lombok.experimental.UtilityClass;

/**
 * @author chenshutian
 * @date 2019/7/16
 */
@UtilityClass
public class StringUtilsExt {

    /**
     * 将 msg 中的 {} 替换为 params 中的值
     */
    public String newMessage(String msg, Object... params) {
        int i = 0;
        while (msg.contains("{}") && i < params.length) {
            msg = msg.replaceFirst("\\{}", params[i++] + "");
        }
        return msg;
    }
}
