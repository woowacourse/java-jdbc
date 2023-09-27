package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class UserDao {

  private static final Logger log = LoggerFactory.getLogger(UserDao.class);

  private final DataSource dataSource;
  private final JdbcTemplate jdbcTemplate;

  public UserDao(final DataSource dataSource) {
    this.dataSource = dataSource;
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  public UserDao(final JdbcTemplate jdbcTemplate) {
    this.dataSource = null;
    this.jdbcTemplate = jdbcTemplate;
  }

  public void insert(final User user) {
    final var sql = "insert into users (account, password, email) values (?, ?, ?)";

    Connection conn = null;
    PreparedStatement pstmt = null;
    try {
      conn = dataSource.getConnection();
      pstmt = conn.prepareStatement(sql);

      log.debug("query : {}", sql);

      pstmt.setString(1, user.getAccount());
      pstmt.setString(2, user.getPassword());
      pstmt.setString(3, user.getEmail());
      pstmt.executeUpdate();
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    } finally {
      try {
        if (pstmt != null) {
          pstmt.close();
        }
      } catch (SQLException ignored) {
      }

      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ignored) {
      }
    }
  }

  public void update(final User user) {
    final String sql = "update users set password = ? where id = ?";

    Connection conn = null;
    PreparedStatement pstmt = null;
    try {
      conn = dataSource.getConnection();
      pstmt = conn.prepareStatement(sql);

      log.debug("query : {}", sql);

      pstmt.setString(1, user.getPassword());
      pstmt.setLong(2, user.getId());
      pstmt.executeUpdate();
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    } finally {
      try {
        if (pstmt != null) {
          pstmt.close();
        }
      } catch (SQLException ignored) {
      }

      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ignored) {
      }
    }
  }

  public List<User> findAll() {
    final var sql = "select id, account, password, email from users";

    final List<Map<Integer, Object>> results = jdbcTemplate.queryPlural(sql);

    List<User> users = new ArrayList<>();

    for (int i = 0; i < results.size(); i++) {
      final Map<Integer, Object> result = results.get(i);

      users.add(new User(
          (Long) result.get(1),
          (String) result.get(2),
          (String) result.get(3),
          (String) result.get(4)
      ));
    }

    return users;
  }

  public User findById(final Long id) {
    final var sql = "select id, account, password, email from users where id = ?";

    final Map<Integer, Object> result = jdbcTemplate.query(sql, id);

    return new User(
        (Long) result.get(1),
        (String) result.get(2),
        (String) result.get(3),
        (String) result.get(4)
    );
  }

  public User findByAccount(final String account) {
    final var sql = "select id, account, password, email from users where account = ?";

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      conn = dataSource.getConnection();
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, account);
      rs = pstmt.executeQuery();

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
      } catch (SQLException ignored) {
      }

      try {
        if (pstmt != null) {
          pstmt.close();
        }
      } catch (SQLException ignored) {
      }

      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ignored) {
      }
    }
  }
}
