package com.interface21.jdbc.core.execution.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

public interface MultiQueryExecution<T> {

    Collection<T> execute(PreparedStatement preparedStatement, QuerySpecification<T> specification) throws SQLException;
}
