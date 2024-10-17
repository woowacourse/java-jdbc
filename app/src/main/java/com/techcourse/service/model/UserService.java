package com.techcourse.service.model;

import com.techcourse.domain.User;

import java.util.Optional;

public interface UserService {

    User findById(long id);

    void save(User user);

    void changePassword(long id, String newPassword, String createdBy);

    Optional<User> findByAccount(String account);
}

