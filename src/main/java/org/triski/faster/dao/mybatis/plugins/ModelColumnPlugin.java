package org.triski.faster.dao.mybatis.plugins;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.DefaultJavaFormatter;
import org.mybatis.generator.api.dom.java.*;

import java.util.*;

/**
 * @author triski
 * @date 2019/7/21
 */
public class ModelColumnPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        String targetProject = context.getJavaModelGeneratorConfiguration().getTargetProject();
        String targetPackage = context.getJavaModelGeneratorConfiguration().getTargetPackage();
        String modelName = introspectedTable.getTableConfiguration().getDomainObjectName();
        String tableName = introspectedTable.getTableConfiguration().getTableName();
        String className = modelName + "Column";
        Map<String, String> field2Column = getFiled2Column(introspectedTable);

        TopLevelClass column = new TopLevelClass(String.format("%s.%s", targetPackage, className));
        column.setVisibility(JavaVisibility.PUBLIC);

        column.addImportedType(new FullyQualifiedJavaType(Set.class.getCanonicalName()));
        column.addImportedType(new FullyQualifiedJavaType(HashSet.class.getCanonicalName()));
        column.addImportedType(new FullyQualifiedJavaType(StringJoiner.class.getCanonicalName()));

        Field hashSet = new Field("columns", new FullyQualifiedJavaType("Set<String>"));
        hashSet.setVisibility(JavaVisibility.PRIVATE);
        hashSet.setInitializationString("new HashSet<>()");
        column.addField(hashSet);
        Method getColumns = new Method("getColumns");
        getColumns.setReturnType(new FullyQualifiedJavaType("Set<String>"));
        getColumns.setVisibility(JavaVisibility.PUBLIC);
        getColumns.addBodyLine("return columns;");
        column.addMethod(getColumns);

        Method getTableName = new Method("getTableName");
        getTableName.setVisibility(JavaVisibility.PUBLIC);
        getTableName.setReturnType(new FullyQualifiedJavaType("String"));
        getTableName.addBodyLine(String.format("return \"%s\";", tableName));
        column.addMethod(getTableName);

        Method all = new Method("all");
        all.setVisibility(JavaVisibility.PUBLIC);
        all.setReturnType(new FullyQualifiedJavaType(className));
        field2Column.forEach((fieldName, columnName) -> {
            all.addBodyLine(String.format("columns.add(%s);", columnName.toUpperCase()));
        });
        all.addBodyLine("return this;");
        column.addMethod(all);

        field2Column.forEach((fieldName, columnName) -> {
            Field field = new Field(columnName.toUpperCase(), new FullyQualifiedJavaType("String"));
            field.setVisibility(JavaVisibility.PUBLIC);
            field.setStatic(true);
            field.setFinal(true);
            field.setInitializationString(String.format("\"%s\"", columnName));
            column.addField(field);
            Method method = new Method(fieldName);
            method.setReturnType(new FullyQualifiedJavaType(className));
            method.setVisibility(JavaVisibility.PUBLIC);
            method.addParameter(new Parameter(new FullyQualifiedJavaType("boolean"), "select"));
            method.addBodyLine(String.format("boolean bool = select ? columns.add(%s) : columns.remove(%s);", columnName.toUpperCase(), columnName.toUpperCase()));
            method.addBodyLine("return this;");
            column.addMethod(method);
        });

        Method toString = new Method("toString");
        toString.setReturnType(new FullyQualifiedJavaType("String"));
        toString.setVisibility(JavaVisibility.PUBLIC);
        toString.addAnnotation("@Override");
        toString.addBodyLine("StringJoiner sj = new StringJoiner(\", \");");
        toString.addBodyLine("columns.forEach(column -> sj.add(getTableName() + \".\" + column));");
        toString.addBodyLine("return sj.toString();");
        column.addMethod(toString);

        GeneratedJavaFile javaFile = new GeneratedJavaFile(column, targetProject, new DefaultJavaFormatter());
        return Arrays.asList(javaFile);
    }

    private Map<String, String> getFiled2Column(IntrospectedTable introspectedTable) {
        Map<String, String> field2Column = new HashMap<>();
        introspectedTable.getAllColumns().forEach(definition -> {
            field2Column.put(definition.getJavaProperty(), definition.getActualColumnName());
        });
        return field2Column;
    }
}
