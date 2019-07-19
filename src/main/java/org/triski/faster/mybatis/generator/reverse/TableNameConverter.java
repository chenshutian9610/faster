package org.triski.faster.mybatis.generator.reverse;


import org.triski.faster.commons.utils.CamelCaseUtils;

/**
 * @author chenshutian
 * @date 2019/7/19
 */
public interface TableNameConverter {
    String convert(String tableName);
}
