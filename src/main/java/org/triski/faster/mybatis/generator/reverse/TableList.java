package org.triski.faster.mybatis.generator.reverse;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.triski.faster.commons.utils.CamelCaseUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenshutian
 * @date 2019/7/19
 */
@Data
public class TableList {
    private TableNameConverter converter;
    private TableNameConverter defaultConverter = tableName -> CamelCaseUtils.toCapitalizeCamel(tableName);
    private List<TableInfo> tableInfos = new ArrayList<>();

    public TableList() {
    }

    public TableList(TableNameConverter converter) {
        this.converter = converter;
    }

    public TableList addTableMap(TableInfo tableMap) {
        if (StringUtils.isBlank(tableMap.getClassName())) {
            if (converter == null) {
                converter = defaultConverter;
            }
            tableMap.setClassName(converter.convert(tableMap.getTableName()));
        }
        tableInfos.add(tableMap);
        return this;
    }
}
