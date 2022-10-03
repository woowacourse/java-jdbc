package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            log.info("query : {}", sql);

            preparedStatement.setString(1, user.getAccount());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public void update(final User user) {
        String sql = "update users set account=?, password=?, email=? where id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.info("query : {}", sql);
            preparedStatement.setString(1, user.getAccount());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setLong(4, user.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "select id, account, password, email from users";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            log.info("query : {}", sql);

            while (resultSet.next()) {
                User user = new User(
                        resultSet.getLong("id"),
                        resultSet.getString("account"),
                        resultSet.getString("password"),
                        resultSet.getString("email")
                );
                users.add(user);
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return users;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)
             ) {
            log.info("query : {}", sql);
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new User(
                        resultSet.getLong("id"),
                        resultSet.getString("account"),
                        resultSet.getString("password"),
                        resultSet.getString("email")
                );
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        throw new IllegalArgumentException("id에 해당하는 유저가 없습니다.");
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            log.info("query : {}", sql);
            preparedStatement.setString(1, account);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new User(
                        resultSet.getLong("id"),
                        resultSet.getString("account"),
                        resultSet.getString("password"),
                        resultSet.getString("email")
                );
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        throw new IllegalArgumentException("id에 해당하는 유저가 없습니다.");
    }

    public void deleteAll() {
        String sql = "truncate table users restart identity";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            log.info("query : {}", sql);
            statement.execute(sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }
}
