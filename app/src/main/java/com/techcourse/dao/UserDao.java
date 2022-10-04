package com.techcourse.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.techcourse.domain.User;

import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.PreparedStatementSetter;
import nextstep.jdbc.RowMapper;

public class UserDao {

    private static final RowMapper<User> ROW_MAPPER = (rs, rowNum) -> new User(rs.getLong(1),
        rs.getString(2),
        rs.getString(3),
        rs.getString(4));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = createQueryForInsert();

        final PreparedStatementSetter pss = pstmt -> setPreparedStatement(pstmt, user.getAccount(), user.getPassword(),
            user.getEmail());
        jdbcTemplate.update(sql, pss);
    }

    private String createQueryForInsert() {
        return "insert into users (account, password, email) values (?, ?, ?)";
    }

    public void update(final User user) {
        final var sql = createQueryForUpdate();

        final PreparedStatementSetter pss = pstmt -> setPreparedStatement(pstmt, user.getAccount(), user.getPassword(),
            user.getEmail(), user.getId());
        jdbcTemplate.update(sql, pss);
    }

    private String createQueryForUpdate() {
        return "update users set account = ?, password = ?, email = ? where id = ?";
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        final PreparedStatementSetter pss = pstmt -> setPreparedStatement(pstmt, id);
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, pss);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        final PreparedStatementSetter pss = pstmt -> setPreparedStatement(pstmt, account);
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, pss);
    }

    private void setPreparedStatement(PreparedStatement pstmt, Object... args) throws SQLException {

        int index = 0;
        for (Object arg : args) {
            index++;
            switch (arg.getClass().getName()) {
                case "String":
                    pstmt.setString(index, (String)arg);
                    break;
                case "Long":
                    pstmt.setLong(index, (Long)arg);
                    break;
                default:
                    pstmt.setObject(index, arg);
            }
        }
    }
}
