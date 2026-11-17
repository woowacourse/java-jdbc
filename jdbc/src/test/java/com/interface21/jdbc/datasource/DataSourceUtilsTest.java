package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class DataSourceUtilsTest {

    @Test
    void 다른_스레드에서_등록한_DB_연결은_조회되지_않는다() throws Exception {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try (var executor = Executors.newSingleThreadExecutor()) {
            // when
            Connection otherThreadConnection = executor.submit(
                () -> TransactionSynchronizationManager.getResource(dataSource)
            ).get(5, TimeUnit.SECONDS);

            // then
            assertThat(otherThreadConnection).isNull();
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void 트랜잭션에_등록된_DB_연결은_재사용하고_닫지_않는다() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try {
            // when
            Connection actual = DataSourceUtils.getConnection(dataSource);
            DataSourceUtils.releaseConnection(actual, dataSource);

            // then
            assertThat(actual).isSameAs(connection);
            then(dataSource).should(never()).getConnection();
            then(connection).should(never()).close();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void 트랜잭션_밖에서_얻은_DB_연결은_사용이_끝나면_닫는다() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);

        // when
        Connection actual = DataSourceUtils.getConnection(dataSource);
        DataSourceUtils.releaseConnection(actual, dataSource);

        // then
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        then(connection).should().close();
    }
}
