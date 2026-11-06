package aop.stage0;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import aop.domain.User;
import aop.service.UserService;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;

class TransactionHandlerTest {

    @DisplayName("@Transactional이 없는 메서드는 트랜잭션 없이 실행한다")
    @Test
    void invokeWithoutTransaction() {
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        UserService target = new UserService() {
            @Override
            public User findById(long id) {
                return null;
            }

            @Override
            public void insert(User user) {
            }

            @Override
            public void changePassword(long id, String newPassword, String createBy) {
            }
        };
        UserService userService = (UserService) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{UserService.class},
                new TransactionHandler(transactionManager, target)
        );

        userService.findById(1L);

        verify(transactionManager, never()).getTransaction(any());
    }
}
