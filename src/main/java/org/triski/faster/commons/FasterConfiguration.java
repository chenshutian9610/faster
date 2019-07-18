package org.triski.faster.commons;

import lombok.Data;

import java.util.Properties;

/**
 * @author chenshutian
 * @date 2019/7/18
 */
@Data
public class FasterConfiguration {
    private String dbDriverClassName;
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    private String codeStyle;

    public FasterConfiguration(Properties properties, boolean ifSpringBoot) {
        dbDriverClassName = properties.getProperty((ifSpringBoot ? "spring." : "") + FasterConfigureConstant.DATABASE_DRIVER);
        dbUrl = properties.getProperty((ifSpringBoot ? "spring." : "") + FasterConfigureConstant.DATABASE_URL);
        dbUsername = properties.getProperty((ifSpringBoot ? "spring." : "") + FasterConfigureConstant.DATABASE_USERNAME);
        dbPassword = properties.getProperty((ifSpringBoot ? "spring." : "") + FasterConfigureConstant.DATABASE_PASSWORD);
        codeStyle = properties.getProperty(FasterConfigureConstant.CODE_STYLE);
    }
}
