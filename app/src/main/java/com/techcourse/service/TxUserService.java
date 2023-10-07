package com.techcourse.service;

import com.techcourse.domain.User;
import javax.sql.DataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
    TransactionSynchronizationManager.start();

    try {
      userService.changePassword(id, newPassword, createBy);
      TransactionSynchronizationManager.commit(dataSource);
    } catch (Exception e) {
      TransactionSynchronizationManager.rollback(dataSource);
      throw e;
    } finally {
      TransactionSynchronizationManager.release(dataSource);
    }
  }
}
