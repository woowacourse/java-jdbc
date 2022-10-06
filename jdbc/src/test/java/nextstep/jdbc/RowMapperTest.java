package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;

class RowMapperTest {

    @DisplayName("RowMapper 인터페이스를 통해 행 단위로 ResultSet에 값을 매핑한다.")
    @Test
    void mappingResultSetByRowMapper() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getLong(0)).thenReturn(1L);

        final RowMapper<Object> rowMapper = (rs, rowNum) -> {
            rs.getLong(rowNum);
            return rs;
        };

        ResultSet targetResultSet = (ResultSet) rowMapper.mapRow(resultSet, 0);
        assertThat(targetResultSet.getLong(0)).isOne();
    }
}
