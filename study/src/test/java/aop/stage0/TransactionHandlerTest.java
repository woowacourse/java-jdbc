package aop.stage0;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import aop.DataAccessException;
import aop.domain.User;
import aop.repository.UserDao;
import aop.repository.UserHistoryDao;
import aop.service.AppUserService;
import aop.service.UserService;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

class TransactionHandlerTest {

    private final PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);

    @Test
    void implementationAnnotationStartsTransaction() {
        final var userDao = mock(UserDao.class);
        final var user = new User("gugu", "password", "gugu@example.com");
        when(userDao.findById(1L)).thenReturn(user);
        final var status = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(status);
        final var target = new AppUserService(userDao, mock(UserHistoryDao.class));

        final var actual = proxy(target).findById(1L);

        assertThat(actual).isSameAs(user);
        verify(transactionManager).getTransaction(any());
        verify(transactionManager).commit(status);
    }

    @Test
    void unannotatedMethodDelegatesWithoutTransaction() {
        final var user = new User("gugu", "password", "gugu@example.com");
        final var target = new AppUserService(null, null) {
            @Override
            public User findById(final long id) {
                return user;
            }
        };

        assertThat(proxy(target).findById(1L)).isSameAs(user);

        verifyNoInteractions(transactionManager);
    }

    @Test
    void unannotatedMethodPreservesBusinessFailure() {
        final var failure = new DataAccessException("query failed");
        final var target = new AppUserService(null, null) {
            @Override
            public User findById(final long id) {
                throw failure;
            }
        };

        final var actual = assertThrows(DataAccessException.class, () -> proxy(target).findById(1L));

        assertThat(actual).isSameAs(failure);
        verifyNoInteractions(transactionManager);
    }

    private UserService proxy(final AppUserService target) {
        return (UserService) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{UserService.class},
                new TransactionHandler(transactionManager, target)
        );
    }
}
