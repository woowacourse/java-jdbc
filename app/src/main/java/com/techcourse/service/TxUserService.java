package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.TransactionTemplate;

public class TxUserService implements UserService {

    private final TransactionTemplate template;
    private final UserService userService;

    public TxUserService(final TransactionTemplate template, final UserService userService) {
        this.template = template;
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return template.executeWithResult(() -> userService.findById(id));
    }

    @Override
    public void insert(User user) {
        template.execute(() -> userService.insert(user));
    }

    // override 대상인 메서드는 userService의 메서드를 그대로 위임(delegate)한다.
    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        template.execute(() -> userService.changePassword(id, newPassword, createBy));
    }
}
