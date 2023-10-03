package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class TransactionManagerTest {

    DataSource dataSource = mock(DataSource.class);

    @BeforeEach
    void setUp() throws Exception {
        given(dataSource.getConnection())
            .willReturn(mock(Connection.class));
    }

    @AfterEach
    void tearDown() {
        if (TransactionManager.isConnectionEnable()) {
            TransactionManager.releaseConnection();
        }
    }

    @Test
    void getConnection_호출하면_트랜잭션_false() {
        // given
        TransactionManager.getConnection(dataSource);

        // when & then
        assertThat(TransactionManager.isTransactionEnable())
            .isFalse();
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
    void rollback_호출해도_트랜잭션은_유지() {
        // given
        TransactionManager.begin();
        TransactionManager.getConnection(dataSource);

        // when
        TransactionManager.rollback();

        // then
        assertThat(TransactionManager.isTransactionEnable())
            .isTrue();
    }

    @Test
    void commit_호출해도_트랜잭션은_유지() {
        // given
        TransactionManager.begin();
        TransactionManager.getConnection(dataSource);

        // when
        TransactionManager.commit();

        // then
        assertThat(TransactionManager.isTransactionEnable())
            .isTrue();
    }

    @Test
    void releaseConnection_호출하면_트랜잭션_종료() {
        // given
        TransactionManager.begin();
        TransactionManager.getConnection(dataSource);

        // when
        TransactionManager.releaseConnection();

        // then
        assertThat(TransactionManager.isTransactionEnable())
            .isFalse();
    }

    @Test
    void 커넥션을_얻지않고_rollback_하면_예외() {
        // given
        TransactionManager.begin();

        // when & then
        assertThatThrownBy(TransactionManager::rollback)
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 커넥션을_얻지않고_commit_하면_예외() {
        // given
        TransactionManager.begin();

        // when & then
        assertThatThrownBy(TransactionManager::commit)
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 커넥션을_얻지_않고_releaseConnection_하면_예외() {
        // given
        TransactionManager.begin();

        assertThatThrownBy(TransactionManager::releaseConnection)
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 트랜잭션을_시작하지_않고_rollback_하면_예외() {
        // given
        TransactionManager.getConnection(dataSource);

        // when & then
        assertThatThrownBy(TransactionManager::rollback)
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 트랜잭션을_시작하지_않고_commit_하면_예외() {
        // given
        TransactionManager.getConnection(dataSource);

        // when & then
        assertThatThrownBy(TransactionManager::commit)
            .isInstanceOf(IllegalStateException.class);
    }
}
