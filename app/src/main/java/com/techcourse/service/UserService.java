package com.techcourse.service;

import com.techcourse.domain.User;

public interface UserService {

    User findById(Long id);

    void insert(User user);

    void changePassword(Long id, String newPassword, String createBy);
}
