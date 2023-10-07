package org.springframework.transaction.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;
import static org.mockito.Mockito.mock;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class TransactionManagerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private ConnectionHolder connectionHolder = ConnectionHolder.getInstance();
    private TransactionManager transactionManager;

    @BeforeEach
    void before() {
        transactionManager = new TransactionManager(dataSource, connectionHolder);
    }

    @Test
    void Connection을_반환한다() throws Exception {
        // given
        given(dataSource.getConnection())
                .willReturn(connection);

        // when
        Connection connection = transactionManager.getConnection();

        // then
        assertThat(connection).isNotNull();
    }

    @Test
    void Connection이_존재하면_새로운_connection을_생성하지_않는다() throws Exception {
        // given
        given(dataSource.getConnection())
                .willReturn(connection);

        // when
        Connection connection = transactionManager.getConnection();
        Connection other = transactionManager.getConnection();

        // then
        assertThat(connection).isEqualTo(other);
    }

    @Test
    void 트랜잭션을_시작하면_auto_commit이_되지_않는다() throws Exception {
        // given
        given(dataSource.getConnection())
                .willReturn(connection);

        // when
        Connection connection = transactionManager.start();

        // then
        then(connection)
                .should(times(1))
                .setAutoCommit(false);
    }

    @Test
    void 트랜잭션을_커밋하면_커넥션을_닫고_holder에서_제거한다() throws Exception {
        // given
        given(dataSource.getConnection())
                .willReturn(connection);

        // when
        transactionManager.commit();

        // then
        then(connection)
                .should(times(1))
                .commit();
        then(connection)
                .should(times(1))
                .close();
        assertThat(connectionHolder.isEmpty()).isTrue();
    }

    @Test
    void 트랜잭션을_롤백하면_커넥션을_닫고_holder에서_제거한다() throws Exception {
        // given
        given(dataSource.getConnection())
                .willReturn(connection);

        // when
        transactionManager.rollback();

        // then
        then(connection)
                .should(times(1))
                .rollback();
        then(connection)
                .should(times(1))
                .close();
        assertThat(connectionHolder.isEmpty()).isTrue();
    }
}
