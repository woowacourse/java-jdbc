package org.springframework.transaction.support;

import nextstep.jdbc.TestDataSourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TransactionSynchronizationManagerTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        // Create a new data source and connection for each test
        dataSource = TestDataSourceConfig.createJdbcDataSource();
        connection = dataSource.getConnection();
    }

    @DisplayName("DataSource에 해당하는 Connection을 반환한다.")
    @Test
    void getResource() {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        Connection boundConnection = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(boundConnection).isEqualTo(connection);
    }

    @DisplayName("resources 자체가 null이거나 resources의 키에 해당하는 값이 없는 경우 null을 반환한다.")
    @Test
    void getResource_ConnectionIsNull() {
        // when
        Connection noBoundedConnection = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(noBoundedConnection).isNull();
    }

    @DisplayName("DataSource에 해당하는 Connection을 바인딩한다.")
    @Test
    void bindResource() {
        // when & then
        assertDoesNotThrow(() -> TransactionSynchronizationManager.bindResource(dataSource, connection));
    }

    @DisplayName("DataSource에 이미 바인딩된 Connection이 있을 경우 IllegalStateException을 던진다.")
    @Test
    void bindResource_ConnectionAlreadyBound() {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when & then
        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, connection))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Already value bound for key " + dataSource);
    }

    @DisplayName("DataSource에 바인딩된 Connection의 바인딩을 해제한다.")
    @Test
    void unbindResource() {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when & then
        assertDoesNotThrow(() -> TransactionSynchronizationManager.unbindResource(dataSource));
    }

    @DisplayName("바인딩을 해제할 때 DataSource에 바인딩된 Connection이 없을 경우, IllegalStateException을 던진다.")
    @Test
    void unbindResource_ConnectionNoBounded() {
        // when & then
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No value bound for key " + dataSource);
    }
}
