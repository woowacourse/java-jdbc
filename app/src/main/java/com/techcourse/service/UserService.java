package com.techcourse.service;

import com.techcourse.domain.User;

import java.util.Optional;

public interface UserService {

    Optional<User> findById(final long id);

    void insert(User user);

    void changePassword(final long id, final String newPassword, final String createBy);
}
