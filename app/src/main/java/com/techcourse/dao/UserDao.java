package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class UserDao {

  private static final Logger log = LoggerFactory.getLogger(UserDao.class);

  private final JdbcTemplate jdbcTemplate;

  public UserDao(final JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void insert(final User user) {
    final var sql = "insert into users (account, password, email) values (?, ?, ?)";

    jdbcTemplate.execute(sql, user.getAccount(), user.getPassword(), user.getEmail());
  }

  public void update(final User user) {
    final String sql = "update users set password = ? where id = ?";

    jdbcTemplate.execute(sql, user.getPassword(), user.getId());
  }

  public List<User> findAll() {
    final var sql = "select id, account, password, email from users";

    return jdbcTemplate.queryPlural(
        sql,
        resultSet -> new User(
            resultSet.getLong(1),
            resultSet.getString(2),
            resultSet.getString(3),
            resultSet.getString(4)
        )
    );
  }

  public User findById(final Long id) {
    final var sql = "select id, account, password, email from users where id = ?";

    return jdbcTemplate.query(
        sql,
        resultSet -> new User(
            resultSet.getLong(1),
            resultSet.getString(2),
            resultSet.getString(3),
            resultSet.getString(4)
        ),
        id
    );
  }

  public User findByAccount(final String account) {
    final var sql = "select id, account, password, email from users where account = ?";

    return jdbcTemplate.query(
        sql,
        resultSet -> new User(
            resultSet.getLong(1),
            resultSet.getString(2),
            resultSet.getString(3),
            resultSet.getString(4)
        ),
        account
    );
  }
}
