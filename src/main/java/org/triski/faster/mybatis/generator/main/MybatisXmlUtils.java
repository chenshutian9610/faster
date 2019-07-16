package org.triski.faster.mybatis.generator.main;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * @author triski
 * @date 2018/12/26
 * <p>
 * 修改 mybatis-generator.xml 中 ${var} 和 ${var:value}
 */
abstract class MybatisXmlUtils {

    private static Properties properties;

    static void config(Properties props) {
        properties = props;
    }

    private static String getRootPackage(File sourceDir, String prefix) {
        File[] files = sourceDir.listFiles();
        if (files.length == 1)
            return getRootPackage(files[0],
                    StringUtils.isNotBlank(prefix) ?
                            String.format("%s.%s", prefix, files[0].getName()) :
                            files[0].getName());
        return prefix;
    }

    /* 提供 ${var} 和 ${var:value} 的功能处理 */
    static InputStream deal(String configFile) throws Exception {
        InputStream inputStream = MybatisXmlUtils.class.getClassLoader().getResourceAsStream(configFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
        String currentLine, expression, ordinary, value, temp;
        int start = 0, end;
        StringBuilder sb = new StringBuilder();

        String rootPackage = properties.getProperty("generate.root.package");
        String module = properties.getProperty("generate.module");
        if (rootPackage == null) {
            String targetProject = properties.getProperty("generate.java.target");
            File dir = targetProject == null ?
                    new File(String.format("./%s/src/main/java", module == null ? "" : module)) :
                    new File(String.format("./%s/%s", module == null ? "" : module, targetProject));
            properties.put("generate.root.package", getRootPackage(dir, ""));
        }
        while ((currentLine = reader.readLine()) != null) {
            /* 1. 如果 generate.module 不为空，则忽略 model.target 等变量 */
            if (StringUtils.isNotBlank(module)) {
                // TODO: 2018/12/29
                if (currentLine.matches(".*\\$\\{generate\\.java\\.target.*\\}.*"))
                    currentLine = currentLine.replaceFirst("\\$\\{generate\\.java\\.target.*\\}",
                            String.format("./%s/src/main/java", module));
                else if (currentLine.matches(".*\\$\\{generate\\.xml\\.target.*\\}.*"))
                    currentLine = currentLine.replaceFirst("\\$\\{generate\\.xml\\.target.*\\}",
                            String.format("./%s/src/main/resources", module));
            }

            /* 2. 处理 ${var} 和 ${var:value} */
            end = 0;
            while ((start = currentLine.indexOf("${", start + 1)) != -1) {
                end = currentLine.indexOf("}", end + 1);
                expression = currentLine.substring(start + 2, end);
                ordinary = String.format("${%s}", expression);
                if (expression.contains(":")) {
                    temp = expression.substring(0, expression.indexOf(":"));
                    value = properties.getProperty(temp);
                    if (value == null && temp.matches("^datasource\\.\\w+$"))
                        value = properties.getProperty("spring." + temp);
                    currentLine = currentLine.replace(ordinary, value == null ? expression.substring(expression.indexOf(":") + 1) : value);
                } else {
                    value = properties.getProperty(expression);
                    if (value == null && expression.matches("^datasource\\.\\w+$"))
                        value = properties.getProperty("spring." + expression);
                    if (value != null) {
                        // 在 XML 中 & 为特殊字符，需要使用转义字符 &amp; 来取代
                        // 原来为：jdbc:mysql///demo?useUnicode=true&characterEncoding=utf8
                        // 变为  ：jdbc:mysql///demo?useUnicode=true&amp;characterEncoding=utf8
                        if (expression.endsWith("datasource.url"))
                            value = value.replaceAll("&", "&amp;");
                        currentLine = currentLine.replace(ordinary, value);
                    } else throw new RuntimeException(expression + " 没有配置");
                }
            }
            sb.append(currentLine).append("\n");
        }

        /* 4. 添加插件配置 */
        String done = new String(sb);
        done = done.replace("<!--generator.define.plugins-->", getPlugins());

        return new ByteArrayInputStream(done.getBytes("utf-8"));
    }

    // 插件添加
    private static String getPlugins() {
        /* 读取 plugin 配置 */
        String prefix = properties.getProperty("generate.mybatis.plugin.prefix");
        String names = properties.getProperty("generate.mybatis.plugin");
        String otherSupport = properties.getProperty("generate.other-support.plugin");

        /* 转化为 plugin list */
        List<String> plugins = new LinkedList<>();
        if (StringUtils.isNotBlank(names)) {
            String[] temps = names.split(",");
            for (String temp : temps)
                plugins.add((StringUtils.isNotBlank(prefix) ? prefix : "org.mybatis.generator.plugins") + "." + temp);
        }
        if (StringUtils.isNotBlank(otherSupport)) {
            String[] temps = otherSupport.split(",");
            for (String temp : temps)
                plugins.add(temp);
        }

        /* 转化为有效格式 */
        StringBuffer sb = new StringBuffer();
        String template = "<plugin type=\"%s\"/>";
        for (String plugin : plugins)
            sb.append(String.format(template, plugin)).append('\n');
        return new String(sb);
    }
}
