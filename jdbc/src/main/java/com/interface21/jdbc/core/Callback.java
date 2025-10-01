package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@FunctionalInterface
public interface Callback<T> {

    T call(PreparedStatement pstmt) throws SQLException;
}
