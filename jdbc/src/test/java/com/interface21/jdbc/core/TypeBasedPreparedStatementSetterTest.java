package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TypeBasedPreparedStatementSetterTest {

    @DisplayName("타입과 순서를 포함한 입력값에 따라 preparedStatement의 파라미터를 설정한다.")
    @Test
    void setValues() throws SQLException {
        // given
        Map<Integer, Object> parameters = new HashMap<>();
        PreparedStatement ps = mock(PreparedStatement.class);
        PreparedStatementSetter pss = new TypeBasedPreparedStatementSetter(
                new SQLParameter(1, "myungoh", JDBCType.VARCHAR),
                new SQLParameter(2, 25, JDBCType.INTEGER)
        );
        doAnswer((a) -> parameters.put(1, "myungoh")).when(ps).setObject(1, "myungoh", JDBCType.VARCHAR);
        doAnswer((a) -> parameters.put(2, 25)).when(ps).setObject(2, 25, JDBCType.INTEGER);

        // when
        pss.setValues(ps);

        // then
        assertThat(parameters.get(1)).isEqualTo("myungoh");
        assertThat(parameters.get(2)).isEqualTo(25);
    }
}
