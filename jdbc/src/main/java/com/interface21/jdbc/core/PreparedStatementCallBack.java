package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementCallBack<T> {

    T processIn(final PreparedStatement pstmt) throws SQLException;
}
