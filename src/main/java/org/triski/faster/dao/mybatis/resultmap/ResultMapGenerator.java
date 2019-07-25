package org.triski.faster.dao.mybatis.resultmap;

import com.thoughtworks.xstream.XStream;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author triski
 * @date 2019/7/12
 */
@UtilityClass
public class ResultMapGenerator {

    private final Logger logger = LoggerFactory.getLogger(ResultMapGenerator.class);

    /**
     * 生成一个 ResultMap.xml 文件
     *
     * @param targetDir 目标目录
     * @param classes
     */
    public void generate(String targetDir, Class... classes) {
        XStream xStream = new XStream();
        xStream.processAnnotations(new Class[]{Mapper.class, ResultMap.class, Result.class});
        List<ResultMap> resultMaps = new ArrayList<>(classes.length);
        for (Class clazz : classes) {
            resultMaps.add(convert(clazz));
        }
        String namespace = "mapper.MyResultMap";
        Mapper mapper = new Mapper(namespace, resultMaps);
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\r\n");
        xml.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">").append("\r\n");
        xml.append(xStream.toXML(mapper));
        String result = xml.toString();

        if (logger.isDebugEnabled()) {
            logger.debug("generated string start:\n{}", result);
            logger.debug("generated string end.");
        }

        try {
            String path = targetDir + "/" + namespace.replaceAll("\\.", "/") + ".xml";

            if (logger.isDebugEnabled()) {
                logger.debug("write generated string to {}", path);
            }

            FileUtils.writeStringToFile(new File(path), result, "utf-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将一个类转换为一个 resultMap 标签
     */
    private ResultMap convert(Class clazz) {
        Field[] fields = clazz.getDeclaredFields();
        Result id = null, result = null;
        List<Result> results = new ArrayList<>(fields.length);
        String columnName, fieldName;
        for (Field field : fields) {
            fieldName = field.getName();
            columnName = fieldName.contains("_") ? fieldName :
                    String.join("_", StringUtils.splitByCharacterTypeCamelCase(fieldName)).toLowerCase();
            result = new Result(columnName, fieldName);
            if (field.isAnnotationPresent(ResultMapID.class)) {
                id = result;
            } else {
                results.add(result);
            }
        }
        String instanceName = clazz.getSimpleName();
        instanceName = instanceName.substring(0, 1).toLowerCase() + instanceName.substring(1);
        ResultMap resultMap = new ResultMap(instanceName, clazz.getCanonicalName());
        resultMap.setIdTag(id);
        resultMap.setResultTags(results);

        if (logger.isDebugEnabled()) {
            logger.debug("conversion result of {} is {}", clazz, resultMap);
        }

        return resultMap;
    }
}
