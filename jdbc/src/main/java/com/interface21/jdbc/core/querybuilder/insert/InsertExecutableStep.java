package com.interface21.jdbc.core.querybuilder.insert;

import java.sql.Connection;

public interface InsertExecutableStep {
    InsertExecutableStep value(String column, Object value);
    void execute(Connection connection);
}
