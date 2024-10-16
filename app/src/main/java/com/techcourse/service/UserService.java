package com.techcourse.service;

import com.techcourse.domain.User;

import java.util.Optional;

public interface UserService {

    Optional<User> findById(final long id);

    Optional<User> findByAccount(final String account);

    void save(final User user);

    void changePassword(final long id, final String newPassword, final String createdBy);
}
