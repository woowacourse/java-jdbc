package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PrepareStatementSetter {

    void setValue(PreparedStatement preparedStatement) throws SQLException;
}
