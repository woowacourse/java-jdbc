package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class TransactionSynchronizationManagerTest {

    private DataSource dataSource = mock(DataSource.class);

    @BeforeEach
    void setUp() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void 리소스를_저장한다() {
        // given
        final Connection connection = mock(Connection.class);
        final SimpleConnectionHolder simpleConnectionHolder = new SimpleConnectionHolder(connection);

        // when
        TransactionSynchronizationManager.bindResource(dataSource, simpleConnectionHolder);

        // then
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isEqualTo(simpleConnectionHolder);
    }

    @Test
    void 리소스를_가져온다() {
        // given
        final Connection connection = mock(Connection.class);
        final SimpleConnectionHolder simpleConnectionHolder = new SimpleConnectionHolder(connection);
        TransactionSynchronizationManager.bindResource(dataSource, simpleConnectionHolder);

        // expect
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isEqualTo(simpleConnectionHolder);
    }

    @Test
    void 리소스를_해제한다() {
        // given
        final Connection connection = mock(Connection.class);
        final SimpleConnectionHolder simpleConnectionHolder = new SimpleConnectionHolder(connection);
        TransactionSynchronizationManager.bindResource(dataSource, simpleConnectionHolder);

        // when
        TransactionSynchronizationManager.unbindResource(dataSource);

        // expect
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }
}
