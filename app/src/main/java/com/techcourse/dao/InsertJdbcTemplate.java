package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.util.Map;
import javax.sql.DataSource;

public class InsertJdbcTemplate extends JdbcTemplate<User> {

    private final DataSource dataSource;

    public InsertJdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected String createQuery() {
        return "insert into users (account, password, email) values (:account, :password, :email)";
    }

    @Override
    protected void setValues(final User user, final Map<String, Object> params) {
        params.put("account", user.getAccount());
        params.put("password", user.getPassword());
        params.put("email", user.getEmail());
    }

    @Override
    protected DataSource getDataSource() {
        return dataSource;
    }
}
