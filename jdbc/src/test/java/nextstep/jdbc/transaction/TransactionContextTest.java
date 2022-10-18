package nextstep.jdbc.transaction;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import nextstep.jdbc.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;

class TransactionContextTest {

    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        transactionManager = mock(PlatformTransactionManager.class);
    }

    @Test
    @DisplayName("트랜잭션으로 값을 반환하는 로직이 정상적으로 실행된 경우 커밋을 호출하는 지를 테스트한다.")
    void runFunction() {
        // given
        final TransactionContext transactionContext = new TransactionContext(transactionManager);

        // when
        transactionContext.runFunction(() -> "Hello");

        // then
        verify(transactionManager).commit(any());
    }

    @Test
    @DisplayName("트랜잭션으로 반환값이 없는 로직이 정상적으로 실행된 경우 커밋을 호출하는 지를 테스트한다.")
    void runConsumer() {
        // given
        final TransactionContext transactionContext = new TransactionContext(transactionManager);

        // when
        transactionContext.runConsumer(() -> {
        });

        // then
        verify(transactionManager).commit(any());
    }

    @Test
    @DisplayName("트랜잭션으로 값을 반환하는 로직이 정상적으로 실행되지 않으면 롤백을 호출한다.")
    void runFunction_exception() {
        // given
        final TransactionContext transactionContext = new TransactionContext(transactionManager);

        // when, then
        assertAll(
                () -> assertThatThrownBy(() -> transactionContext.runFunction(() -> {
                    throw new Exception();
                })).isExactlyInstanceOf(DataAccessException.class),
                () -> verify(transactionManager).rollback(any())
        );
    }

    @Test
    @DisplayName("트랜잭션으로 반환값이 없는 로직이 정상적으로 실행되지 않으면 롤백을 호출한다.")
    void runConsumer_exception() {
        // given
        final TransactionContext transactionContext = new TransactionContext(transactionManager);

        // when, then
        assertAll(
                () -> assertThatThrownBy(() -> transactionContext.runConsumer(() -> {
                    throw new Exception();
                })).isExactlyInstanceOf(DataAccessException.class),
                () -> verify(transactionManager).rollback(any())
        );
    }
}
