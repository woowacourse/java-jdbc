package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
interface StatementCallback<T> {

    T doInStatement(PreparedStatement pstmt) throws SQLException;
}
