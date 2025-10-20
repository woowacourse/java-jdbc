package com.techcourse.service;

import com.interface21.transaction.Transactional;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    @Transactional
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    @Transactional
    public void save(final User user) {
        userService.save(user);
    }

    @Override
    @Transactional
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        userService.changePassword(id, newPassword, createdBy);
    }
}
