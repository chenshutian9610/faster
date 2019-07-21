package org.triski.faster.mybatis.plugins;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.triski.faster.commons.FasterProperties;
import org.triski.faster.commons.annotation.Comment;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author triski
 * @date 2018/12/4
 */
public class ModelStylePlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    private void replaceDateAPI(TopLevelClass topLevelClass, Class<? extends Temporal> dateClass) {
        List<Field> fields = topLevelClass.getFields();
        long count = fields.stream().filter(field -> "Date".equals(field.getType().getShortName())).map(field -> {
            field.setType(new FullyQualifiedJavaType(dateClass.getCanonicalName()));
            return field;
        }).count();
        if (count != 0) {
            topLevelClass.addImportedType(new FullyQualifiedJavaType(dateClass.getCanonicalName()));
            topLevelClass.getImportedTypes().removeIf(type -> type.equals(new FullyQualifiedJavaType(Date.class.getCanonicalName())));
        }
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        FasterProperties properties = FasterProperties.get();

        // java8 风格
        String dateAPI = properties.getProperty(FasterProperties.MYBATIS_GENERATOR_STYLE_DATE_API);
        if (StringUtils.equalsAny(dateAPI, "Instant", "LocalDateTime")) {
            switch (dateAPI) {
                case "Instant":
                    replaceDateAPI(topLevelClass, Instant.class);
                    break;
                case "LocalDateTime":
                    replaceDateAPI(topLevelClass, LocalDateTime.class);
                    break;
            }
        }

        // lombok 风格
        boolean lombokEnable = Objects.equals(properties.getProperty(FasterProperties.MYBATIS_GENERATOR_STYLE_LOMBOK), "true");
        if (lombokEnable) {
            topLevelClass.addImportedType(new FullyQualifiedJavaType(Data.class.getCanonicalName()));
            topLevelClass.addImportedType(new FullyQualifiedJavaType(Accessors.class.getCanonicalName()));
            topLevelClass.addAnnotation("@Data");
            topLevelClass.addAnnotation("@Accessors(chain = true)");
            topLevelClass.getMethods().clear();
        }

        // 添加注释 (使用 @Comment 或 javadoc 的方式)
        Map<String, String> comments = getColumnComment(introspectedTable);
        String annotation = "";
        boolean commentEnable = Objects.equals(properties.getProperty(FasterProperties.MYBATIS_GENERATOR_STYLE_COMMENT), "true");
        if (commentEnable) {
            annotation = Comment.class.getCanonicalName();
            topLevelClass.addImportedType(new FullyQualifiedJavaType(annotation));
        }
        List<Field> fields = topLevelClass.getFields();
        for (Field field : fields) {
            String comment = comments.get(field.getName());
            if (StringUtils.isNotBlank(comment)) {
                if (commentEnable) {
                    field.addAnnotation(getAnnotationString(annotation, comment));
                } else {
                    field.addJavaDocLine(String.format("/** %s */", comment));
                }
            }
        }
        return true;
    }

    /** 获取所有字段的注释 */
    private Map<String, String> getColumnComment(IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> columns = introspectedTable.getBaseColumns();
        Map<String, String> result = new HashMap<>();
        for (IntrospectedColumn column : columns) {
            result.put(column.getJavaProperty(), column.getRemarks());
        }
        List<IntrospectedColumn> keys = introspectedTable.getPrimaryKeyColumns();
        for (IntrospectedColumn key : keys) {
            result.put(key.getJavaProperty(), key.getRemarks());
        }
        return result;
    }

    /** 获取 @Comment("$VALUE") 的字符串 */
    private String getAnnotationString(String className, String value) {
        if (className.contains(".")) {
            className = className.substring(className.lastIndexOf(".") + 1);
        }
        return String.format("@%s(\"%s\")", className, value);
    }
}
