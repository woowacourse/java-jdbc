package org.springframework.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.springframework.jdbc.datasource.DataSourceUtils.getConnection;
import static org.springframework.jdbc.datasource.DataSourceUtils.releaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class DataSourceUtilsTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    private AutoCloseable openedMock;

    @BeforeEach
    void setUp() {
        openedMock = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        openedMock.close();
    }

    @Test
    void Connection을_얻는다() throws SQLException {
        // given
        given(dataSource.getConnection())
                .willReturn(connection);

        // when
        Connection connection = getConnection(dataSource);

        // then
        assertThat(connection).isNotNull();
    }

    @Test
    void Connection을_얻을_때_예외가_발생하면_Connection_을_얻지_못했다는_예외메시지를_전달한다() throws SQLException {
        // given
        given(dataSource.getConnection())
                .willThrow(new SQLException());

        // expect
        assertThatThrownBy(() -> getConnection(dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasMessageContaining("Failed to obtain JDBC Connection");
    }

    @Test
    void Connection을_해제한다() throws SQLException {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        releaseConnection(connection, dataSource);

        // then
        then(connection)
                .should(times(1))
                .close();
    }

    @Test
    void Connection을_해제할_때_예외가_발생하면_Conection_을_닫지_못했다는_예외메시지를_전달한다() throws SQLException {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        
        willThrow(SQLException.class).given(connection).close();

        // expect
        assertThatThrownBy(() -> releaseConnection(connection, dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasMessageContaining("Failed to close JDBC Connection");
    }
}
