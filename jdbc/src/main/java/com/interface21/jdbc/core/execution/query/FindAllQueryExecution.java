package com.interface21.jdbc.core.execution.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FindAllQueryExecution<T> extends MultiQueryExecution<T> {

    @Override
    public List<T> executeInternal(
            ResultSet resultSet,
            QuerySpecification<T> specification
    ) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(specification.rowMapper().map(resultSet));
        }
        return results;
    }
}
