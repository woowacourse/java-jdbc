package com.interface21.jdbc.core.execution.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public abstract class MultiQueryExecution<T> extends QueryExecution<T, List<T>> {

    @Override
    public final List<T> execute(
            PreparedStatement preparedStatement,
            QuerySpecification<T> specification
    ) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            return executeInternal(resultSet, specification);
        }
    }

    protected abstract List<T> executeInternal(
            ResultSet resultSet,
            QuerySpecification<T> specification
    ) throws SQLException;
}
