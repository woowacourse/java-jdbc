package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
interface PreparedStatementSetter {

    void setValues(PreparedStatement pstmt) throws SQLException;
}
