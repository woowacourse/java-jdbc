package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ConnectionHolderTest {

    private ConnectionHolder sut = ConnectionHolder.getInstance();

    @BeforeEach
    void setUp() {
        sut.clear();
    }

    @Test
    void 커넥션을_ThreadLocal에_저장한다() {
        // given
        final Connection connection = mock(Connection.class);

        // when
        sut.setConnection(connection);

        // then
        assertThat(sut.getConnection()).isEqualTo(connection);
    }

    @Test
    void ThreadLocal에_저장된_커넥션을_가져온다() {
        // given
        final Connection connection = mock(Connection.class);
        sut.setConnection(connection);

        // when
        final Connection findConnection = sut.getConnection();

        // then
        assertThat(findConnection).isEqualTo(connection);
    }

    @Test
    void ThreadLocal에_저장된_커넥션을_제거한다() {
        // given
        final Connection connection = mock(Connection.class);
        sut.setConnection(connection);

        // when
        sut.clear();

        // then
        assertThat(sut.getConnection()).isNull();
    }

    @Test
    void ThreadLocal에_저장된_커넥션이_없는지_확인한다() {
        // expect
        assertThat(sut.isEmpty()).isTrue();
    }

    @Test
    void ThreadLocal에_저장된_커넥션이랑_동일한지_확인한다() {
        // given
        final Connection connection = mock(Connection.class);
        sut.setConnection(connection);

        // expect
        assertThat(sut.isSameConnection(connection)).isTrue();
    }
}
