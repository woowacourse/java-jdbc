package com.techcourse.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceUtils;

class DataSourceUtilsTest {

    @Test
    @DisplayName("DataSourceUtils를 이용해 커넥션을 가져올 수 있다.")
    void getConnection() {
        final DataSource dataSource = DataSourceConfig.getInstance();
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        assertThat(connection)
                .as("기존 커넥션이 없다면 새로 만들어 가져오고,")
                .isNotNull();

        final Connection connection2 = DataSourceUtils.getConnection(dataSource);
        assertThat(connection)
                .as("기존 커넥션이 있다면 등록된 커넥션을 가져온다.")
                .isSameAs(connection2);
    }
}
