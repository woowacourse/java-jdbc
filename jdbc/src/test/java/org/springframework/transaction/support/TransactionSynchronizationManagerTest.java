package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.mock;

import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class TransactionSynchronizationManagerTest {

    DataSource dataSource = mock(DataSource.class);

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void 트랜잭션을_시작하면_isTransactionEnable_true() {
        // given
        TransactionSynchronizationManager.doBegin();

        // when & then
        assertThat(TransactionSynchronizationManager.isTransactionEnable())
            .isTrue();

        TransactionSynchronizationManager.doEnd();
    }

    @Test
    void 트랜잭션을_시작하지_않으면_isTransactionEnable_false() {
        // when & then
        assertThat(TransactionSynchronizationManager.isTransactionEnable())
            .isFalse();
    }
}
