package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class DataSourceUtilsTest {

    @Test
    void DataSource의_Connection을_반환() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        // when
        Connection actual = DataSourceUtils.getConnection(dataSource);

        // then
        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> verify(dataSource).getConnection()
        );
    }

    @Test
    void DataSource의_Connection이_있다면_기존_Connection을_반환() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        Connection initialConnection = DataSourceUtils.getConnection(dataSource);

        // when
        Connection actual = DataSourceUtils.getConnection(dataSource);

        // then
        assertThat(actual).isEqualTo(initialConnection);
    }

    @Test
    void DataSource의_Connection을_닫음_등록되지_않은_Connenction인_경우() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        // when
        DataSourceUtils.releaseConnection(connection, dataSource);

        // then
        verify(connection).close();
    }

    @Test
    void DataSource의_Connection을_닫음_등록된_Connection인_경우() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        doAnswer(invocation -> when(connection.isClosed()).thenReturn(true))
                .when(connection).close();

        Connection initialConnection = DataSourceUtils.getConnection(dataSource);

        // when
        DataSourceUtils.releaseConnection(connection, dataSource);

        // then
        assertAll(
                () -> assertThat(connection).isEqualTo(initialConnection),
                () -> verify(initialConnection).close()
        );
    }

    @Test
    void 등록된_Connection과_다른Connection을_보낸_경우_모두_닫음() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection firstConnection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(firstConnection);

        doAnswer(invocation -> when(firstConnection.isClosed()).thenReturn(true))
                .when(firstConnection).close();

        Connection initialConnection = DataSourceUtils.getConnection(dataSource);
        Connection secondConnection = mock(Connection.class);

        // when
        DataSourceUtils.releaseConnection(secondConnection, dataSource);

        // then
        assertAll(
                () -> assertThat(firstConnection).isEqualTo(initialConnection),
                () -> assertThat(firstConnection).isNotEqualTo(secondConnection),
                () -> verify(firstConnection).close(),
                () -> verify(secondConnection).close()
        );
    }
}
