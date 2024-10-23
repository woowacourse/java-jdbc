package com.techcourse.service;

import com.techcourse.domain.User;

public interface UserService {

    User findById(Long id);

    void save(User user);

    void changePassword(Long userId, String newPassword, String createdBy);
}
