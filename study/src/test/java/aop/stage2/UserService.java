package aop.stage2;

import aop.Transactional;
import aop.domain.User;
import aop.domain.UserHistory;
import aop.repository.UserDao;
import aop.repository.UserHistoryDao;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserDao userDao;
    private UserHistoryDao userHistoryDao;

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    @Transactional
    public User findById(long id) {
        return userDao.findById(id);
    }

    @Transactional
    public void insert(User user) {
        userDao.insert(user);
    }

    @Transactional
    public void changePassword(long id, String newPassword, String createBy) {
        User user = findById(id);
        user.changePassword(newPassword);
        userDao.update(user);
        userHistoryDao.log(new UserHistory(user, createBy));
    }

    public void setUserHistoryDao(UserHistoryDao userHistoryDao) {
        this.userHistoryDao = userHistoryDao;
    }
}
