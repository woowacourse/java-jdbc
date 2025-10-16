package com.interface21.jdbc.core.execution.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class QueryExecution<T, R> {

    public abstract R execute(
            PreparedStatement preparedStatement,
            QuerySpecification<T> specification
    ) throws SQLException;
}
