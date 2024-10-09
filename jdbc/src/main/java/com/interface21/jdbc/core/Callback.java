package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface Callback<T> {

    T start(PreparedStatement pstmt) throws SQLException;
}
