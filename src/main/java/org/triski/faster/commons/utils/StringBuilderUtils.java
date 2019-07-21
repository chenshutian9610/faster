package org.triski.faster.commons.utils;

import lombok.experimental.UtilityClass;

/**
 * @author triski
 * @date 2019/7/20
 */
@UtilityClass
public class StringBuilderUtils {
    /** 和 String 一样的替换功能, 最后返回替换的次数 */
    public int replace(StringBuilder sb, String oldStr, String newStr) {
        int i, j, n = 0;
        while (sb.indexOf(oldStr) != -1) {
            i = sb.indexOf(oldStr);
            j = i + oldStr.length();
            sb.replace(i, j, newStr);
            n++;
        }
        return n;
    }
}
