package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    final List<Map<Integer, Object>> results = jdbcTemplate.queryPlural(sql);

    List<User> users = new ArrayList<>();

    for (int i = 0; i < results.size(); i++) {
      final Map<Integer, Object> result = results.get(i);

      users.add(createUserFrom(result));
    }

    return users;
  }

  public User findById(final Long id) {
    final var sql = "select id, account, password, email from users where id = ?";

    final Map<Integer, Object> result = jdbcTemplate.query(sql, id);

    return createUserFrom(result);
  }

  public User findByAccount(final String account) {
    final var sql = "select id, account, password, email from users where account = ?";

    final Map<Integer, Object> result = jdbcTemplate.query(sql, account);

    return createUserFrom(result);
  }

  private User createUserFrom(final Map<Integer, Object> result) {
    return new User(
        (Long) result.get(1),
        (String) result.get(2),
        (String) result.get(3),
        (String) result.get(4)
    );
  }
}
