package org.triski.faster.commons;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.triski.faster.commons.utils.PropertiesUtils;
import org.triski.faster.commons.utils.YamlUtils;

import java.util.Properties;

/**
 * @author chenshutian
 * @date 2019/7/19
 */
@Data
public class FasterProperties {
    private static final Logger logger = LoggerFactory.getLogger(FasterProperties.class);

    public static final String DATABASE_DRIVER_CLASS_NAME = "";
    public static final String DATABASE_URL = "";
    public static final String DATABASE_USERNAME = "";
    public static final String DATABASE_PASSWORD = "";

    public static final String MYBATIS_GENERATOR_STYLE = "";
    public static final String MYBATIS_GENERATOR_PLUGIN = "";

    public static final String ROOT_PACKAGE = "";
    public static final String JAVA_DIR = "";
    public static final String RESOURCES_DIR = "";

    private Properties properties = new Properties();

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public static FasterProperties load(String classpath) {
        FasterProperties fasterProperties = new FasterProperties();
        Properties properties = null;
        if (classpath.endsWith(".properties")) {
            properties = PropertiesUtils.getProperties(classpath);
        } else if (StringUtils.endsWithAny(classpath, ".yml", ".yaml")) {
            properties = YamlUtils.getProperties(classpath);
        }

        if (properties != null) {
            properties.forEach((k, v) -> {
                String key = (String) k;
                if (key.startsWith("spring.")) {
                    key = key.substring("spring.".length());
                }
                fasterProperties.properties.setProperty(key, (String) v);
            });
        }

        if (logger.isDebugEnabled()) {
            logger.debug("init properties: {}", fasterProperties.properties);
        }
        return fasterProperties;
    }
}
