package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(createQueryForInsert())) {
            setValuesForInsert(user, pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private String createQueryForInsert() {
        return "insert into users (account, password, email) values (?, ?, ?)";
    }

    private void setValuesForInsert(final User user, final PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
    }

    public void update(final User user) {
        final int updatedRows;
        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(createQueryForUpdate())) {
            setValuesForUpdate(user, pstmt);
            updatedRows = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
        if (updatedRows == 0) {
            throw new DataAccessException("수정할 사용자가 존재하지 않습니다. id = " + user.getId());
        }
    }

    private String createQueryForUpdate() {
        return "update users set account = ?, password = ?, email = ? where id = ?";
    }

    private void setValuesForUpdate(final User user, final PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
        pstmt.setLong(4, user.getId());
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
    }
}
