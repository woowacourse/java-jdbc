package com.interface21.jdbc.core.execution.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class FindOneQueryExecution<T> extends SingleQueryExecution<T> {

    @Override
    public Optional<T> executeInternal(
            ResultSet resultSet,
            QuerySpecification<T> specification
    ) throws SQLException {
        if (!resultSet.next()) {
            return Optional.empty();
        }
        T result = specification.rowMapper().map(resultSet);
        if (resultSet.next()) {
            throw new RuntimeException("쿼리 결과가 2개 이상입니다.");
        }
        return Optional.of(result);
    }
}
