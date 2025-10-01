package com.techcourse.dao.template;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

public class SelectJdbcTemplate extends JdbcTemplate<User> {

    private final DataSource dataSource;

    public SelectJdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected String createQuery() {
        throw new UnsupportedOperationException("This template is for select queries only.");
    }

    @Override
    protected void setValues(final User user, final Map<String, Object> params) {
        throw new UnsupportedOperationException("This template is for select queries only.");
    }

    @Override
    protected DataSource getDataSource() {
        return dataSource;
    }

    private final RowMapper<User> rowMapper = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return query(sql, rowMapper);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = :account";
        final Map<String, Object> params = createParamsForFindByAccount(account);
        return queryForObject(sql, rowMapper, params);
    }

    private Map<String, Object> createParamsForFindByAccount(final String account) {
        final Map<String, Object> params = new HashMap<>();
        params.put("account", account);
        return params;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = :id";
        return queryForObject(sql, rowMapper, Map.of("id", id));
    }
}
