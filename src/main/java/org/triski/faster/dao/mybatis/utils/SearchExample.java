package org.triski.faster.dao.mybatis.utils;

import org.apache.commons.lang3.StringUtils;
import org.triski.faster.commons.annotation.MainMethod;
import org.triski.faster.commons.exception.FasterException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author triski
 * @date 2019/7/30
 */
public class SearchExample {
    private String columnStr = "*";
    private String tableStr;
    private SearchCondition SearchCondition = new SearchCondition();

    public SearchExample select(String... columns) {
        columnStr = String.join(", ", columns);
        return this;
    }

    public SearchExample from(String... tables) {
        tableStr = String.join(", ", tables);
        return this;
    }

    public SearchCondition where() {
        return SearchCondition;
    }

    @Override
    public String toString() {
        if (StringUtils.isBlank(tableStr)) {
            throw new FasterException("please set table to select");
        }
        final String template = "select %s from %s where %s";
        return String.format(template, columnStr, tableStr, SearchCondition);
    }

    public static class SearchCondition {
        enum Operation {
            EQ("="),
            NE("!="),
            GT(">"),
            GE(">="),
            LT("<"),
            LE("<="),
            IN("in"),
            NOT_IN("not in");

            private String operation;

            Operation(String operation) {
                this.operation = operation;
            }

            String process(String column, Object value, boolean doubleQuote) {
                if (value instanceof Collection) {
                    StringJoiner sj = new StringJoiner(", ");
                    ((Collection) value).forEach(v -> sj.add(doubleQuote ? String.format("'%s'", v) : v + ""));
                    return String.format("and %s %s (%s) ", column, operation, sj.toString());
                }
                return String.format(doubleQuote ? "and %s %s '%s'" : "and %s %s %s", column, operation, value);
            }
        }

        private StringBuilder conditionStr = new StringBuilder("1 = 1 ");

        public SearchCondition eq(String column, Serializable value) {
            return operate(Operation.EQ, column, value, false);
        }

        public SearchCondition eq(String column, Serializable value, boolean doubleQuote) {
            return operate(Operation.EQ, column, value, doubleQuote);
        }

        public SearchCondition ne(String column, Serializable value) {
            return operate(Operation.NE, column, value, false);
        }

        public SearchCondition ne(String column, Serializable value, boolean doubleQuote) {
            return operate(Operation.NE, column, value, doubleQuote);
        }

        public SearchCondition gt(String column, Serializable value) {
            return operate(Operation.GT, column, value, false);
        }

        public SearchCondition gt(String column, Serializable value, boolean doubleQuote) {
            return operate(Operation.GT, column, value, doubleQuote);
        }

        public SearchCondition ge(String column, Serializable value) {
            return operate(Operation.GE, column, value, false);
        }

        public SearchCondition ge(String column, Serializable value, boolean doubleQuote) {
            return operate(Operation.GE, column, value, doubleQuote);
        }

        public SearchCondition lt(String column, Serializable value) {
            return operate(Operation.LT, column, value, false);
        }

        public SearchCondition lt(String column, Serializable value, boolean doubleQuote) {
            return operate(Operation.LT, column, value, doubleQuote);
        }

        public SearchCondition le(String column, Serializable value) {
            return operate(Operation.LE, column, value, false);
        }

        public SearchCondition le(String column, Serializable value, boolean doubleQuote) {
            return operate(Operation.LE, column, value, doubleQuote);
        }

        public SearchCondition in(String column, List<Serializable> value) {
            return operate(Operation.IN, column, value, false);
        }

        public SearchCondition in(String column, List<Serializable> value, boolean doubleQuote) {
            return operate(Operation.IN, column, value, doubleQuote);
        }

        public SearchCondition notIn(String column, List<Serializable> value) {
            return operate(Operation.NOT_IN, column, value, false);
        }

        public SearchCondition notIn(String column, List<Serializable> value, boolean doubleQuote) {
            return operate(Operation.NOT_IN, column, value, doubleQuote);
        }

        public SearchCondition between(String column, Serializable value, Serializable value2) {
            return between(column, value, value2, false);
        }

        public SearchCondition between(String column, Serializable value, Serializable value2, boolean doubleQuote) {
            conditionStr.append(doubleQuote ? "and %s between %s and %s " : "and %s between '%s' and '%s' ");
            return this;
        }

        public SearchCondition like(String column, String value) {
            conditionStr.append(String.format("and %s like '%%%s%% ", column, value));
            return this;
        }

        public SearchCondition isNull(String column) {
            conditionStr.append(String.format(String.format("and %s is null ", column)));
            return this;
        }

        public SearchCondition isNotNull(String column) {
            conditionStr.append(String.format(String.format("and %s is not null ", column)));
            return this;
        }

        public SearchCondition and(SearchCondition SearchCondition) {
            return bind("and", SearchCondition);
        }

        public SearchCondition or(SearchCondition SearchCondition) {
            return bind("or", SearchCondition);
        }

        @Override
        public String toString() {
            return conditionStr.toString();
        }

        @MainMethod
        private SearchCondition operate(Operation operation, String column, Object value, boolean doubleQuote) {
            conditionStr.append(operation.process(column, value, doubleQuote));
            return this;
        }

        @MainMethod
        private SearchCondition bind(String bind, SearchCondition SearchCondition) {
            String condition = conditionStr.toString();
            String condition2 = SearchCondition.conditionStr.toString();
            conditionStr.delete(0, conditionStr.length());
            conditionStr.append(String.format("((%s) %s (%s))", condition, bind, condition2));
            return this;
        }
    }
}
