package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ConnectionHolderTest {

    @Test
    void TransactionActiveFlag를_설정한다() {
        // given
        final Connection connection = mock(Connection.class);
        final ConnectionHolder connectionHolder = new ConnectionHolder(connection);

        // when
        connectionHolder.setTransactionActive(true);

        // then
        assertThat(connectionHolder.isTransactionActive()).isTrue();
    }

    @Test
    void 동일한_커넥션인지_비교한다() {
        // given
        final Connection connection = mock(Connection.class);
        final ConnectionHolder connectionHolder = new ConnectionHolder(connection);

        // expect
        assertThat(connectionHolder.isSameConnection(connection)).isTrue();
    }
}
