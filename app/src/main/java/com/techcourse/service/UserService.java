package com.techcourse.service;

import com.techcourse.domain.User;
import java.util.Optional;

public interface UserService {

    Optional<User> findById(long id);

    void insert(User user);

    void changePassword(long id, String newPassword, String createBy);
}
