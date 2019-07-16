package org.triski.faster.commons.utils;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author triski
 * @date 2018/12/28
 * @export scan
 */
@UtilityClass
public class PackageUtils {

    private final Logger logger = LoggerFactory.getLogger(PackageUtils.class);

    public List<Class> scan(String packageToScan) {
        // 获取扫描的路径
        File dir = ClasspathUtils.getFileByPackageName(packageToScan);

        if (logger.isDebugEnabled()) {
            logger.debug("scanned directory is '{}', package's name is '{}'", dir.getAbsolutePath(), packageToScan);
        }

        // 获取路径下所有类
        List<Class> list = new ArrayList<>();
        scan0(dir, packageToScan, list);

        if (logger.isDebugEnabled()) {
            logger.debug("scanned result {size: {}}: {}", list.size(), list);
        }

        return list;
    }

    /**
     * 递归获取 dir 目录下所有的类
     *
     * @param dir         扫描的目录
     * @param packageName 包名
     * @param clazzList   返回的类列表
     */
    private void scan0(File dir, String packageName, List<Class> clazzList) {
        // 扫描
        if (dir.isDirectory()) {
            if (!packageName.endsWith(dir.getName()))
                packageName = packageName + "." + dir.getName();
            File[] files = dir.listFiles();
            for (File file : files)
                scan0(file, packageName, clazzList);
            return;
        }
        // 获取类
        String className = packageName + "." + dir.getName().substring(0, dir.getName().indexOf("."));
        try {
            clazzList.add(Class.forName(className));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return;
    }
}
