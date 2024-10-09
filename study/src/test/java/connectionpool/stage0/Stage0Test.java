package connectionpool.stage0;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class Stage0Test {

    private static final String H2_URL = "jdbc:h2:./test";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

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
     *
     * Autoloading of JDBC drivers.
     * In earlier versions of JDBC, applications had to manually register drivers before requesting Connections.
     * With JDBC 4.0, applications no longer need to issue a Class.forName() on the driver name;
     * instead, the DriverManager will find an appropriate JDBC driver when the application requests a Connection.
     */
    @Test
    void driverManager() throws SQLException {
        // JDBC 드라이버를 로드하는 데 사용되는 메서드로,
        // 4.0 부터 DriverManager가 자동으로 드라이버를 로드하도록 지원하기 때문에 생략 가능
        // Class.forName("org.h2.Driver");

        // DriverManager 클래스를 활용하여 static 변수의 정보를 활용하여 h2 db에 연결한다.
        try (final Connection connection = DriverManager.getConnection(H2_URL, USER, PASSWORD)) {
            assertThat(connection.isValid(1)).isTrue();
        }
    }

    /**
     * DataSource
     * 데이터베이스, 파일 같은 물리적 데이터 소스에 연결할 때 사용하는 인터페이스.
     * 구현체는 각 vendor에서 제공한다.
     * 테스트 코드의 JdbcDataSource 클래스는 h2에서 제공하는 클래스다.
     *
     * DirverManager가 아닌 DataSource를 사용하는 이유
     * - 애플리케이션 코드를 직접 수정하지 않고 properties로 디비 연결을 변경할 수 있다.
     * - 커넥션 풀링(Connection pooling) 또는 분산 트랜잭션은 DataSource를 통해서 사용 가능하다.
     *
     * Using a DataSource Object to Make a Connection
     * https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/javax/sql/package-summary.html
     *
     * yml spring.datasource 하위에 url, username, password 정보를 입력하면 Datasource에 자동으로 설정되어,
     * 디비 정보 변경에도 애플리케이션 코드를 수정하지 않아도 된다.
     */
    @Test
    void dataSource() throws SQLException {
        final JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(H2_URL);
        dataSource.setUser(USER);
        dataSource.setPassword(PASSWORD);

        try (final var connection = dataSource.getConnection()) {
            assertThat(connection.isValid(1)).isTrue();
        }
    }
}
