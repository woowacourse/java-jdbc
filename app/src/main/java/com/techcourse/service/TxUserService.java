package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.support.Transactional;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return Transactional.serviceForObject(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        Transactional.serviceForUpdate(() -> userService.insert(user));
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        Transactional.serviceForUpdate(() -> userService.changePassword(id, newPassword, createBy));
    }
}
