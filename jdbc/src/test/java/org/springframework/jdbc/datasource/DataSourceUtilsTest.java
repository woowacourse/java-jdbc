package org.springframework.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.config.DataSourceConfig;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@DisplayNameGeneration(ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class DataSourceUtilsTest {

    @Test
    void getSameConnectionWhenRequired() {
        // given
        Connection firstConnection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());

        // when
        Connection secondConnection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());

        // then
        assertThat(firstConnection).isEqualTo(secondConnection);
    }

    @Test
    void closeConnectionAndUnbindResourceWhenReleaseConnection() throws SQLException {
        // given
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = DataSourceUtils.getConnection(dataSource);

        // when
        DataSourceUtils.releaseConnection(connection, dataSource);

        // then
        assertThat(connection.isClosed()).isTrue();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }
}
