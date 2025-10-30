package com.interface21.jdbc.core.querybuilder.update;

import java.sql.Connection;

public interface UpdateExecutableStep {
    UpdateExecutableStep set(String column, Object value);
    UpdateExecutableStep where(String column, Object value);
    void execute(Connection connection);
}
