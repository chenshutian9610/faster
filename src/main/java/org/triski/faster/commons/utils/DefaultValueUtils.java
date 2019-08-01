package org.triski.faster.commons.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

/**
 * @author chenshutian
 * @date 2019/7/29
 */
@UtilityClass
public class DefaultValueUtils {
    public <T> T getDefaultValueIfNull(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    public String getDefaultValueIfEmpty(String value, String defaultValue) {
        return StringUtils.isNotEmpty(value) ? value : defaultValue;
    }

    public String getDefaultValueIfBlank(String value, String defaultValue) {
        return StringUtils.isNotBlank(value) ? value : defaultValue;
    }
}
