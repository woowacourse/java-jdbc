package com.techcourse.service;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.exception.TechCourseApplicationException;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
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
