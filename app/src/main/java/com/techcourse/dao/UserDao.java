package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(User user) throws SQLException {
        final String sql = createQueryForInsert();
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        try (connection; preparedStatement) {
            log.debug("query : {}", sql);
            setValuesForInsert(user, preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setValuesForInsert(User user, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
    }

    private String createQueryForInsert() {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        return sql;
    }

    public void update(User user) throws SQLException {
        final String sql = createQueryForUpdate();
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        try (connection; preparedStatement) {
            log.debug("query : {}", sql);
            setValuesForUpdate(user, preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setValuesForUpdate(User user, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getPassword());
        pstmt.setLong(2, user.getId());
    }

    private String createQueryForUpdate() {
        final String sql = "update users set password=? where id=?";
        return sql;
    }

    public List<User> findAll() throws SQLException {
        final String sql = "SELECT * FROM users";
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        try (connection; preparedStatement) {
            log.debug("query : {}", sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String account = resultSet.getString("account");
                String password = resultSet.getString("password");
                String email = resultSet.getString("email");
                users.add(new User(id, account, password, email));
                log.info("유저 findAll - id: {}, account: {}, password: {}, email: {}", id, account, password, email);
            }
            return users;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public User findById(Long id) throws SQLException {
        final String sql = "select id, account, password, email from users where id = ?";
        return (User) jdbcTemplate.queryForObject(sql,
                (resultSet, rowNum) -> new User(
                        resultSet.getLong("id"),
                        resultSet.getString("account"),
                        resultSet.getString("password"),
                        resultSet.getString("email")), id);
    }

    public User findByAccount(String account) throws SQLException {
        final String sql = "select id, account, password, email from users where account = ?";
        return (User) jdbcTemplate.queryForObject(sql,
                (resultSet, rowNum) -> new User(
                        resultSet.getLong("id"),
                        resultSet.getString("account"),
                        resultSet.getString("password"),
                        resultSet.getString("email")), account);
    }
}
