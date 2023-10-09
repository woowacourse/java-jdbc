package org.springframework.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.test_supporter.DataSourceConfig;

class DataSourceUtilsTest {

    @Nested
    @DisplayName("datasource로 connection을 반환한다.")
    class GetTransaction {

        @Test
        @DisplayName("이미 스레드에서 connection을 사용중이라면, 기존 connection을 반환한다.")
        void alreadyCreate() throws SQLException {
            final DataSource ds = DataSourceConfig.getInstance();
            final Connection firstConnection = DataSourceUtils.getConnection(ds);

            final Connection secondConnection = DataSourceUtils.getConnection(ds);

            assertThat(firstConnection)
                .isEqualTo(secondConnection);
        }

        @Test
        @DisplayName("스레드에서 connection을 생성한적이 없다면, 기존 connection을 반환한다.")
        void firstCreate() throws SQLException {
            final DataSource ds = DataSourceConfig.getInstance();

            final Connection connection = DataSourceUtils.getConnection(ds);

            assertThat(connection)
                .isNotNull();
        }
    }
}
