package com.techcourse.service;

import com.interface21.jdbc.datasource.Connection;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.ConnectionSynchronizeManager;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        Connection conn = ConnectionSynchronizeManager.getConnection();
        try {
            return userService.findById(id);
        } finally {
            conn.close();
        }
    }

    @Override
    public void insert(User user) {
        Connection conn = ConnectionSynchronizeManager.getConnection();
        conn.setAutoCommit(false);
        try {
            userService.insert(user);
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        Connection conn = ConnectionSynchronizeManager.getConnection();
        conn.setAutoCommit(false);
        try {
            userService.changePassword(id, newPassword, createBy);
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }
}
