package org.triski.faster.commons.utils;

import lombok.experimental.UtilityClass;
import org.triski.faster.commons.enumeration.DateTimeRegexEnum;
import org.triski.faster.commons.exception.FasterException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author triski
 * @date 2019/7/21
 */
@UtilityClass
public class InstantUtils {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ISO_LOCAL_TIME;

    public Instant parse(String datetimeStr) {
        DateTimeFormatter formatter = null;
        if (DateTimeRegexEnum.DATE_TIME.match(datetimeStr)) {
            formatter = DATE_TIME;
        } else if (DateTimeRegexEnum.DATE.match(datetimeStr)) {
            formatter = DATE;
        } else if (DateTimeRegexEnum.TIME.match(datetimeStr)) {
            formatter = TIME;
        } else {
            throw new FasterException("'{}' can not to be parsed", datetimeStr);
        }
        return LocalDateTime.parse(datetimeStr, formatter).atZone(ZoneId.systemDefault()).toInstant();
    }

    public String toString(Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).format(DATE_TIME);
    }

    public String toString(Instant instant, String pattern) {
        return instant.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(pattern));
    }
}
