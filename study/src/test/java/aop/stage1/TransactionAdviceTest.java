package aop.stage1;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

class TransactionAdviceTest {

    private PlatformTransactionManager transactionManager;
    private TransactionStatus status;
    private MethodInvocation invocation;
    private TransactionAdvice transactionAdvice;

    @BeforeEach
    void setUp() {
        transactionManager = mock(PlatformTransactionManager.class);
        status = mock(TransactionStatus.class);
        invocation = mock(MethodInvocation.class);

        when(transactionManager.getTransaction(any())).thenReturn(status);

        transactionAdvice = new TransactionAdvice(transactionManager);
    }

    @DisplayName("대상 메서드에서 Error가 발생하면 롤백한다")
    @Test
    void rollbackOnError() throws Throwable {
        AssertionError error = new AssertionError();
        when(invocation.proceed()).thenThrow(error);

        assertThatThrownBy(() -> transactionAdvice.invoke(invocation))
                .isSameAs(error);

        verify(transactionManager).rollback(status);
        verify(transactionManager, never()).commit(status);
    }

    @DisplayName("대상 메서드에서 checked 예외가 발생하면 커밋한다")
    @Test
    void commitOnCheckedException() throws Throwable {
        Exception exception = new Exception();
        when(invocation.proceed()).thenThrow(exception);

        assertThatThrownBy(() -> transactionAdvice.invoke(invocation))
                .isSameAs(exception);

        verify(transactionManager).commit(status);
        verify(transactionManager, never()).rollback(status);
    }
}
