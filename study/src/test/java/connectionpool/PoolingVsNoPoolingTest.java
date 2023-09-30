package connectionpool;

import com.mysql.cj.jdbc.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.util.ClockSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * pooling을 사용한 경우와 사용하지 않은 경우 트래픽이 얼마나 차이나는지 확인해보자.
 *
 * network bandwidth capture
 * 터미널에 iftop를 설치하고 아래 명령어를 실행한 상태에서 테스트를 실행하자.
 * $ sudo iftop -i lo0 -nf "host localhost"
 * windows 사용자라면 wsl2를 사용하거나 다른 모니터링 툴을 찾아보자.
 */
class PoolingVsNoPoolingTest {

    private final Logger log = LoggerFactory.getLogger(PoolingVsNoPoolingTest.class);

    private static final int COUNT = 1000;

    private static MySQLContainer<?> container;

    @BeforeAll
    static void beforeAll() throws SQLException {
        // TestContainer로 임시 MySQL을 실행한다.
        container = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.30"))
                .withDatabaseName("test");
        container.start();

        final var dataSource = createMysqlDataSource();

        // 테스트에 사용할 users 테이블을 생성하고 데이터를 추가한다.
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(true);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS users;");
                stmt.execute("CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, email VARCHAR(100) NOT NULL) ENGINE=INNODB;");
                stmt.executeUpdate("INSERT INTO users (email) VALUES ('hkkang@woowahan.com')");
                conn.setAutoCommit(false);
            }
        }
    }

    @AfterAll
    static void afterAll() {
        container.stop();
    }

    @Test
    void noPoling() throws SQLException {
        final var dataSource = createMysqlDataSource();

        long start = ClockSource.currentTime();
        connect(dataSource);
        long end = ClockSource.currentTime();

        // 테스트 결과를 확인한다.
        log.info("Elapsed runtime: {}", ClockSource.elapsedDisplayString(start, end));
    }

    @Test
    void pooling() throws SQLException {
        final var config = new HikariConfig();
        config.setJdbcUrl(container.getJdbcUrl());
        config.setUsername(container.getUsername());
        config.setPassword(container.getPassword());
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(1);
        config.setConnectionTimeout(1000);
        config.setAutoCommit(false);
        config.setReadOnly(false);
        final var hikariDataSource = new HikariDataSource(config);

        long start = ClockSource.currentTime();
        connect(hikariDataSource);
        long end = ClockSource.currentTime();

        // 테스트 결과를 확인한다.
        log.info("Elapsed runtime: {}", ClockSource.elapsedDisplayString(start, end));
    }

    private static void connect(DataSource dataSource) throws SQLException {
        // COUNT만큼 DB 연결을 수행한다.
        for (int i = 0; i < COUNT; i++) {
            try (Connection connection = dataSource.getConnection()) {
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
                    if (rs.next()) {
                        rs.getString(1).hashCode();
                    }
                }
            }
        }
    }

    private static MysqlDataSource createMysqlDataSource() throws SQLException {
        final var dataSource = new MysqlDataSource();
        dataSource.setUrl(container.getJdbcUrl());
        dataSource.setUser(container.getUsername());
        dataSource.setPassword(container.getPassword());
        dataSource.setConnectTimeout(1000);
        return dataSource;
    }
}
