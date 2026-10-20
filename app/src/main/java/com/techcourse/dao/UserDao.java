package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final AbstractJdbcTemplate template = new AbstractJdbcTemplate(jdbcTemplate.getDataSource()) {
            @Override
            protected String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            protected void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
            }
        };
        template.update();
    }

    public void update(final User user) {
        final AbstractJdbcTemplate template = new AbstractJdbcTemplate(jdbcTemplate.getDataSource()) {
            @Override
            protected String createQuery() {
                return "update users set account = ?, password = ?, email = ? where id = ?";
            }

            @Override
            protected void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                pstmt.setLong(4, user.getId());
            }
        };
        final int updatedRows = template.update();
        if (updatedRows == 0) {
            throw new DataAccessException("수정할 사용자가 존재하지 않습니다. id = " + user.getId());
        }
    }

    public List<User> findAll() {
        final SelectJdbcTemplate<User> template = new SelectJdbcTemplate<>(jdbcTemplate.getDataSource()) {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users";
            }

            @Override
            protected void setValues(final PreparedStatement pstmt) {
            }

            @Override
            protected User mapRow(final ResultSet rs) throws SQLException {
                return mapUser(rs);
            }
        };
        return template.query();
    }

    public User findById(final Long id) {
        final SelectJdbcTemplate<User> template = new SelectJdbcTemplate<>(jdbcTemplate.getDataSource()) {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where id = ?";
            }

            @Override
            protected void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, id);
            }

            @Override
            protected User mapRow(final ResultSet rs) throws SQLException {
                return mapUser(rs);
            }
        };
        return template.queryForObject();
    }

    public User findByAccount(final String account) {
        final SelectJdbcTemplate<User> template = new SelectJdbcTemplate<>(jdbcTemplate.getDataSource()) {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where account = ?";
            }

            @Override
            protected void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, account);
            }

            @Override
            protected User mapRow(final ResultSet rs) throws SQLException {
                return mapUser(rs);
            }
        };
        return template.queryForObject();
    }

    private static User mapUser(final ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email"));
    }
}
