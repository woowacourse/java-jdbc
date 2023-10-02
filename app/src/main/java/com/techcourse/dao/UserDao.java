package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserDao {

  private static final Logger log = LoggerFactory.getLogger(UserDao.class);
  private static final RowMapper<User> USER_ROW_MAPPER = resultSet ->
      new User(
          resultSet.getLong(1),
          resultSet.getString(2),
          resultSet.getString(3),
          resultSet.getString(4)
      );

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
        USER_ROW_MAPPER
    );
  }

  public User findById(final Long id) {
    final var sql = "select id, account, password, email from users where id = ?";

    return jdbcTemplate.query(
        sql,
        USER_ROW_MAPPER,
        id
    ).orElseThrow(() -> new IllegalArgumentException("해당 값이 존재하지 않습니다."));
  }

  public User findByAccount(final String account) {
    final var sql = "select id, account, password, email from users where account = ?";

    return jdbcTemplate.query(
        sql,
        USER_ROW_MAPPER,
        account
    ).orElseThrow(() -> new IllegalArgumentException("해당 값이 존재하지 않습니다."));
  }
}
