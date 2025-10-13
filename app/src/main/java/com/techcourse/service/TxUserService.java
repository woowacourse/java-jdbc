package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.PlatformTransactionManager;
import com.techcourse.domain.User;

import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(UserService userService,DataSource dataSource) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void save(User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        try{
            PlatformTransactionManager.begin(dataSource);
            userService.changePassword(id,newPassword,createBy);
            PlatformTransactionManager.commit(dataSource);
        } catch (DataAccessException e) {
            PlatformTransactionManager.rollback(dataSource);
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            PlatformTransactionManager.end(dataSource);
        }
    }
}
