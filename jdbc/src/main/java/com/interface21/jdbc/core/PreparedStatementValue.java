package com.interface21.jdbc.core;


import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementValue {
    void setValue(PreparedStatement pstmt)throws SQLException;
}
