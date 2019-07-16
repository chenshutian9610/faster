package org.triski.faster.mybatis.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.*;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author triski
 * @date 2019/6/2
 */
public class Model2TablePlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        ColumnEnum column = new ColumnEnum("Column");
        column.setVisibility(JavaVisibility.PUBLIC);
        column.setColumnMap(getColumnMap(introspectedTable));
        column.setTableName(introspectedTable.getTableConfiguration().getTableName());

        try {
            Class.forName("org.tree.support.mybatis.sql.SQLColumn");
            topLevelClass.addImportedType("SQLColumn");
            column.addSuperInterface(new FullyQualifiedJavaType("SQLColumn"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        column.addField(new Field("fieldName", new FullyQualifiedJavaType("private String")));
        column.addField(new Field("columnName", new FullyQualifiedJavaType("private String")));

        Method getJavaFieldName = new Method("getJavaFieldName");
        getJavaFieldName.setVisibility(JavaVisibility.PUBLIC);
        getJavaFieldName.setReturnType(new FullyQualifiedJavaType("String"));
        getJavaFieldName.addBodyLine("return this.fieldName;");
        column.addMethod(getJavaFieldName);

        Method getColumnName = new Method("getColumnName");
        getColumnName.setVisibility(JavaVisibility.PUBLIC);
        getColumnName.setReturnType(new FullyQualifiedJavaType("String"));
        getColumnName.addBodyLine("return this.columnName;");
        column.addMethod(getColumnName);

        Method getTableName = new Method("getTableName");
        getTableName.setVisibility(JavaVisibility.PUBLIC);
        getTableName.setReturnType(new FullyQualifiedJavaType("String"));
        getTableName.addBodyLine(String.format("return \"%s\";",
                introspectedTable.getTableConfiguration().getTableName()));
        column.addMethod(getTableName);

        Method toString = new Method("toString");
        toString.setVisibility(JavaVisibility.PUBLIC);
        toString.setReturnType(new FullyQualifiedJavaType("String"));
        toString.addAnnotation("@Override");
        toString.addBodyLine("return this.columnName;");
        column.addMethod(toString);

        Method all = new Method("all");
        all.setStatic(true);
        all.setReturnType(new FullyQualifiedJavaType("Column[]"));
        all.setVisibility(JavaVisibility.PUBLIC);
        all.addBodyLine("return Column.values();");
        column.addMethod(all);

        Method except = new Method("except");
        except.setReturnType(new FullyQualifiedJavaType("Column[]"));
        except.setVisibility(JavaVisibility.PUBLIC);
        except.setStatic(true);
        except.addParameter(new Parameter(new FullyQualifiedJavaType("Column"), "columns", true));
        except.addBodyLine("List<Column> temp = Arrays.asList(columns);");
        except.addBodyLine("return Arrays.stream(Column.values()).filter(column -> !temp.contains(column)).toArray(Column[]::new);");
        column.addMethod(except);

        topLevelClass.addImportedType("java.util.*");
        topLevelClass.addInnerEnum(column);
        return true;
    }

    private Map<String, String> getColumnMap(IntrospectedTable table) {
        Map<String, String> result = new LinkedHashMap<>();
        List<IntrospectedColumn> columns = table.getAllColumns();
        columns.forEach(column -> result.put(column.getActualColumnName(), column.getJavaProperty()));
        return result;
    }

    private static class ColumnEnum extends InnerEnum {
        private Map<String, String> columnMap;
        private String tableName;

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public void setColumnMap(Map<String, String> map) {
            columnMap = map;
        }

        ColumnEnum(String enumName) {
            super(new FullyQualifiedJavaType(enumName));
        }

        @Override
        public void addEnumConstant(String enumConstant) {
            throw new RuntimeException("ColumnEnum 不支持此方法");
        }

        @Override
        public String getFormattedContent(int indentLevel, CompilationUnit compilationUnit) {
            StringBuilder sb = new StringBuilder();
            this.addFormattedJavadoc(sb, indentLevel);
            this.addFormattedAnnotations(sb, indentLevel);
            OutputUtilities.javaIndent(sb, indentLevel);
            if (this.getVisibility() == JavaVisibility.PUBLIC) {
                sb.append(this.getVisibility().getValue());
            }

            sb.append("enum ");
            sb.append(this.getType().getShortName());
            Iterator fldIter;
            if (getSuperInterfaceTypes().size() > 0) {
                sb.append(" implements ");
                boolean comma = false;

                FullyQualifiedJavaType fqjt;
                for (fldIter = getSuperInterfaceTypes().iterator(); fldIter.hasNext(); sb.append(JavaDomUtils.calculateTypeName(compilationUnit, fqjt))) {
                    fqjt = (FullyQualifiedJavaType) fldIter.next();
                    if (comma) {
                        sb.append(", ");
                    } else {
                        comma = true;
                    }
                }
            }

            sb.append(" {");
            ++indentLevel;
            Iterator strIter = getEnumConstants().iterator();

            while (strIter.hasNext()) {
                OutputUtilities.newLine(sb);
                OutputUtilities.javaIndent(sb, indentLevel);
                String enumConstant = (String) strIter.next();
                sb.append(enumConstant);
                if (strIter.hasNext()) {
                    sb.append(',');
                } else {
                    sb.append(';');
                }
            }

            if (columnMap != null) {
                final int indent = indentLevel;
                columnMap.forEach((column, field) -> {
                    OutputUtilities.newLine(sb);
                    OutputUtilities.javaIndent(sb, indent);
                    sb.append(String.format("%s(\"%s\", \"%s.%s\"),", column.toUpperCase(), field, tableName, column));
                });
                if (sb.toString().endsWith(","))
                    sb.replace(sb.length() - 1, sb.length(), ";");
            } else {
                OutputUtilities.newLine(sb);
                OutputUtilities.javaIndent(sb, indentLevel);
                sb.append(";");
            }

            if (getFields().size() > 0) {
                OutputUtilities.newLine(sb);
            }

            fldIter = getFields().iterator();

            while (fldIter.hasNext()) {
                OutputUtilities.newLine(sb);
                Field field = (Field) fldIter.next();
                sb.append(field.getFormattedContent(indentLevel, compilationUnit));
                if (fldIter.hasNext()) {
                    OutputUtilities.newLine(sb);
                }
            }

            sb.append("\n\n")
                    .append("\t\t").append(String.format("%s(String fieldName, String columnName) {", getType().getShortName()))
                    .append("\n\t\t\t").append("this.fieldName = fieldName;")
                    .append("\n\t\t\t").append("this.columnName = columnName;")
                    .append("\n\t\t").append("}");

            if (getMethods().size() > 0) {
                OutputUtilities.newLine(sb);
            }

            Iterator mtdIter = getMethods().iterator();

            while (mtdIter.hasNext()) {
                OutputUtilities.newLine(sb);
                Method method = (Method) mtdIter.next();
                sb.append(method.getFormattedContent(indentLevel, false, compilationUnit));
                if (mtdIter.hasNext()) {
                    OutputUtilities.newLine(sb);
                }
            }

            if (getInnerClasses().size() > 0) {
                OutputUtilities.newLine(sb);
            }

            Iterator icIter = getInnerClasses().iterator();

            while (icIter.hasNext()) {
                OutputUtilities.newLine(sb);
                InnerClass innerClass = (InnerClass) icIter.next();
                sb.append(innerClass.getFormattedContent(indentLevel, compilationUnit));
                if (icIter.hasNext()) {
                    OutputUtilities.newLine(sb);
                }
            }

            if (getInnerEnums().size() > 0) {
                OutputUtilities.newLine(sb);
            }

            Iterator ieIter = getInnerEnums().iterator();

            while (ieIter.hasNext()) {
                OutputUtilities.newLine(sb);
                InnerEnum innerEnum = (InnerEnum) ieIter.next();
                sb.append(innerEnum.getFormattedContent(indentLevel, compilationUnit));
                if (ieIter.hasNext()) {
                    OutputUtilities.newLine(sb);
                }
            }

            --indentLevel;
            OutputUtilities.newLine(sb);
            OutputUtilities.javaIndent(sb, indentLevel);
            sb.append('}');
            return sb.toString();
        }
    }
}
