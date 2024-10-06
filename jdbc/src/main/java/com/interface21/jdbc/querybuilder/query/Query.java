package com.interface21.jdbc.querybuilder.query;

import com.interface21.jdbc.querybuilder.ConditionExpression;
import com.interface21.jdbc.querybuilder.QueryMethod;
import java.util.List;
import java.util.StringJoiner;

public abstract class Query {

    public static Query from(
            QueryMethod method,
            String table,
            List<String> fieldNames,
            List<ConditionExpression> conditions
    ) {
        if (method == QueryMethod.SELECT) {
            return SelectQuery.from(fieldNames, table, conditions);
        }

        if (method == QueryMethod.INSERT) {
            return InsertQuery.from(fieldNames, table);
        }

        if (method == QueryMethod.UPDATE) {
            return UpdateQuery.from(fieldNames, table, conditions);
        }

        throw new RuntimeException("쿼리를 구성할 수 없습니다.");
    }

    protected static StringJoiner getQueryJoiner() {
        return new StringJoiner(" ");
    }

    protected static StringJoiner getFieldJoiner() {
        return new StringJoiner(", ");
    }

    public abstract String getSql();
}
