package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.springframework.transaction.support.TransactionSynchronizationManager.getResource;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class TransactionManagerTest {

    private TransactionManager transactionManager;
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        transactionManager = new TransactionManager(dataSource);
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void 트랜잭션을_시작한다() throws SQLException {
        // given
        final Connection connection = mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);

        // when
        transactionManager.initialize();

        // when
        final ConnectionHolder connectionHolder = getResource(dataSource);
        assertSoftly(softly -> {
            softly.assertThat(connectionHolder.isTransactionActive()).isTrue();
            softly.assertThat(connectionHolder.getConnection()).isEqualTo(connection);
        });
        then(connection)
                .should(times(1))
                .setAutoCommit(false);
    }

    @Test
    void 커밋한다() throws SQLException {
        // given
        final Connection connection = mock(Connection.class);
        final ConnectionHolder connectionHolder = new ConnectionHolder(connection);
        TransactionSynchronizationManager.bindResource(dataSource, connectionHolder);

        // when
        transactionManager.commit();

        // when
        then(connection)
                .should(times(1))
                .commit();
    }

    @Test
    void 커넥션을_종료한다() throws SQLException {
        // given
        final Connection connection = mock(Connection.class);
        final ConnectionHolder connectionHolder = new ConnectionHolder(connection);
        TransactionSynchronizationManager.bindResource(dataSource, connectionHolder);

        // when
        transactionManager.close();

        // when
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        then(connection)
                .should(times(1))
                .close();
    }
}
