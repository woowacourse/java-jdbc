package org.springframework.transaction.support;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class ConnectionHolderTest {

    private final Connection connection = mock(Connection.class);

    @Test
    void conneciton을_저장한다() {
        // given
        ConnectionHolder connectionHolder = ConnectionHolder.getInstance();

        // when
        connectionHolder.setConnection(connection);

        // then
        assertThat(connectionHolder.getConnection()).isEqualTo(connection);
    }

    @Test
    void 저장한_connection이_없으면_true를_반환한다() {
        // given
        ConnectionHolder connectionHolder = ConnectionHolder.getInstance();

        // when
        boolean result = connectionHolder.isEmpty();

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 저장한_connection이_있으면_false를_반환한다() {
        // given
        ConnectionHolder connectionHolder = ConnectionHolder.getInstance();
        connectionHolder.setConnection(connection);

        // when
        boolean result = connectionHolder.isEmpty();

        // then
        assertThat(result).isFalse();
    }
}
