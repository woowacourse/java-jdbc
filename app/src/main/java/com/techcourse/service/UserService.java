package com.techcourse.service;

import com.techcourse.domain.User;

import java.sql.SQLException;

public interface UserService {

    User findById(long id) throws SQLException;

    void insert(User user) throws SQLException;

    void changePassword(long id, String newPassword, String createBy) throws SQLException;
}
