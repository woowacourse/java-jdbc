package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class TransactionManagerTest {

    @AfterEach
    void tearDown() {
        TransactionManager.clear();
    }

    @Test
    void begin_호출하면_트랜잭션_true() {
        // given
        TransactionManager.begin();

        // when & then
        assertThat(TransactionManager.isTransactionEnable())
            .isTrue();
    }

    @Test
    void 트랜잭션을_시작하지_않고_rollback_하면_예외() {
        // when & then
        assertThatThrownBy(TransactionManager::setRollback)
            .isInstanceOf(IllegalStateException.class);
    }
}
