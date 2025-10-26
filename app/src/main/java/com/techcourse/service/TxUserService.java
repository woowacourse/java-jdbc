package com.techcourse.service;

import static com.techcourse.support.transaction.TransactionInterceptor.applyTransaction;

import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return applyTransaction(() -> userService.findById(id));
    }

    @Override
    public void save(User user) {
        applyTransaction(() -> {
            userService.save(user);
            return null;
        });
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        applyTransaction(() -> {
            userService.changePassword(id, newPassword, createdBy);
            return null;
        });
    }
}
