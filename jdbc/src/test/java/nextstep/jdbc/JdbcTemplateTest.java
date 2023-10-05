package nextstep.jdbc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

class JdbcTemplateTest {

    @Mock
    private Connection connection;
    @Mock
    private DataSource dataSource;
    @Mock
    private PreparedStatement preparedStatement;
    @InjectMocks
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("try문에서 메서드 호출을 통해 변수를 초기화 해도 객체를 사용 후 반환한다.")
    void tryWithResourcesTest() throws SQLException {
        //given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        //when
        jdbcTemplate.update(connection, "Test Sql");

        //then
        verify(preparedStatement, times(1)).close();
    }
}
