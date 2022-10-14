package nextstep.jdbc;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DataSourceUtilsTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = Mockito.mock(DataSource.class);
        connection = Mockito.mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
    }

    @Test
    void getResource() throws SQLException {
        Connection connection = DataSourceUtils.getConnection(dataSource);

        assertAll(
                () -> assertThat(connection).isNotNull(),
                () -> assertThat(connection.isClosed()).isFalse()
        );
    }
}
