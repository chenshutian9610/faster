package org.triski.faster.commons;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.triski.faster.commons.utils.PropertiesUtils;
import org.triski.faster.commons.utils.YamlUtils;

import java.util.Objects;
import java.util.Properties;

/**
 * @author chenshutian
 * @date 2019/7/19
 * @export load, get
 */
public class FasterProperties extends Properties {
    private static final Logger logger = LoggerFactory.getLogger(FasterProperties.class);

    public static final String CONTROLLER_DEBUG = "controller.debug";

    public static final String DATABASE_DRIVER_CLASS_NAME = "datasource.driverClassName";
    public static final String DATABASE_URL = "datasource.url";
    public static final String DATABASE_USERNAME = "datasource.username";
    public static final String DATABASE_PASSWORD = "datasource.password";

    public static final String HBM2DDL_PACKAGE_TO_SCAN = "generator.hibernate.packageToScan";
    public static final String HBM2DDL_OPERATION_MODE = "generator.hibernate.hbm2ddl";
    public static final String HBM2DDL_TARGET_TYPE = "generator.hibernate.targetType";
    public static final String HBM2DDL_OUTPUT_FILE = "generator.hibernate.output";
    public static final String HBM2DDL_CHARSET = "generator.hibernate.charset";
    public static final String HBM2DDL_DATABASE_DIALECT = "generator.hibernate.dialect";

    public static final String MYBATIS_GENERATOR_STYLE_LOMBOK = "generator.mybatis.style.lombok";
    public static final String MYBATIS_GENERATOR_STYLE_COMMENT = "generator.mybatis.style.comment";
    public static final String MYBATIS_GENERATOR_STYLE_DATE_API = "generator.mybatis.style.dateApi";

    public static final String MYBATIS_GENERATOR_PLUGIN = "generator.mybatis.plugins";
    public static final String MYBATIS_GENERATOR_ROOT_PACKAGE = "generator.mybatis.rootPackage";
    public static final String MYBATIS_GENERATOR_JAVA_DIR = "generator.mybatis.javaDir";
    public static final String MYBATIS_GENERATOR_RESOURCES_DIR = "generator.mybatis.resourcesDir";

    private static volatile FasterProperties fasterProperties = new FasterProperties();
    private static String propertiesClasspath;

    private FasterProperties() {
    }

    public static FasterProperties get() {
        return fasterProperties;
    }

    public static synchronized FasterProperties load(String classpath) {
        if (Objects.equals(propertiesClasspath, classpath) && fasterProperties.size() != 0) {
            return get();
        }
        propertiesClasspath = classpath;
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
