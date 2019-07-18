package org.triski.faster.commons.utils;

import lombok.experimental.UtilityClass;
import org.triski.faster.commons.exception.FasterException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @author triski
 * @date 2019/6/1
 */
@UtilityClass
public class YamlUtils {

    public Properties getProperties(File file) {
        Yaml yaml = new Yaml();
        Map<String, Object> yml = null;
        try (InputStream in = new FileInputStream(file)) {
            yml = yaml.load(in);
        } catch (IOException e) {
            throw new FasterException(e);
        }
        Properties properties = new Properties();
        yml2properties(null, yml, properties);
        return properties;
    }

    public Properties getProperties(String classpath) {
        Yaml yaml = new Yaml();
        Map<String, Object> yml = null;
        try (InputStream in = ClassLoader.getSystemResourceAsStream(classpath)) {
            yml = yaml.load(in);
        }catch (IOException e){
            throw new FasterException(e);
        }
        Properties properties = new Properties();
        yml2properties(null, yml, properties);
        return properties;
    }

    private void yml2properties(String prefix, Map<String, ?> yml, Properties properties) {
        if (yml == null) {
            return;
        }
        yml.forEach((key, value) -> {
            if (prefix != null)
                key = String.format("%s.%s", prefix, key);

            if (value instanceof Map) {
                yml2properties(key, (Map<String, ?>) value, properties);
            } else {
                properties.setProperty(key, (String) value);
            }
        });
    }
}
