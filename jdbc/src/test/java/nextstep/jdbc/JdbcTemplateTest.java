package nextstep.jdbc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private PreparedStatement preparedStatement;

    @BeforeEach
    void init() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        given(dataSource.getConnection()).willReturn(connection);
        preparedStatement = mock(PreparedStatement.class);
        given(connection.prepareStatement(any())).willReturn(preparedStatement);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void queryForObject를_사용할때_조회결과가_하나가_아니면_예외처리한다() throws SQLException {
        //given
        //when
        ResultSet resultSet = mock(ResultSet.class);
        given(resultSet.next()).willReturn(true);
        given(resultSet.isLast()).willReturn(false);
        given(preparedStatement.executeQuery()).willReturn(resultSet);

        //then
        Assertions.assertThatThrownBy(() -> jdbcTemplate.queryForObject("대충 쿼리", rs -> "대충 쿼리 결과"))
                .isInstanceOf(IllegalStateException.class);
    }
}
