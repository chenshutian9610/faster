package org.triski.faster.dao.hibernate.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.triski.faster.commons.annotation.Comment;
import org.triski.faster.commons.annotation.DefaultValue;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author chenshutian
 * @date 2019/7/26
 */
@UtilityClass
public class TableAnnotationUtils {

    public Map<String, Object> process(List<Class> classes) {
        Map<String, Object> map = new HashMap<>();
        classes.forEach(clazz -> {
            String className = clazz.getSimpleName();
            Table table = (Table) clazz.getAnnotation(Table.class);
            boolean tableNameEnable = table != null && table.name().length() != 0;
            // @Comment for @Table
            Comment tableComment = (Comment) clazz.getAnnotation(Comment.class);
            if (tableComment != null && StringUtils.isNotBlank(tableComment.value())) {
                putIntoMap(map, tableNameEnable ? table.name() : className, "comment", tableComment.value());
            }
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                Column column = (Column) field.getAnnotation(Column.class);
                boolean columnNameEnable = column != null && column.name().length() != 0;
                String fieldName = field.getName();
                // @Comment for @Column
                Comment columnComment = (Comment) field.getAnnotation(Comment.class);
                if (columnComment != null && StringUtils.isNotBlank(columnComment.value())) {
                    putIntoMap(map,
                            tableNameEnable ? table.name() : className,
                            columnNameEnable ? column.name() : fieldName,
                            "comment", columnComment.value());
                }
                // @DefaultValue for @Column
                DefaultValue columnDefaultValue = (DefaultValue) field.getAnnotation(DefaultValue.class);
                if (columnDefaultValue != null && StringUtils.isNotBlank(columnDefaultValue.value())) {
                    putIntoMap(map,
                            tableNameEnable ? table.name() : className,
                            columnNameEnable ? column.name() : fieldName,
                            "defaultValue", String.format("'%s'", columnDefaultValue.value()));
                }
            }
        });
        return map;
    }

    private void putIntoMap(Map<String, Object> map, String className, String key, Object value) {
        if (value != null) {
            if (value instanceof String && Objects.equals("", value)) {
                return;
            }
            map.put(String.format("%s#%s", className, key), value);
        }
    }

    private void putIntoMap(Map<String, Object> map, String className, String fieldName, String key, Object value) {
        if (value != null) {
            if (value instanceof String && Objects.equals("", value)) {
                return;
            }
            map.put(String.format("%s#%s#%s", className, fieldName, key), value);
        }
    }
}
