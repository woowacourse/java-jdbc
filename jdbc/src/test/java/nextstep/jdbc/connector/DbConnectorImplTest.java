package nextstep.jdbc.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DbConnectorImplTest {

    private static final JdbcDataSource jdbcDataSource = new JdbcDataSource();

    @BeforeAll
    static void setUp() {
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
    }

    @DisplayName("DB 커넥션 가져오는 기능 테스트")
    @Test
    void getConnectionTest() throws SQLException {
        //given
        //when
        Connection connection = jdbcDataSource.getConnection();
        //then
        assertThat(connection.isClosed()).isFalse();
    }

}