package com.interface21.jdbc.core.execution.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

public abstract class MultiQueryExecution<T> {

    public final Collection<T> execute(
            PreparedStatement preparedStatement,
            QuerySpecification<T> specification
    ) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            return executeInternal(resultSet, specification);
        }
    }

    protected abstract Collection<T> executeInternal(
            ResultSet resultSet,
            QuerySpecification<T> specification
    ) throws SQLException;
}
