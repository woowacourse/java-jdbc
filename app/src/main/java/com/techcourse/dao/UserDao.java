package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
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
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        try (Connection connection = Objects.requireNonNull(dataSource).getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, user.getAccount());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());

            statement.executeUpdate();
            log.info("query : {}", sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("회원을 저장하는데 실패했습니다. :" + user);
        }
    }

    public void update(final User user) {
        final String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";

        try (Connection connection = Objects.requireNonNull(dataSource).getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, user.getAccount());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());
            statement.setLong(4, user.getId());

            statement.executeUpdate();
            log.info("query : {}", sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("회원 정보를 업데이트하는데 실패했습니다. :" + user);
        }
    }

    public List<User> findAll() {
        final String sql = "SELECT id, account, password, email FROM users";

        try (Connection connection = Objects.requireNonNull(dataSource).getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            ResultSet resultSet = statement.executeQuery();
            log.info("query : {}", sql);

            return mapToUsers(resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("회원 정보를 조회하는데 실패했습니다.");
        }
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        try (Connection connection = Objects.requireNonNull(dataSource).getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, id);

            ResultSet resultSet = statement.executeQuery();
            log.info("query : {}", sql);

            return mapToUser(resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("id가" + id + "인 회원 정보를 조회하는데 실패했습니다. :");
        }
    }

    public User findByAccount(final String account) {
        final String sql = "SELECT id, account, password, email FROM users WHERE account = ?";

        try (Connection connection = Objects.requireNonNull(dataSource).getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, account);

            ResultSet resultSet = statement.executeQuery();
            log.info("query : {}", sql);

            return mapToUser(resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("account가" + account + "인 회원 정보를 조회하는데 실패했습니다. :");
        }
    }

    private List<User> mapToUsers(ResultSet resultSet) throws SQLException {
        List<User> users = new ArrayList<>();
        while (resultSet.next()) {
            users.add(mapToUser(resultSet));
        }
        return users;
    }

    private User mapToUser(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return new User(
                    resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email")
            );
        }
        return null;
    }
}
