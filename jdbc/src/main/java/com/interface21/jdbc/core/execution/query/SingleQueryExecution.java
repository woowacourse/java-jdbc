package com.interface21.jdbc.core.execution.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public abstract class SingleQueryExecution<T> {

    public final Optional<T> execute(
            PreparedStatement preparedStatement,
            QuerySpecification<T> specification
    ) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            return executeInternal(resultSet, specification);
        }
    }

    protected abstract Optional<T> executeInternal(
            ResultSet resultSet,
            QuerySpecification<T> specification
    ) throws SQLException;
}
