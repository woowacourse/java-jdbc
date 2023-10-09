package aop.stage2;

import aop.domain.User;
import aop.repository.UserHistoryDao;

public interface UserService {

    User findById(long id);

    void insert(User user);

    void changePassword(long id, String newPassword, String createBy);

    void setUserHistoryDao(UserHistoryDao userHistoryDao);
}
