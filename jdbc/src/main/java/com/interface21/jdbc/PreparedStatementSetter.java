package com.interface21.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementSetter {

    public void setValue(PreparedStatement psmt) throws SQLException;
}
