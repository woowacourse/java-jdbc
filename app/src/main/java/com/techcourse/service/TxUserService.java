package com.techcourse.service;

import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void save(User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        // 트랜잭션 처리 영역

        userService.changePassword(id, newPassword, createdBy);

        // 트랜잭션 처리 영역
    }
}
