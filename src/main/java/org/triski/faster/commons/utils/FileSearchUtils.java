package org.triski.faster.commons.utils;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.triski.faster.commons.annotation.MainMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author triski
 * @date 2018/11/4
 * @export searchDirectory, searchFile, listFilesRecursively
 */
@UtilityClass
public class FileSearchUtils {

    private final Logger logger = LoggerFactory.getLogger(FileSearchUtils.class);

    /**
     * 在指定目录中寻找指定文件
     *
     * @param dir      目录
     * @param name     指定文件的名字
     * @param excludes 忽略目录
     */
    @MainMethod
    public File searchDirectory(File dir, String name, Set<String> excludes) {
        if (logger.isDebugEnabled()) {
            logger.debug("pass {}", dir.getPath());
        }

        if (dir.isDirectory() == false)
            return null;
        if (excludes.contains(dir.getName()))
            return null;
        if (dir.getName().equals(name))
            return dir;

        File[] files = dir.listFiles();
        File result = null;
        for (File file : files) {
            file = searchDirectory(file, name, excludes);
            if (file == null) continue;
            result = file;
            break;
        }
        return result;
    }

    public File searchDirectory(File dir, String name) {
        return searchDirectory(dir, name, Collections.emptySet());
    }

    /**
     * 在指定目录中寻找指定文件
     *
     * @param dir      目录
     * @param name     指定文件的名字
     * @param excludes 忽略目录
     */
    public File searchFile(File dir, String name, Set<String> excludes) {
        if (logger.isDebugEnabled()) {
            logger.debug("pass {}", dir.getPath());
        }

        if (excludes.contains(dir.getName()))
            return null;

        File result = null;
        if (dir.isDirectory() == true) {
            File[] files = dir.listFiles();
            for (File file : files) {
                file = searchFile(file, name, excludes);
                if (file == null) continue;
                result = file;
                break;
            }
        } else if (dir.getName().equals(name)) {
            result = dir;
        }
        return result;
    }

    public File searchFile(File dir, String name) {
        return searchFile(dir, name, Collections.emptySet());
    }

    /**
     * 递归列出文件对象
     *
     * @param dir      目录
     * @param list     最后返回的列表
     * @param excludes 忽略目录
     */
    @MainMethod
    private void listFilesRecursively(File dir, List<File> list, Set<String> excludes) {
        if (excludes.contains(dir.getName())) return;
        if (dir.isDirectory()) {
            if (logger.isDebugEnabled()) {
                logger.debug("pass {}", dir.getPath());
            }

            File[] files = dir.listFiles();
            for (File file : files)
                listFilesRecursively(file, list, excludes);
        }
        list.add(dir);
    }

    public List<File> listFilesRecursively(File dir, Set<String> excludes) {
        List<File> files = new ArrayList<>();
        listFilesRecursively(dir, files, excludes);
        return files;
    }

    public List<File> listFilesRecursively(File dir) {
        return listFilesRecursively(dir, Collections.emptySet());
    }
}
