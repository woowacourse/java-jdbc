package connectionpool.stage0;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

class Stage0Test {

    private static final String DB_PROPERTIES = "db.properties";
    private static final String PROPERTY_DB_URL = "db.url";
    private static final String PROPERTY_DB_USER = "db.user";
    private static final String PROPERTY_DB_PASSWORD = "db.password";

    /**
     * DriverManager
     * JDBC 드라이버를 관리하는 가장 기본적인 방법.
     * 커넥션 풀, 분산 트랜잭션을 지원하지 않아서 잘 사용하지 않는다.
     *
     * JDBC 4.0 이전에는 Class.forName 메서드를 사용하여 JDBC 드라이버를 직접 등록해야 했다.
     * JDBC 4.0 부터 DriverManager가 적절한 JDBC 드라이버를 찾는다.
     *
     * Autoloading of JDBC drivers
     * https://docs.oracle.com/javadb/10.8.3.0/ref/rrefjdbc4_0summary.html
     */
    @Test
    void driverManager() throws Exception {
        // Class.forName("org.h2.Driver"); // JDBC 4.0 부터 생략 가능
        // DriverManager 클래스를 활용하여 static 변수의 정보를 활용하여 h2 db에 연결한다.
        final Properties props = loadProps();

        final String url = props.getProperty(PROPERTY_DB_URL);
        final String user = props.getProperty(PROPERTY_DB_USER);
        final String password = props.getProperty(PROPERTY_DB_PASSWORD);

        try (final Connection connection = DriverManager.getConnection(url, user, password)) {
            assertThat(connection.isValid(1)).isTrue();
        }
    }

    /**
     * DataSource
     * 데이터베이스, 파일 같은 물리적 데이터 소스에 연결할 때 사용하는 인터페이스.
     * 구현체는 각 vendor에서 제공한다.
     * 테스트 코드의 JdbcDataSource 클래스는 h2에서 제공하는 클래스다.
     *
     * DriverManager가 아닌 DataSource를 사용하는 이유
     * - DataSource는 인터페이스이기 때문에 H2, MySQL, PostgreSQL 같은 DB 벤더별 DataSource,
     *   혹은 HikariCP/DBCP2 같은 커넥션 풀 구현체를 코드 수정 없이 교체할 수 있다.
     * - 커넥션 풀링(Connection pooling) 또는 분산 트랜잭션은 DataSource를 통해서 사용 가능하다.
     *
     * Using a DataSource Object to Make a Connection
     * https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/javax/sql/package-summary.html
     */
    @Test
    void dataSource() throws Exception {
        final Properties props = loadProps();

        // 구현체 설정(H2)
        final JdbcDataSource jdbcDataSource = new JdbcDataSource();
        final String url = props.getProperty(PROPERTY_DB_URL);
        jdbcDataSource.setURL(url);
        final String user = props.getProperty(PROPERTY_DB_USER);
        jdbcDataSource.setUser(user);
        final String password = props.getProperty(PROPERTY_DB_PASSWORD);
        jdbcDataSource.setPassword(password);

        // 사용부는 인터페이스만 의존
        final DataSource dataSource = jdbcDataSource;

        try (final var connection = dataSource.getConnection()) {
            assertThat(connection.isValid(1)).isTrue();
        }
    }

    private Properties loadProps() throws Exception {
        final Properties props = new Properties();
        try (final InputStream in = getClass().getClassLoader().getResourceAsStream(DB_PROPERTIES)) {
            props.load(in);
        }
        return props;
    }
}
