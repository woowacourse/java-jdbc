package com.techcourse.service;

import com.techcourse.domain.User;

public interface UserService {
    User findById(long id);
    void save(User user);

    default void insert(final User user) {
        save(user);
    }

    void changePassword(long id, String newPassword, String createBy);
}
