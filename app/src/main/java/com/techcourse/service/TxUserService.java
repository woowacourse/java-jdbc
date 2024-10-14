package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.jdbc.manager.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.service.model.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);
    private static final String PASSWORD_ERROR_MESSAGE = "비밀번호를 수정하던 도중 에러가 발생했습니다";

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(UserService userService) {
        this.userService = userService;
        this.dataSource = DataSourceConfig.getInstance();
    }

    @Override
    public User findById(long id) {
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            return TransactionManager.start(connection,
                    () -> userService.findById(id),
                    dataSource
            );
        } catch (SQLException | DataAccessException e) {
            log.error(PASSWORD_ERROR_MESSAGE + " : {}", e.getMessage());
            throw new DataAccessException(PASSWORD_ERROR_MESSAGE, e);
        }
    }

    @Override
    public Optional<User> findByAccount(String account) {
        return userService.findByAccount(account);
    }

    @Override
    public void save(User user) {
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            TransactionManager.start(connection,
                    () -> userService.save(user),
                    dataSource
            );
        } catch (SQLException | DataAccessException e) {
            log.error(PASSWORD_ERROR_MESSAGE + " : {}", e.getMessage());
            throw new DataAccessException(PASSWORD_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        try {
            Connection connection = DataSourceUtils.getConnection(dataSource);
            TransactionManager.start(connection,
                    () -> userService.changePassword(id, newPassword, createdBy),
                    dataSource
            );
        } catch (DataAccessException e) {
            log.error(PASSWORD_ERROR_MESSAGE + " : {}", e.getMessage());
            throw new DataAccessException(PASSWORD_ERROR_MESSAGE, e);
        }
    }
}
