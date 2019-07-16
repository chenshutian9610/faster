package org.triski.faster.commons.utils;

import lombok.experimental.UtilityClass;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author triski
 * @date 18-12-11
 */
@UtilityClass
public class PropertiesUtils {

    public Properties getProperties(String classpath) {
        try {
            Properties properties = new Properties();
            InputStream in = ClassLoader.getSystemResourceAsStream(classpath);
            properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(String.format("路径 %s 错误", classpath));
        }
    }

}
