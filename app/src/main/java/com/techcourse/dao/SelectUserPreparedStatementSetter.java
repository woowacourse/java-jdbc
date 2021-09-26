package com.techcourse.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SelectUserPreparedStatementSetter implements PreparedStatementSetter {

    private final Long id;

    public SelectUserPreparedStatementSetter(Long id) {
        this.id = id;
    }

    @Override
    public void setValues(PreparedStatement pstmt) throws SQLException {
        pstmt.setLong(1, id);
    }
}
