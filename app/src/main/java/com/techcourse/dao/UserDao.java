package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.ParameterSource;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        ParameterSource parameterSource = new ParameterSource();
        parameterSource.addParam(user.getAccount());
        parameterSource.addParam(user.getPassword());
        parameterSource.addParam(user.getEmail());

        executeUpdate(sql, parameterSource);
    }

    public void update(final User user) {
        final var sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        ParameterSource parameterSource = new ParameterSource();
        parameterSource.addParam(user.getAccount());
        parameterSource.addParam(user.getPassword());
        parameterSource.addParam(user.getEmail());
        parameterSource.addParam(user.getId());

        executeUpdate(sql, parameterSource);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        ParameterSource parameterSource = new ParameterSource();

        return executeQuery(sql, parameterSource);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        ParameterSource parameterSource = new ParameterSource();
        parameterSource.addParam(id);

        return executeQuery(sql, parameterSource).get(0); //  TODO IndexOutOfBoundsException 대응되도록 수정 필요
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        ParameterSource parameterSource = new ParameterSource();
        parameterSource.addParam(account);

        return executeQuery(sql, parameterSource).get(0);
    }

    private List<User> executeQuery(String sql, ParameterSource parameterSource) {
        try (final var conn = getConnection();
             final var pstmt = conn.prepareStatement(sql)) {
            setParams(pstmt, parameterSource);
            log.debug("query : {}", sql);
            return query(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void executeUpdate(String sql, ParameterSource parameterSource) {
        try (final var conn = getConnection();
             final var pstmt = conn.prepareStatement(sql)) {
            setParams(pstmt, parameterSource);
            log.debug("query : {}", sql);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setParams(PreparedStatement pstmt, ParameterSource parameterSource) throws SQLException {
        for (var index = 0; index < parameterSource.getParamCount(); index++) {
            pstmt.setObject(index + 1, parameterSource.getParam(index));
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (NullPointerException e) {
            throw new IllegalStateException("DataSource가 설정되지 않았습니다.");
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private List<User> query(PreparedStatement pstmt) throws SQLException {
        try (final var rs = pstmt.executeQuery()) {
            List<User> users = new ArrayList<>();
            if (rs.next()) {
                users.add(rowMapper.mapRow(rs));
            }
            return users;
        }
    }

    private final RowMapper<User> rowMapper = (resultSet) ->
            new User(resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email"));
}
