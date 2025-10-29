package com.interface21.jdbc.core.preparedstatement;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public record PreparedStatementSpecification(String sql, List<PreparedStatementParameter> parameters) {

    public PreparedStatementSpecification {
        validateSpecification(sql, parameters);
    }

    public static Builder builder(String sql) {
        return new Builder(sql);
    }

    private void validateSpecification(
            String sql,
            List<PreparedStatementParameter> parameters
    ) {
        long placeholderCount = sql
                .chars()
                .filter(ch -> ch == '?')
                .count();
        long parameterCount = parameters.size();

        if (placeholderCount != parameterCount) {
            throw new IllegalArgumentException(
                    new SQLSyntaxErrorException("PreparedStatementContext의 SQL과 파라미터의 개수가 일치하지 않습니다.")
            );
        }
    }

    public static class Builder {
        private final String sql;
        private final List<PreparedStatementParameter> parameters = new ArrayList<>();

        public Builder(String sql) {
            this.sql = sql;
        }

        public Builder parameter(PreparedStatementParameter parameter) {
            this.parameters.add(parameter);
            return this;
        }

        public Builder parameters(PreparedStatementParameter... parameters) {
            this.parameters.addAll(Arrays.stream(parameters).toList());
            return this;
        }

        public Builder parameters(Collection<PreparedStatementParameter> parameters) {
            this.parameters.addAll(parameters);
            return this;
        }

        public PreparedStatementSpecification build() {
            return new PreparedStatementSpecification(
                    sql,
                    parameters
            );
        }
    }
}
