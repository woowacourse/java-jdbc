package com.techcourse.dao;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
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

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            conn = dataSource.getConnection();
            preparedStatement = conn.prepareStatement(sql);

            log.debug("query : {}", sql);

            preparedStatement.setString(1, user.getAccount());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {}
        }
    }

    public void update(final User user) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            final String sql = "update users set account=?, password=?, email=? where id=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getAccount());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setLong(4, user.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {}
        }
    }

    public List<User> findAll() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            final String sql = "select * from users";
            preparedStatement = connection.prepareStatement(sql);
            final ResultSet resultSet = preparedStatement.executeQuery();

            final List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                final Long id = resultSet.getLong("id");
                final String account = resultSet.getString("account");
                final String password = resultSet.getString("password");
                final String email = resultSet.getString("email");
                users.add(new User(id, account, password, email));
            }
            return users;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {}
        }
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        Connection conn = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setLong(1, id);
            rs = preparedStatement.executeQuery();

            log.debug("query : {}", sql);

            if (rs.next()) {
                return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {}
        }
    }

    public User findByAccount(final String account) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            final String sql = "select * from users where account = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, account);
            final ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                final Long id = resultSet.getLong("id");
                final String password = resultSet.getString("password");
                final String email = resultSet.getString("email");
                return new User(id, account, password, email);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {}
        }
        return null;
    }

}
