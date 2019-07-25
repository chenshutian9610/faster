package org.triski.faster.commons.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.triski.faster.commons.annotation.MainMethod;
import org.triski.faster.commons.exception.FasterException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * @author triski
 * @date 2019/6/1
 * @export getProperties
 */
@UtilityClass
public class YamlUtils {

    public Properties getProperties(File file) {
        return getProperties0(() -> IOStreamUtils.getInputStream(file));
    }

    public Properties getProperties(String classpath) {
        return getProperties0(() -> ClasspathUtils.getResourcesAsStream(classpath));
    }

    @MainMethod
    private final Properties getProperties0(Supplier<InputStream> supplier) {
        Properties properties = new Properties();
        Yaml yaml = new Yaml();
        Map yml = null;
        try (InputStream in = supplier.get()) {
            yml = yaml.load(in);
        } catch (IOException e) {
            throw new FasterException(e);
        }
        ymlMap2properties(null, yml, properties);
        return properties;
    }

    /** 将 yml 加载出来的 map 转换为 properties */
    private void ymlMap2properties(String prefix, Map ymlMap, Properties properties) {
        if (ymlMap == null) {
            return;
        }
        ymlMap.forEach((key, value) -> {
            if (prefix != null)
                key = String.format("%s.%s", prefix, key);

            if (value instanceof Map) {
                ymlMap2properties(key.toString(), (Map) value, properties);
            } else if (value instanceof List) {
                if (value != null) {
                    String keyStr = key.toString();
                    String valueStr = StringUtils.join((List) value, ",");
                    if (key.toString().contains("-")) {
                        properties.setProperty(CamelCaseUtils.toUnCapitalizeCamel(keyStr), valueStr);
                    }
                    properties.setProperty(keyStr, valueStr);
                }
            } else {
                if (value != null) {
                    String keyStr = key.toString();
                    String valueStr = value.toString();
                    if (key.toString().contains("-")) {
                        properties.setProperty(CamelCaseUtils.toUnCapitalizeCamel(keyStr), valueStr);
                    }
                    properties.setProperty(keyStr, valueStr);
                }
            }
        });
    }
}
