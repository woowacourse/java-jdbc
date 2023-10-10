package org.springframework.jdbc.datasource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.DataSourceConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class DataSourceUtilsTest {

    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        this.dataSource = DataSourceConfig.getInstance();
    }

    @Test
    void 같은_데이터소스간에는_하나의_커넥션을_지급한다() {
        Connection expected = DataSourceUtils.getConnection(dataSource);

        Connection connection = DataSourceUtils.getConnection(dataSource);

        assertThat(connection).isEqualTo(expected);
    }

    @Test
    void 데이터소스가_다르면_다른_커넥션을_지급한다() {
        Connection connection = DataSourceUtils.getConnection(dataSource);

        Connection other = DataSourceUtils.getConnection(DataSourceConfig.newInstance());

        assertThat(other).isNotEqualTo(connection);
    }

    @Test
    void 쓰레드가_다르면_같은_데이터소스라도_다른_커넥션을_받는다() throws InterruptedException {
        AtomicReference<Connection> connection = new AtomicReference<>();
        AtomicReference<Connection> otherConnection = new AtomicReference<>();

        Thread thread = new Thread(() -> connection.set(DataSourceUtils.getConnection(dataSource)));
        Thread otherThread = new Thread(() -> otherConnection.set(DataSourceUtils.getConnection(dataSource)));
        thread.start();
        otherThread.start();
        thread.join();
        otherThread.join();

        assertThat(connection.get()).isNotEqualTo(otherConnection.get());
    }
}
