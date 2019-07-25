package org.triski.faster.commons.enumeration;

/**
 * @author triski
 * @date 2019/7/21
 */
public enum DateTimeRegexEnum {
    DATE_TIME("^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$"),
    DATE("^[0-9]{4}-[0-9]{2}-[0-9]{2}$"),
    TIME("^[0-9]{2}:[0-9]{2}:[0-9]{2}$");

    private String regex;

    DateTimeRegexEnum(String regex) {
        this.regex = regex;
    }

    public boolean match(String datetimeStr) {
        return datetimeStr.trim().matches(regex);
    }
}
