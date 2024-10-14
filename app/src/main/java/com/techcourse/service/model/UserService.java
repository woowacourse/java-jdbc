package com.techcourse.service.model;

import com.techcourse.domain.User;

import java.util.Optional;

public interface UserService {

    User findById(final long id);

    void save(final User user);

    void changePassword(final long id, final String newPassword, final String createdBy);

    Optional<User> findByAccount(String account);
}

