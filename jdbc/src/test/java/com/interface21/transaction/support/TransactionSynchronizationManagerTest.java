package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {

    @Test
    void DataSource에_등록된_Connection을_반환() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = dataSource.getConnection();
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        Connection actual = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(actual).isEqualTo(connection);
    }

    @Test
    void DataSource에_등록된_Connection이_없을_경우_null_반환() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);

        // when
        Connection actual = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(actual).isNull();
    }

    @Test
    void DataSource에_등록된_Connection을_삭제() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = dataSource.getConnection();
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        Connection actual = TransactionSynchronizationManager.unbindResource(dataSource);

        // then
        assertThat(actual).isEqualTo(connection);
    }

    @Test
    void 등록되지않은_DataSource의_Connection을_삭제하는_경우_예외() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);

        // when, then
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
