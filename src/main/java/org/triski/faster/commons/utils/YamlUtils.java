package org.triski.faster.commons.utils;

import lombok.experimental.UtilityClass;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @author triski
 * @date 2019/6/1
 */
@UtilityClass
public class YamlUtils {

    public Properties getProperties(String classpath) {
        Yaml yaml = new Yaml();
        InputStream in = ClassLoader.getSystemResourceAsStream(classpath);
        Map<String, Object> yml = yaml.load(in);
        Properties properties = new Properties();
        yml2properties(null, yml, properties);
        return properties;
    }

    private void yml2properties(String prefix, Map<String, ?> yml, Properties properties) {
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
