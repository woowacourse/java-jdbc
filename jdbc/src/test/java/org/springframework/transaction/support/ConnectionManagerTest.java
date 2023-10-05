package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ConnectionManagerTest {

    private ConnectionManager sut;
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        sut = new ConnectionManager(dataSource);
    }

    @Test
    void 커넥션을_반환한다() throws SQLException {
        // given
        final Connection connection = mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);

        // when
        final Connection result = sut.getConnection();

        // then
        assertThat(result).isEqualTo(connection);
    }

    @Test
    void ConnectionHolder에_커넥션이_존재하면_해당_커넥션을_반환한다() throws SQLException {
        // given
        final Connection connection = mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
        sut.initializeConnection();

        // when
        final Connection result = sut.getConnection();

        // then
        assertThat(result).isEqualTo(connection);
    }

    @Test
    void ConnectionHolder에_커넥션을_저장한다() throws SQLException {
        // given
        final Connection connection = mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);

        // when
        sut.initializeConnection();

        // then
        assertThat(sut.getConnection()).isEqualTo(connection);
    }

    @Test
    void ConnectionHolder에_저장된_커넥션이_아니라면_커넥션을_닫는다() throws SQLException {
        // given
        final Connection connection = mock(Connection.class);

        // when
        sut.release(connection);

        // then
        then(connection)
                .should(times(1))
                .close();
    }

    @Test
    void ConnectionHolder에_저장된_커넥션이라면_ConnectionHodler를_비우고_커넥션을_닫는다() throws SQLException {
        // given
        final Connection connection = mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
        sut.initializeConnection();

        // when
        sut.close(connection);

        // then
        then(connection)
                .should(times(1))
                .close();
    }

    @Test
    void 커넥션을_롤백한다() throws SQLException {
        // given
        final Connection connection = mock(Connection.class);

        // when
        sut.rollback(connection);

        // then
        then(connection)
                .should(times(1))
                .rollback();
    }
}
