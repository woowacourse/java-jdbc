package com.interface21.jdbc.core.execution.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

public interface SingleQueryExecution<T> {

    Optional<T> execute(PreparedStatement preparedStatement, QuerySpecification<T> specification) throws SQLException;
}
