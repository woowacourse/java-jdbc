package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;

import javax.sql.DataSource;
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
    private final InsertJdbcTemplate insertJdbcTemplate;
    private final UpdateJdbcTemplate updateJdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertJdbcTemplate = new InsertJdbcTemplate(jdbcTemplate.getDataSource());
        this.updateJdbcTemplate = new UpdateJdbcTemplate(jdbcTemplate.getDataSource());
    }

    public void insert(final User user) {
        insertJdbcTemplate.insert(user, this);
    }

    public String createQueryForInsert() {
        return "insert into users (account, password, email) values (?, ?, ?)";
    }

    public void setValuesForInsert(final User user, final PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
    }

    public void update(final User user) {
        final int updatedRows = updateJdbcTemplate.update(user, this);
        if (updatedRows == 0) {
            throw new DataAccessException("수정할 사용자가 존재하지 않습니다. id = " + user.getId());
        }
    }

    public String createQueryForUpdate() {
        return "update users set account = ?, password = ?, email = ? where id = ?";
    }

    public void setValuesForUpdate(final User user, final PreparedStatement pstmt) throws SQLException {
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
