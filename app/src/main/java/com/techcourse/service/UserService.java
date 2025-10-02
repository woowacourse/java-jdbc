package com.techcourse.service;

import com.interface21.jdbc.datasource.LocalTransactionManager;
import com.interface21.jdbc.exception.JdbcFailException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

import java.sql.SQLException;
import javax.sql.DataSource;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    //이제 jdbc 템플릿이 아닌 외부에서 트랜잭션을 제어할 필요성이 생겼다
    //이를 깔끔하게 관리하기 위해서는 @Transactional과 같이 외부에서 이를 관리해줄 필요성또한 생긴것이다
    //우선은 가장 간단한 방법으로 구현했다.
    public void changePassword(final long id, final String newPassword, final String createBy) {
        try{
            LocalTransactionManager.begin(dataSource);

            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));

            LocalTransactionManager.commit();
        } catch (Exception e) {
            LocalTransactionManager.rollback();
        }finally {
            LocalTransactionManager.end();
        }
    }
}
