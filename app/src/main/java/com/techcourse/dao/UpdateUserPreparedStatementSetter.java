package com.techcourse.dao;

import com.techcourse.domain.User;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateUserPreparedStatementSetter implements PreparedStatementSetter {

    private final User user;

    public UpdateUserPreparedStatementSetter(User user) {
        this.user = user;
    }

    @Override
    public void setValues(PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
        pstmt.setLong(4, user.getId());
    }
}
