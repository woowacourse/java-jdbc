package nextstep.jdbc.support;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nextstep.jdbc.exception.DataAccessException;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

class TransactionServiceTest {

    @Test
    void 동작이_예외를_반환하지_않으면_commit_한다() {
        // given
        final PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        final TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any(DefaultTransactionDefinition.class)))
                .thenReturn(transactionStatus);

        final TransactionService transactionService = new MockTransactionService(transactionManager);

        // when
        transactionService.runWithTransaction(mock(Runnable.class));

        // then
        assertAll(
                () -> verify(transactionManager).commit(transactionStatus),
                () -> verify(transactionManager, never()).rollback(transactionStatus)
        );
    }

    @Test
    void 동작이_예외를_반환하면_rollback_한다() {
        // given
        final PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        final TransactionStatus transactionStatus = mock(TransactionStatus.class);
        final Runnable runnable = mock(Runnable.class);
        when(transactionManager.getTransaction(any(DefaultTransactionDefinition.class)))
                .thenReturn(transactionStatus);
        doThrow(new DataAccessException())
                .when(runnable)
                .run();

        final TransactionService transactionService = new MockTransactionService(transactionManager);

        // when, then
        assertAll(
                () -> assertThatThrownBy(() -> transactionService.runWithTransaction(runnable))
                        .isInstanceOf(DataAccessException.class),
                () -> verify(transactionManager, never()).commit(transactionStatus),
                () -> verify(transactionManager).rollback(transactionStatus)
        );
    }

    class MockTransactionService extends TransactionService {

        protected MockTransactionService(PlatformTransactionManager transactionManager) {
            super(transactionManager);
        }
    }
}
