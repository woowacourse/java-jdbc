package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.exception.TechCourseApplicationException;
import java.sql.Connection;
import java.sql.SQLException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TxUserService implements UserService {

    private final UserService userService;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public TxUserService(UserService userService, UserDao userDao, UserHistoryDao userHistoryDao) {
        this.userService = userService;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        try (connection) {
            connection.setAutoCommit(false);
            userService.changePassword(id, newPassword, createdBy);
            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new TechCourseApplicationException("데이터를 롤백하는 것에 실패했습니다", ex);
            }
            throw new TechCourseApplicationException("비밀번호를 변경하는 것에 실패했습니다", e);
        }
    }
}
