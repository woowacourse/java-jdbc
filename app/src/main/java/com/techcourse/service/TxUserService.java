package com.techcourse.service;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        Consumer<Connection> consumer = (connection) -> userService.changePassword(id, newPassword, createBy);
        TxManager.run(consumer);
    }

    @Override
    public void save(User user) {
        Consumer<Connection> consumer = (connection) -> userService.save(user);
        TxManager.run(consumer);
    }

    @Override
    public User findById(long id) {
        Function<Connection, User> consumer = (connection) -> userService.findById(id);
        return TxManager.run(consumer);
    }
}
