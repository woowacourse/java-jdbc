package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.interface21.transaction.PlatformTransactionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionTemplateTest {

    private TransactionTemplate transactionTemplate;
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        this.transactionManager = mock(PlatformTransactionManager.class);
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Test
    void 트랜잭션을_커밋한다() {
        // given
        TransactionCallback<Long> action = () -> 1L;

        // when
        Long result = transactionTemplate.execute(action);

        // then
        assertAll(
                () -> verify(transactionManager).commit(any()),
                () -> verify(transactionManager, never()).rollback(any()),
                () -> assertThat(result).isEqualTo(1L)
        );
    }

    @Test
    void 트랜잭션을_롤백한다() {
        // given
        TransactionCallback<Void> action = () -> {
            throw new IllegalArgumentException();
        };

        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> transactionTemplate.execute(action))
                        .isExactlyInstanceOf(IllegalArgumentException.class),
                () -> verify(transactionManager).rollback(any()),
                () -> verify(transactionManager, never()).commit(any())
        );
    }
}
