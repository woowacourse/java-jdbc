package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.TransactionManager;

public class TxUserService implements UserService {

  private final UserService userService;

  public TxUserService(final UserService userService) {
    this.userService = userService;
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
      TransactionManager.commit();
    } catch (Exception e) {
      TransactionManager.rollback();
      throw e;
    } finally {
      TransactionManager.release();
    }
  }
}
