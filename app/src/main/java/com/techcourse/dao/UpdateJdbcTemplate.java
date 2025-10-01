package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.SqlExecutor;
import com.techcourse.domain.User;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateJdbcTemplate {

    private final JdbcTemplate jdbcTemplate;

    public UpdateJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void update(User user, UserDao userDao) {
        final var sql = createQueryForUpdate();
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        setValuesForUpdate(user, params);
        jdbcTemplate.update(sql, params);
    }

    private String createQueryForUpdate() {
        return "update users set account = :account, password = :password, email = :email where id = :id";
    }

    private void setValuesForUpdate(User user, java.util.Map<String, Object> params) {
        params.put("account", user.getAccount());
        params.put("password", user.getPassword());
        params.put("email", user.getEmail());
        params.put("id", user.getId());
    }
}
