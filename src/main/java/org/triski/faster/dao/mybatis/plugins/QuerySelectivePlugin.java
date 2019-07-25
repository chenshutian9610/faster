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
 * @author triski
 * @date 2019/7/21
 */
public class QuerySelectivePlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    /************************** 向 XML 文件添加 id = querySelective 的 <select/> ***************************/

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        String tableName = introspectedTable.getTableConfiguration().getTableName();

        // <select id="querySelective" parameterType="map" resultMap="BaseResultMap"/>
        XmlElement querySelective = new XmlElement("select");
        querySelective.addAttribute(new Attribute("id", "querySelective"));
        querySelective.addAttribute(new Attribute("parameterType", "map"));
        querySelective.addAttribute(new Attribute("resultMap", "BaseResultMap"));
        querySelective.addElement(new TextElement("select"));
        // <if test="example.distinct">distinct</if>
        XmlElement _if = new XmlElement("if");
        _if.addAttribute(new Attribute("test", "example.distinct"));
        _if.addElement(new TextElement("distinct"));
        querySelective.addElement(_if);
        querySelective.addElement(new TextElement(String.format("${columns} from %s", tableName)));
        // <if test="example != null"><include refid="Update_By_Example_Where_Clause"/></if>
        XmlElement _include = new XmlElement("include");
        _include.addAttribute(new Attribute("refid", "Update_By_Example_Where_Clause"));
        XmlElement _if2 = new XmlElement("if");
        _if2.addAttribute(new Attribute("test", "example != null"));
        _if2.addElement(_include);
        querySelective.addElement(_if2);
        // <if test="example.orderByClause != null">order by ${example.orderByClause}</if>
        XmlElement _if3 = new XmlElement("if");
        _if3.addAttribute(new Attribute("test", "example.orderByClause != null"));
        _if3.addElement(new TextElement("order by ${example.orderByClause}"));
        querySelective.addElement(_if3);
        document.getRootElement().addElement(querySelective);

        // <select id="querySelectiveByPrimaryKey" parameterType="map" resultMap="BaseResultMap"/>
        List<IntrospectedColumn> keys = introspectedTable.getPrimaryKeyColumns();
        if (keys.size() == 1) {
            String keyColumn = keys.get(0).getActualColumnName();
            String keyProperty = keys.get(0).getJavaProperty();
            XmlElement querySelectiveByPrimaryKey = new XmlElement("select");
            querySelectiveByPrimaryKey.addAttribute(new Attribute("id", "querySelectiveByPrimaryKey"));
            querySelectiveByPrimaryKey.addAttribute(new Attribute("parameterType", "map"));
            querySelectiveByPrimaryKey.addAttribute(new Attribute("resultMap", "BaseResultMap"));
            querySelectiveByPrimaryKey.addElement(new TextElement("select"));
            querySelectiveByPrimaryKey.addElement(new TextElement(String.format("${columns} from %s", tableName)));
            querySelectiveByPrimaryKey.addElement(new TextElement(String.format("where %s = #{%s}", keyColumn, keyProperty)));
            document.getRootElement().addElement(querySelectiveByPrimaryKey);
        }

        return true;
    }

    /****************************** 向 Mapper 接口添加 querySelective 方法 *******************************/

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String model = introspectedTable.getTableConfiguration().getDomainObjectName();
        String modelPackage = context.getJavaModelGeneratorConfiguration().getTargetPackage();

        Method querySelective = new Method("querySelective");
        querySelective.addParameter(new Parameter(new FullyQualifiedJavaType(String.format("%sColumn", model)), "columns", "@Param(\"columns\")"));
        querySelective.addParameter(new Parameter(new FullyQualifiedJavaType(String.format("%sExample", model)), "example", "@Param(\"example\")"));
        querySelective.setReturnType(new FullyQualifiedJavaType(String.format("java.util.List<%s>", model)));
        querySelective.setVisibility(JavaVisibility.PUBLIC);
        interfaze.addMethod(querySelective);

        List<IntrospectedColumn> keys = introspectedTable.getPrimaryKeyColumns();
        if (keys.size() == 1) {
            FullyQualifiedJavaType keyType = keys.get(0).getFullyQualifiedJavaType();
            String keyProperty = keys.get(0).getJavaProperty();
            Method querySelectiveByPrimaryKey = new Method("querySelectiveByPrimaryKey");
            querySelectiveByPrimaryKey.addParameter(new Parameter(new FullyQualifiedJavaType(String.format("%sColumn", model)), "columns", "@Param(\"columns\")"));
            querySelectiveByPrimaryKey.addParameter(new Parameter(keyType, keyProperty, String.format("@Param(\"%s\")", keyProperty)));
            querySelectiveByPrimaryKey.setReturnType(new FullyQualifiedJavaType(model));
            querySelectiveByPrimaryKey.setVisibility(JavaVisibility.PUBLIC);
            interfaze.addMethod(querySelectiveByPrimaryKey);
        }

        interfaze.addImportedType(new FullyQualifiedJavaType(String.format("%s.%sColumn", modelPackage, model)));
        return true;
    }
}
