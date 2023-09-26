package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;


public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private static final RowMapper<User> USER_ROW_MAPPER = (resultSet, rowNum) ->
            new User(
                    resultSet.getLong(1),
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getString(4)
            );

    private final DataSource dataSource;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        update(sql, user.getAccount(), user.getPassword(), user.getEmail());

    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? ";

        update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    private void update(final String sql, final Object... parameters) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            for (int i = 1; i <= parameters.length; i++) {
                preparedStatement.setObject(i, parameters[i - 1]);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users ";

        return query(sql, USER_ROW_MAPPER);
    }


    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()
        ) {

            log.debug("query : {}", sql);

            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(
                        rowMapper.mapRow(resultSet, resultSet.getRow())
                );
            }

            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return queryForObject(sql, USER_ROW_MAPPER, id)
                .orElseThrow(DataAccessException::new);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return queryForObject(sql, USER_ROW_MAPPER, account)
                .orElseThrow(DataAccessException::new);
    }


    private <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object id) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setObject(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            log.debug("query : {}", sql);

            if (resultSet.next()) {
                T result = rowMapper.mapRow(resultSet, resultSet.getRow());

                resultSet.close();
                return Optional.ofNullable(result);
            }
            resultSet.close();
            return Optional.empty();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
