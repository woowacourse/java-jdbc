package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.util.HashMap;
import java.util.Map;

public class InsertJdbcTemplate {

    private final JdbcTemplate jdbcTemplate;

    public InsertJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private String createQueryForInsert() {
        return "insert into users (account, password, email) values (:account, :password, :email)";
    }

    public void insert(User user, UserDao userDao) {
        final var sql = createQueryForInsert();
        Map<String, Object> params = new HashMap<>();
        setValuesForInsert(user, params);
        jdbcTemplate.update(sql, params);
    }

    private void setValuesForInsert(User user, Map<String, Object> params) {
        params.put("account", user.getAccount());
        params.put("password", user.getPassword());
        params.put("email", user.getEmail());
    }
}
