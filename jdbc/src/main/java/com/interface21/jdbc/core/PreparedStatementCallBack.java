package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallBack<R> {

    R doInPreparedStatement(PreparedStatement ps) throws SQLException;
}
