package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;

public class UserDao {

    private static final String INSERT_SQL = "insert into users (account, password, email) values (?, ?, ?)";
    private static final String UPDATE_SQL = "update users set account = ?, password = ?, email = ? where id = ?";
    private static final String FIND_ALL_SQL = "select id, account, password, email from users";
    private static final String FIND_BY_ID_SQL = "select id, account, password, email from users where id = ?";
    private static final String FIND_BY_ACCOUNT_SQL = "select id, account, password, email from users where account = ?";

    private final RowMapper<User> rowMapper = ((rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    ));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        jdbcTemplate.update(INSERT_SQL, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        jdbcTemplate.update(UPDATE_SQL, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, rowMapper);
    }

    public User findById(final Long id) {
        return jdbcTemplate.queryForObject(FIND_BY_ID_SQL, rowMapper, id);
    }

    public User findByAccount(final String account) {
        return jdbcTemplate.queryForObject(FIND_BY_ACCOUNT_SQL, rowMapper, account);
    }
}
