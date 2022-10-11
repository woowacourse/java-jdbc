package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.PlatformTransactionManager;

public class TxUserService implements UserService{

    @Override
    public User findById(long id) {
        return null;
    }

    @Override
    public void insert(User user) {

    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {

    }

}
