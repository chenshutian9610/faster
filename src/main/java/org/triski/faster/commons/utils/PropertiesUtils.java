package org.triski.faster.commons.utils;

import lombok.experimental.UtilityClass;
import org.triski.faster.commons.exception.FasterException;

import java.io.File;
import java.io.FileInputStream;
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
            throw new FasterException(e);
        }
    }

    public Properties getProperties(File file) {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            return properties;
        } catch (IOException e) {
            throw new FasterException(e);
        }
    }

}
