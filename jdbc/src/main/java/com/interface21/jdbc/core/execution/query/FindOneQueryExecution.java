package com.interface21.jdbc.core.execution.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class FindOneQueryExecution<T> implements SingleQueryExecution<T> {

    @Override
    public Optional<T> execute(
            PreparedStatement preparedStatement,
            QuerySpecification<T> specification
    ) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                T result = specification.rowMapper().map(resultSet);
                if (resultSet.next()) {
                    throw new RuntimeException("쿼리 결과가 2개 이상입니다.");
                }
                return Optional.of(result);
            }
            return Optional.empty();
        }
    }
}
