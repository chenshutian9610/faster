package org.triski.faster.dao.hibernate.utils;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.dialect.MySQL55Dialect;
import org.triski.faster.commons.FasterProperties;

/**
 * @author chenshutian
 * @date 2019/7/26
 */
public class MySQL5DialectExt extends MySQL55Dialect {
    private FasterProperties properties = FasterProperties.get();

    @Override
    public String getTableTypeString() {
        String charsetStr = " CHARSET=utf8 ";
        if (properties != null) {
            String charset = properties.getProperty(FasterProperties.HBM2DDL_CHARSET);
            if (StringUtils.isNotBlank(charset)) {
                if (charset.toLowerCase().equals("utf-8")) {
                    charset = "utf8";
                }
                charsetStr = String.format(" CHARSET=%s ", charset);
            }
        }
        return super.getTableTypeString() + charsetStr;
    }
}
