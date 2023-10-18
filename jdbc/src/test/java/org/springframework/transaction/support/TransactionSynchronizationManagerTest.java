package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import testUtil.TestDataSourceConfig;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class TransactionSynchronizationManagerTest {

    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = TestDataSourceConfig.getInstance();
    }

    @Test
    void 트랜잭션이_시작되지_않았다면_autoCommit_이_켜진_커넥션을_반환한다() throws Exception {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        assertThat(connection.getAutoCommit()).isTrue();
    }

    @Test
    void 트랜잭션이_시작되었다면_autoCommit_이_꺼진_커넥션을_반환한다() throws Exception {
        TransactionSynchronizationManager.beginTransaction();

        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        assertThat(connection.getAutoCommit()).isFalse();
    }

    @Test
    void 트랜잭션이_켜진_뒤_커밋을_하면_커넥션이_닫힌다() throws Exception {
        // given
        TransactionSynchronizationManager.beginTransaction();
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);

        // when
        TransactionSynchronizationManager.commit(dataSource);

        // then
        assertThat(connection.isClosed()).isTrue();
    }

    @Test
    void 트랜잭션이_켜진_뒤_롤백을_하면_커넥션이_닫힌다() throws Exception {
        // given
        TransactionSynchronizationManager.beginTransaction();
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);

        // when
        TransactionSynchronizationManager.rollback();

        // then
        assertThat(connection.isClosed()).isTrue();
    }

    @Test
    void 트랜잭션이_켜진_뒤_커넥션을_다시_요청하면_동일한_커넥션을_반환한다() {
        // given
        TransactionSynchronizationManager.beginTransaction();
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);

        // when
        Connection sameConnection = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(connection).isEqualTo(sameConnection);
    }

    @Test
    void 커밋_이후_커넥션을_다시_요청_하면_새로운_커넥션을_반환한다() {
        // given
        TransactionSynchronizationManager.beginTransaction();
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);

        // when
        TransactionSynchronizationManager.commit(dataSource);
        Connection otherConnection = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(connection).isNotEqualTo(otherConnection);
    }

    @Test
    void 롤백_이후_커넥션을_다시_요청_하면_새로운_커넥션을_반환한다() {
        // given
        TransactionSynchronizationManager.beginTransaction();
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);

        // when
        TransactionSynchronizationManager.rollback();
        Connection otherConnection = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(connection).isNotEqualTo(otherConnection);
    }

    @AfterEach
    void clear() {
        TransactionSynchronizationManager.rollback();
    }
}
