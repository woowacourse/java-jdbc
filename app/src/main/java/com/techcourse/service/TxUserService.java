package com.techcourse.service;

import com.techcourse.domain.User;
import javax.sql.DataSource;
import org.springframework.jdbc.core.TransactionManager;

public class TxUserService implements UserService {

  private final UserService userService;
  private final DataSource dataSource;

  public TxUserService(final UserService userService, final DataSource dataSource) {
    this.userService = userService;
    this.dataSource = dataSource;
  }

  @Override
  public User findById(final long id) {
    return userService.findById(id);
  }

  @Override
  public void insert(final User user) {
    userService.insert(user);
  }

  @Override
  public void changePassword(final long id, final String newPassword, final String createBy) {
    TransactionManager.start();

    try {
      userService.changePassword(id, newPassword, createBy);
      TransactionManager.commit(dataSource);
    } catch (Exception e) {
      TransactionManager.rollback(dataSource);
      throw e;
    } finally {
      TransactionManager.release(dataSource);
    }
  }
}
