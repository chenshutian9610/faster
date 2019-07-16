package org.triski.faster.commons.utils;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.InputStream;

/**
 * @author chenshutian
 * @date 2019/7/16
 */
@UtilityClass
public class ClasspathUtils {

    public InputStream getResourcesAsStream(String classpath) {
        return ClasspathUtils.class.getClassLoader().getResourceAsStream(classpath);
    }

    public File getFileByPackageName(String packageName) {
        return getFile(packageName.replace(".", "/"));
    }

    public File getFile(String classpath) {
        String path = ClasspathUtils.class
                .getClassLoader()
                .getResource(classpath)
                .getFile()
                .replaceAll("%20", " ");
        return new File(path);
    }
}
