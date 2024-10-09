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
     */
    @Test
    void driverManager() throws SQLException {
        // Class.forName("org.h2.Driver"); // JDBC 4.0 부터 생략 가능
        // DriverManager 클래스를 활용하여 static 변수의 정보를 활용하여 h2 db에 연결한다.

        /*
        Q: 자바에서 제공하는 JDBC 드라이버를 직접 다뤄본다.
        A: DriverManager를 사용하면 URL에 맞는 적절한 JDBC 드라이버를 찾아올 수 있다.
        DriverManager.getConnection의 코드를 보면, 찾은 JDBC 드라이버를 활용해서 Connection을 얻어오는 과정을 볼 수 있다.

        +) Class.forName("org.h2.Driver"); 는 리턴 값을 활용해서 어떤 작업을 하기 위함이 아니다.
        이 코드를 수행하면 JDBC 드라이버가 메모리로 로드되기 때문에 사용하는 것!
         */
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
     */
    @Test
    void dataSource() throws SQLException {
        /*
        Q: 데이터베이스에 어떻게 연결하는가?
        A:

        Q: 왜 DataSource를 사용하는가?
        A:
        1. DriverManager는 데이터베이스 연결 정보를 애플리케이션 코드에서 작성해줘야 하지만
        DataSource는 외부 설정 파일(properties, XML 등)을 통해 데이터베이스 연결을 설정할 수 있기 때문이다.
        2. DriverManager는 각 요청마다 새로운 데이터베이스 연결을 생성하므로 트래픽이 많은 애플리케이션에서 성능 저하를 유발한다.
        DataSource는 커넥션 풀링을 지원한다. 따라서 애플리케이션은 매번 새로운 연결을 생성할 필요 없이, 재사용 가능한 커넥션을 활용할 수 있다.
        3. DriverManager는 단일 데이터베이스와의 연결만을 처리할 수 있다. 여러 데이터베이스를 사용하는 분산 트랜잭션 환경에서는 처리할 수 없으며, 별도의 트랜잭션 관리 시스템이 필요하다.
        DataSource는 분산 트랜잭션을 지원한다. 여러 데이터베이스 간의 트랜잭션을 처리해야 할 때, 트랜잭션 매니저와 연동되어 각 데이터베이스에 대한 트랜잭션을 안전하게 처리할 수 있다.

        +) 분산 트랜잭션이란, 단일 트랜잭션 내에서 여러 시스템 또는 데이터베이스 걸쳐 작업을 수행한다.
        모든 참여하는 리소스에 대해 ACID 속성을 보장하는 것이 분산 트랜잭션의 핵심 목표이다.
         */
        final JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(H2_URL);
        dataSource.setUser(USER);
        dataSource.setPassword(PASSWORD);

        try (final var connection = dataSource.getConnection()) {
            assertThat(connection.isValid(1)).isTrue();
        }
    }
}
