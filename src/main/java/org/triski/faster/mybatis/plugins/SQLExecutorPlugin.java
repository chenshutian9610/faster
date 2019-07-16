package org.triski.faster.mybatis.plugins;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.DefaultJavaFormatter;
import org.mybatis.generator.api.dom.java.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author triski
 * @date 2019/3/5
 */
public class SQLExecutorPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles() {
        List<GeneratedJavaFile> javaFiles = new ArrayList<>(1);
        String targetPackage = context.getJavaClientGeneratorConfiguration().getTargetPackage();
        String targetProject = context.getJavaModelGeneratorConfiguration().getTargetProject();

        Interface mapper = new Interface(targetPackage + ".SQLExecutor");
        mapper.setVisibility(JavaVisibility.PUBLIC);

        // Map<String, Object> query(@Param("sql") String sql);
        Parameter sql = new Parameter(new FullyQualifiedJavaType("String"), "sql", "@Param(\"sql\")");
        Method query = new Method("query");
        query.addAnnotation("@Select(\"${sql}\")");
        query.setReturnType(new FullyQualifiedJavaType("Map<String, Object>"));
        query.addParameter(sql);
        mapper.addImportedType(new FullyQualifiedJavaType("java.util.Map"));
        mapper.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
        mapper.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Select"));
        mapper.addMethod(query);

        // default <T> T query(String sql, Class<T> clazz) { ... }
        try {
            Class.forName("com.alibaba.fastjson.JSON");
            Parameter sql2 = new Parameter(new FullyQualifiedJavaType("String"), "sql");
            Parameter clazz = new Parameter(new FullyQualifiedJavaType("Class<T>"), "clazz");
            Method query2=new Method("query");
            query2.addTypeParameter(new TypeParameter("T"));
            query2.setReturnType(new FullyQualifiedJavaType("T"));
            query2.setDefault(true);
            query2.addParameter(sql2);
            query2.addParameter(clazz);
            query2.addBodyLine("return JSON.parseObject(JSON.toJSONString(this.query(sql)), clazz);");
            mapper.addImportedType(new FullyQualifiedJavaType("com.alibaba.fastjson.JSON"));
            mapper.addMethod(query2);
        } catch (ClassNotFoundException e) {}

        GeneratedJavaFile javaFile = new GeneratedJavaFile(mapper, targetProject, new DefaultJavaFormatter());
        javaFiles.add(javaFile);
        return javaFiles;
    }
}
