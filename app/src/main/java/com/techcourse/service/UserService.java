package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);


    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(IllegalArgumentException::new);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try(Connection connection = dataSource.getConnection()) { // 외부 try - catch: connection 연결 과정 에러 관리
            connection.setAutoCommit(false);

            try { // 내부 try - catch 트랜잭션 관리 보장.
                final var user = findById(id);
                user.changePassword(newPassword);
                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));

                connection.commit();
            } catch (Exception exceptionWhenCommit) {
                log.error("커밋 실패, 롤백 시도", exceptionWhenCommit);

                try { // 롤백 try - catch
                    connection.rollback();
                } catch (SQLException exceptionWhenRollback) {
                    log.error("트랜잭션 롤벡 실패", exceptionWhenRollback);
                }

                // 롤백 성공, 실패와 상관 없이 원본 예외를 다시 던진다.
                throw new DataAccessException("커밋 중 에러가 발생했습니다. 롤백합니다.", exceptionWhenCommit);
            }

        } catch (SQLException exceptionWhenConnect) {
            log.error("DB 커넥션 획득 실패.", exceptionWhenConnect);
            throw new DataAccessException("커넥션을 얻지 못했습니다.", exceptionWhenConnect);
        }
    }
}
