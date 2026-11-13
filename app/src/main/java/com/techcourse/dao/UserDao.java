package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final String UPDATE_SQL = "update users set account = ?, password = ?, email = ? where id = ?";
    private static final String FIND_BY_ID_SQL = "select id, account, password, email from users where id = ?";

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(
                sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail()
        );
    }

    public void update(final User user) {
        jdbcTemplate.update(UPDATE_SQL, createUpdateSetter(user));
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, this::mapUser);
    }

    public User findById(final Long id) {
        return jdbcTemplate.queryForObject(FIND_BY_ID_SQL, this::mapUser, id);
    }

    public User findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, this::mapUser, account);
    }

    private PreparedStatementSetter createUpdateSetter(final User user) {
        return pstmt -> {
            pstmt.setObject(1, user.getAccount());
            pstmt.setObject(2, user.getPassword());
            pstmt.setObject(3, user.getEmail());
            pstmt.setObject(4, user.getId());
        };
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
        );
    }
}
