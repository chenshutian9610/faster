package org.triski.faster.commons;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.triski.faster.commons.utils.PropertiesUtils;
import org.triski.faster.commons.utils.YamlUtils;
import java.util.Properties;

/**
 * @author chenshutian
 * @date 2019/7/19
 * @export load
 */
public class FasterProperties extends Properties {
    private static final Logger logger = LoggerFactory.getLogger(FasterProperties.class);

    public static final String DATABASE_DRIVER_CLASS_NAME = "datasource.driverClassName";
    public static final String DATABASE_URL = "datasource.url";
    public static final String DATABASE_USERNAME = "datasource.username";
    public static final String DATABASE_PASSWORD = "datasource.password";

    public static final String MYBATIS_GENERATOR_STYLE_DATE_API = "mybatisGenerator.style.dateAPI";
    public static final String MYBATIS_GENERATOR_STYLE_LOMBOK = "mybatisGenerator.style.lombok";
    public static final String MYBATIS_GENERATOR_STYLE_COMMENT = "mybatisGenerator.style.comment";

    public static final String MYBATIS_GENERATOR_PLUGIN = "mybatisGenerator.plugin";
    public static final String ROOT_PACKAGE = "mybatisGenerator.rootPackage";
    public static final String JAVA_DIR = "mybatisGenerator.javaDir";
    public static final String RESOURCES_DIR = "mybatisGenerator.resourcesDir";

    private static volatile FasterProperties fasterProperties = new FasterProperties();

    private FasterProperties() {
    }

    public static FasterProperties get() {
        return fasterProperties;
    }

    public static synchronized FasterProperties load(String classpath) {
        fasterProperties.clear();
        Properties properties = null;
        if (classpath.endsWith(".properties")) {
            properties = (FasterProperties) PropertiesUtils.getProperties(classpath);
        } else if (StringUtils.endsWithAny(classpath, ".yml", ".yaml")) {
            properties = YamlUtils.getProperties(classpath);
        }
        if (properties != null) {
            properties.forEach((k, v) -> {
                if (k != null) {
                    String key = k.toString();
                    if (key.startsWith("spring.")) {
                        key = key.substring("spring.".length());
                    }
                    if (v != null) {
                        fasterProperties.setProperty(key, v.toString());
                    }
                }
            });
        }
        if (logger.isDebugEnabled()) {
            logger.debug("init properties: {}", fasterProperties);
        }
        return fasterProperties;
    }
}
