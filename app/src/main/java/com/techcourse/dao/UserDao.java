package com.techcourse.dao;

import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.interface21.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = new RowMapper<>() {
        @Override
        public User mapRow(final ResultSet rs) throws SQLException {
            return new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
            );
        }
    };

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
            }
        });
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                pstmt.setLong(4, user.getId());
            }
        });
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER, new PreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement pstmt) {
            }
        });
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, new PreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, id);
            }
        });
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, new PreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, account);
            }
        });
    }
}
