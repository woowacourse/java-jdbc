package com.interface21.jdbc.core.execution.query;

import com.interface21.jdbc.core.RowMapper;
import com.interface21.jdbc.core.preparedstatement.PreparedStatementSpecification;

public record QuerySpecification<T>(
        RowMapper<T> rowMapper,
        PreparedStatementSpecification preparedStatementSpecification
) {

    public QuerySpecification {
        validateRowMapper(rowMapper);
        validatePreparedStatementSpecification(preparedStatementSpecification);
    }

    private void validateRowMapper(RowMapper<?> rowMapper) {
        if (rowMapper == null) {
            throw new IllegalArgumentException("RowMapper는 null일 수 없습니다.");
        }
    }

    private void validatePreparedStatementSpecification(PreparedStatementSpecification preparedStatementSpecification) {
        if (preparedStatementSpecification == null) {
            throw new IllegalArgumentException("PreparedStatementSpecification는 null일 수 없습니다.");
        }
    }
}
