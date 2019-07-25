package org.triski.faster.dao.mybatis.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

/**
 * @author chenshutian
 * @date 2019/7/23
 * @usage 批量更新指定字段
 * @precondition 仅有一个主键
 */
public class BatchUpdatePlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (introspectedTable.getPrimaryKeyColumns().size() != 1) {
            return true;
        }
        String modelName = introspectedTable.getTableConfiguration().getDomainObjectName();
        Method method = new Method("batchUpdate");
        method.setReturnType(new FullyQualifiedJavaType("int"));
        method.addParameter(new Parameter(new FullyQualifiedJavaType(modelName + "Column"), "columns", "@Param(\"columns\")"));
        method.addParameter(new Parameter(new FullyQualifiedJavaType(String.format("List<%s>", modelName)), "records", "@Param(\"records\")"));
        interfaze.addMethod(method);
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        if (introspectedTable.getPrimaryKeyColumns().size() != 1) {
            return true;
        }
        String tableName = introspectedTable.getTableConfiguration().getTableName();
        IntrospectedColumn keyColumn = introspectedTable.getPrimaryKeyColumns().get(0);
        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();

        XmlElement batchUpdate = new XmlElement("update");
        batchUpdate.addAttribute(new Attribute("id", "batchUpdate"));
        batchUpdate.addAttribute(new Attribute("parameterType", "map"));

        batchUpdate.addElement(new TextElement(String.format("update %s set", tableName)));

        XmlElement trim = new XmlElement("trim");
        trim.addAttribute(new Attribute("suffixOverrides", ","));
        columns.forEach(column -> {
            XmlElement _if = new XmlElement("if");
            _if.addAttribute(new Attribute("test", String.format("columns.columns.contains('%s')", column.getActualColumnName())));
            _if.addElement(new TextElement(
                    String.format("%s = (case %s <foreach collection=\"records\" item=\"record\">when #{record.%s} then #{record.%s} </foreach>end ),"
                            , column.getActualColumnName(), keyColumn.getActualColumnName(), keyColumn.getJavaProperty(), column.getJavaProperty())));
            trim.addElement(_if);
        });
        batchUpdate.addElement(trim);

        batchUpdate.addElement(new TextElement(String.format("where %s in (", keyColumn.getActualColumnName())));

        XmlElement foreach = new XmlElement("foreach");
        foreach.addAttribute(new Attribute("collection", "records"));
        foreach.addAttribute(new Attribute("item", "record"));
        foreach.addAttribute(new Attribute("separator", ","));
        foreach.addElement(new TextElement(String.format("#{record.%s}", keyColumn.getJavaProperty())));
        batchUpdate.addElement(foreach);

        batchUpdate.addElement(new TextElement(")"));

        document.getRootElement().addElement(batchUpdate);
        return true;
    }
}
