package org.triski.faster.commons.utils;

import lombok.experimental.UtilityClass;
import org.triski.faster.commons.annotation.MainMethod;
import org.triski.faster.commons.exception.FasterException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * @author triski
 * @date 18-12-11
 * @export getProperties
 */
@UtilityClass
public class PropertiesUtils {

    public Properties getProperties(String classpath) {
        return getProperties0(() -> ClasspathUtils.getResourcesAsStream(classpath));
    }

    public Properties getProperties(File file) {
        return getProperties0(() -> IOStreamUtils.getInputStream(file));
    }

    @MainMethod
    private Properties getProperties0(Supplier<InputStream> supplier) {
        try (InputStream in = supplier.get()) {
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new FasterException(e);
        }
    }

}
