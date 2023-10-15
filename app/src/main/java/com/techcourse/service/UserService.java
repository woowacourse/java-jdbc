package com.techcourse.service;

import com.techcourse.domain.User;

import java.sql.SQLException;

public interface UserService {

    User findById(final long id) throws SQLException;

    void insert(final User user) throws SQLException;

    void changePassword(final long id, final String newPassword, final String createBy) throws SQLException;
}
