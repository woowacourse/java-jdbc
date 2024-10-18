package com.techcourse.service;

import com.techcourse.domain.User;


public interface UserService {

    User findById(long id);

    User findByAccount(String account);

    void save(User user);

    void changePassword(long id, String newPassword, String createdBy);

}
