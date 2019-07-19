package org.triski.faster.commons.utils;

import lombok.experimental.UtilityClass;
import org.triski.faster.commons.exception.FasterException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author chenshutian
 * @date 2019/7/19
 */
@UtilityClass
public class IOStreamUtils {
    public InputStream getInputStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new FasterException(e);
        }
    }
}
