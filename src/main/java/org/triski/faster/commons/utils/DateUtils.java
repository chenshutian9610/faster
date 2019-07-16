package org.triski.faster.commons.utils;

import lombok.experimental.UtilityClass;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author triski
 * @date 18-12-14
 * @export of, format, parse
 */
@UtilityClass
public class DateUtils {
    private final String datetime = "yyyy-MM-dd HH:mm:ss";
    private final String date = "yyyy-MM-dd";
    private final String time = "HH:mm:ss";

    public Date of(String datetimeStr) {
        return parse(datetimeStr);
    }

    public Date of(int year, int month, int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, date);
        return calendar.getTime();
    }

    public Date of(int year, int month, int date, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, date, hour, minute, second);
        return calendar.getTime();
    }

    public String format(Date date) {
        SimpleDateFormat df = new SimpleDateFormat(datetime);
        return df.format(date);
    }

    /**
     * @param datetimeString 可以是 {datetime}，{date} 或 {time}
     * @return
     */
    public Date parse(String datetimeString) {
        boolean isDate = datetimeString.contains("-");
        boolean isTime = datetimeString.contains(":");
        String format = "";
        if (isDate && isTime) {
            format = datetime;
        } else if (isDate) {
            format = date;
        } else if (isTime) {
            format = time;
        }
        SimpleDateFormat df = new SimpleDateFormat(format);
        try {
            return df.parse(datetimeString);
        } catch (ParseException e) {
            throw new RuntimeException(String.format("can not parse '%s' to date", datetimeString));
        }
    }
}