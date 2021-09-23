package com.techcourse.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementSetter {

    void setParams(PreparedStatement pstmt) throws SQLException;
}
