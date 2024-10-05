package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class UserDaoImpl implements UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDaoImpl(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        PreparedStatementSetter setter = pstmt -> {
            pstmt.setObject(1, user.getAccount());
            pstmt.setObject(2, user.getPassword());
            pstmt.setObject(3, user.getEmail());
        };
        jdbcTemplate.update(sql, setter);
    }

    @Override
    public void update(User user) {
        String sql = "update users set password = ? where id = ?";
        PreparedStatementSetter setter = pstmt -> {
            pstmt.setObject(1, user.getPassword());
            pstmt.setObject(2, user.getId());
        };
        jdbcTemplate.update(sql, setter);
    }

    @Override
    public List<User> findAll() {
        String sql = "select id, account, password, email from users";
        RowMapper<List<User>> rowMapper = this::createUsers;

        return jdbcTemplate.query(sql, rowMapper);
    }

    private List<User> createUsers(ResultSet rs) throws SQLException {
        List<User> users = new ArrayList<>();
        while (rs.next()) {
            users.add(createUser(rs));
        }
        return users;
    }

    @Override
    public Optional<User> findById(final Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        PreparedStatementSetter setter = pstmt -> pstmt.setLong(1, id);
        RowMapper<User> rowMapper = this::createUser;

        User user = jdbcTemplate.query(sql, setter, rowMapper);
        if (user == null) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    private User createUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
        );
    }

    @Override
    public Optional<User> findByAccount(final String account) {
        String sql = "select id, account, password, email from users where account = ?";
        PreparedStatementSetter setter = pstmt -> pstmt.setString(1, account);
        RowMapper<User> rowMapper = this::createUser;

        User user = jdbcTemplate.query(sql, setter, rowMapper);
        if (user == null) {
            return Optional.empty();
        }
        return Optional.of(user);
    }
}
