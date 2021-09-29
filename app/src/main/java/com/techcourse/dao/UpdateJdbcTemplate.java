package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UpdateJdbcTemplate extends JdbcTemplate {

    private final DataSource dataSource;

    public UpdateJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected DataSource getDatasource() {
        return dataSource;
    }

    @Override
    protected String createQuery() {
        return "update users set account = ?, password = ?, email = ?  where id = ?";
    }

    @Override
    protected void setValues(User user, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
        pstmt.setLong(4, user.getId());
    }
}
