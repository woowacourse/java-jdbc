package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementCaller<T> {

    T call(PreparedStatement psmt) throws SQLException;
}
