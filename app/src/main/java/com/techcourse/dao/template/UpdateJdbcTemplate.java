package com.techcourse.dao.template;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.util.Map;
import javax.sql.DataSource;

public class UpdateJdbcTemplate extends JdbcTemplate<User> {

    private final DataSource dataSource;

    public UpdateJdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected String createQuery() {
        return "update users set account = :account, password = :password, email = :email where id = :id";
    }

    @Override
    protected void setValues(final User user, final Map<String, Object> params) {
        params.put("account", user.getAccount());
        params.put("password", user.getPassword());
        params.put("email", user.getEmail());
        params.put("id", user.getId());
    }

    @Override
    protected DataSource getDataSource() {
        return dataSource;
    }
}
