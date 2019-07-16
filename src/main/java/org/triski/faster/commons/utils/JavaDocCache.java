package org.triski.faster.commons.utils;

import com.sun.javadoc.*;
import com.sun.tools.javadoc.Main;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * javadoc 解析器
 *
 * @author triski
 * @date 2019/7/15
 * @export init, getresult, getFieldDocMap
 * @notice 键中带 :: 表示方法, 带 # 表示字段
 * @optional 提供 {classpath}/javadoc.properties (只有一个 java.src.dir 属性需要配置)
 */
@UtilityClass
public class JavaDocCache {

    private final Logger logger = LoggerFactory.getLogger(JavaDocCache.class);

    private RootDoc root;
    private Map<String, String> result = new ConcurrentHashMap<>();

    /**
     * javadoc 初始化的 Doclet 需要有一个静态的 start 方法
     */
    public static boolean start(RootDoc root) {
        JavaDocCache.root = root;
        return true;
    }

    public Map<String, String> getResult() {
        return result;
    }

    public String getComment(String key) {
        String value = result.get(key);
        return value == null ? "" : value;
    }

    /**
     * 初始化 javadoc
     *
     * @param classes
     */
    public void init(List<Class> classes) {
        // 加载 javadoc.properties 的配置
        Properties properties = new Properties();
        File file = ClasspathUtils.getFile("javadoc.properties");
        if (file.exists() == false) {
            logger.warn("classpath isn't exists 'javadoc.properties' file");
        } else {
            try {
                properties.load(new FileInputStream(file));
            } catch (IOException e) {
                logger.error("javadoc.properties is invalid");
            }
        }
        // 初始化 root 的前置判断
        String srcDir = properties.getProperty("javadoc.src.dir");
        if (StringUtils.isBlank(srcDir)) {
            logger.warn("'javadoc.src.dir' isn't configured, so take the default config (src/main/java)");
            srcDir = "src/main/java";
        }
        if (new File(srcDir).exists() == false) {
            logger.warn("the file of 'java.src.dir' is not exists");
            return;
        }
        // 组建命令参数
        final String[] prefix = {
                "-doclet", JavaDocCache.class.getName(),
                "-encoding", "utf-8"
        };
        String[] main = getFiles(srcDir, classes);
        String[] args = new String[prefix.length + main.length];
        System.arraycopy(prefix, 0, args, 0, prefix.length);
        System.arraycopy(main, 0, args, prefix.length, main.length);
        // 初始化 javadoc
        Main.execute(args);
        if (root == null) {
            logger.error("can not init javadoc!");
            return;
        }
        // 初始化 result 和 fieldDocMap
        ClassDoc[] classDocs = root.classes();
        String key;
        for (ClassDoc classDoc : classDocs) {
            result.put(classDoc.name(), classDoc.commentText());
            FieldDoc[] fieldDocs = classDoc.fields(false);
            for (FieldDoc fieldDoc : fieldDocs) {
                key = String.format("%s#%s", classDoc.name(), fieldDoc.name());
                result.put(key, fieldDoc.commentText());
            }
            MethodDoc[] methodDocs = classDoc.methods();
            for (MethodDoc methodDoc : methodDocs) {
                key = String.format("%s::%s", classDoc.name(), methodDoc.name());
                result.put(key, methodDoc.commentText());
                Tag[] tags = methodDoc.tags("@param");
                for (Tag tag : tags) {
                    ParamTag paramTag = (ParamTag) tag;
                    key = String.format("%s::%s#%s", classDoc.name(), methodDoc.name(), paramTag.parameterName());
                    result.put(key, paramTag.parameterComment());
                }
            }
        }
    }

    /**
     * 从类列表转换为路径列表, 如 org.tree.model.User -> {srcDir}/org/tree/model/User.java
     *
     * @param srcDir  源码目录, 如 src/main/java
     * @param classes 类列表
     * @return 路径列表
     */
    private String[] getFiles(String srcDir, List<Class> classes) {
        classes.removeIf(clazz ->  clazz.getPackage().getName().equals("java.lang"));
        return classes.stream()
                .map(clazz -> srcDir + "/" + clazz.getCanonicalName().replaceAll("\\.", "/") + ".java")
                .toArray(String[]::new);
    }
}