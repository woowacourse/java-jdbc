package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.SQLException;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataSourceUtilsTest {

    private JdbcDataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:data_source_utils");
    }

    @Test
    void releasesConnectionWithoutBindingItOutsideTransaction() throws SQLException {
        try (var connection = DataSourceUtils.getConnection(dataSource)) {
            try {
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();

                DataSourceUtils.releaseConnection(connection, dataSource);

                assertThat(connection.isClosed()).isTrue();
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        }
    }

    @Test
    void leavesBoundConnectionOpenUntilItIsUnbound() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            try {
                assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);

                DataSourceUtils.releaseConnection(connection, dataSource);

                assertThat(connection.isClosed()).isFalse();
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }

            DataSourceUtils.releaseConnection(connection, dataSource);
            assertThat(connection.isClosed()).isTrue();
        }
    }
}
