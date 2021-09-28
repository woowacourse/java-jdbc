package com.techcourse.repository;

import com.techcourse.domain.User;

import java.util.Optional;

public interface UserRepository {
    void save(User user);
    Optional<User> findByAccount(String account);
}
