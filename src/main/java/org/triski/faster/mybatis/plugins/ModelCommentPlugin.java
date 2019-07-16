package org.triski.faster.mybatis.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.triski.faster.commons.annotation.Comment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author triski
 * @date 2018/12/4
 * <p>
 * 使用 @Comment 为所有 model 类添加注释
 */
public class ModelCommentPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }


    /****************************** 为 model 类添加注释 *******************************/

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String annotation = Comment.class.getCanonicalName();
        topLevelClass.addImportedType(new FullyQualifiedJavaType(annotation));
        Map<String, String> comments = getColumnComment(introspectedTable);
        List<Field> fields = topLevelClass.getFields();
        String comment;
        for (Field field : fields) {
            comment = comments.get(field.getName());
            if (comment != null && comment.length() != 0)
                field.addAnnotation(getAnnotationString(annotation, comment));
        }
        return true;
    }

    /* 获取所有字段的注释 */
    private Map<String, String> getColumnComment(IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> columns = introspectedTable.getBaseColumns();
        Map<String, String> result = new HashMap<>();
        for (IntrospectedColumn column : columns)
            result.put(column.getJavaProperty(), column.getRemarks());
        List<IntrospectedColumn> keys = introspectedTable.getPrimaryKeyColumns();
        for (IntrospectedColumn key : keys)
            result.put(key.getJavaProperty(), key.getRemarks());
        return result;
    }

    /* 获取 @Comment("$VALUE") 的字符串 */
    private String getAnnotationString(String className, String value) {
        if (className.contains("."))
            className = className.substring(className.lastIndexOf(".") + 1);
        return "@" + className + "(\"" + value + "\")";
    }
}
