package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.SQLException;

public class TransactionUserService implements UserService {

    private AppUserService appUserService;
    private DataSource dataSource;


    public TransactionUserService(final AppUserService appUserService, final DataSource dataSource) {
        this.appUserService = appUserService;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(final long id) {
        return appUserService.findById(id);
    }

    @Override
    public void insert(final User user) {
        appUserService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        Transaction transaction = Transaction.start(dataSource);
        try {
            appUserService.changePassword(id, newPassword, createBy);
            transaction.commit();
        } catch (SQLException | RuntimeException e) {
            transaction.rollback();
            throw new DataAccessException(e);
        } finally {
            transaction.close();
        }
    }
}
