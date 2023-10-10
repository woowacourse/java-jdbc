package org.springframework.transaction.support;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.config.TestDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionSynchronizationManagerTest {

    @Test
    void getResource() throws SQLException {
        //given
        final DataSource dataSource = TestDataSource.getInstance();
        final Connection expected = dataSource.getConnection();
        TransactionSynchronizationManager.bindResource(dataSource, expected);

        //when
        final Connection actual = TransactionSynchronizationManager.getResource(dataSource);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void bindResource() throws SQLException {
        //given
        final DataSource dataSource = TestDataSource.getInstance();

        //when
        TransactionSynchronizationManager.bindResource(TestDataSource.getInstance(), dataSource.getConnection());

        //then
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNotNull();

    }

    @Test
    void unbindResource() throws SQLException {
        //given
        final DataSource dataSource = TestDataSource.getInstance();
        TransactionSynchronizationManager.bindResource(TestDataSource.getInstance(), dataSource.getConnection());
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNotNull();

        //when
        TransactionSynchronizationManager.unbindResource(dataSource);

        //then
        final Connection resource = TransactionSynchronizationManager.getResource(dataSource);
        assertThat(resource).isNull();
    }
}
