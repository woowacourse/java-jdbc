package aop.service;

import aop.DataAccessException;
import aop.domain.User;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(PlatformTransactionManager transactionManager, UserService userService) {
        this.transactionManager = transactionManager;
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
    public void changePassword(long id, String newPassword, String createBy) {
        /* ===== 트랜잭션 영역 ===== */
        var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
        /* ===== 트랜잭션 영역 ===== */

        /* ===== 애플리케이션 영역 ===== */
            userService.changePassword(id, newPassword, createBy);
        /* ===== 애플리케이션 영역 ===== */

        /* ===== 트랜잭션 영역 ===== */
        } catch (RuntimeException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
        transactionManager.commit(transactionStatus);
        /* ===== 트랜잭션 영역 ===== */
    }
}
