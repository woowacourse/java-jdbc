package com.techcourse.dao.jdbc.template;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementSetter {

    void setValues(PreparedStatement pstmt) throws SQLException;
}
