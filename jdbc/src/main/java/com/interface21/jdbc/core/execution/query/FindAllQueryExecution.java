package com.interface21.jdbc.core.execution.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FindAllQueryExecution<T> implements MultiQueryExecution<T> {

    @Override
    public Collection<T> execute(
            PreparedStatement preparedStatement,
            QuerySpecification<T> specification
    ) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(specification.rowMapper().map(resultSet));
            }
            return results;
        }
    }
}
