package com.techcourse.service;

import com.techcourse.domain.User;


public interface UserService {

    User findById(final long id);

    User findByAccount(String account);

    void save(final User user);

    void changePassword(final long id, final String newPassword, final String createdBy);

}
