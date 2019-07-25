package org.triski.faster.dao.mybatis.generator.reverse;

import lombok.Data;

/**
 * @author chenshutian
 * @date 2019/7/19
 */
@Data
public class TableInfo {
    private String tableName;
    private String className;

    public TableInfo(String tableName) {
        this.tableName = tableName;
    }

    public TableInfo(String tableName, String className) {
        this.tableName = tableName;
        this.className = className;
    }
}
