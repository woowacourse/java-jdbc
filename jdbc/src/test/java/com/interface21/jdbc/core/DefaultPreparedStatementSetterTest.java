package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.JDBCType;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultPreparedStatementSetterTest {

    private Map<Integer, Object> parameters;
    private PreparedStatement preparedStatement;

    @BeforeEach
    void setUp() {
        parameters = new HashMap<>();
        preparedStatement = mock(PreparedStatement.class);
    }

    @DisplayName("순서에 따라 preparedStatement의 파라미터를 설정한다.")
    @Test
    void setValues_initByObject() throws SQLException {
        // given
        doAnswer((a) -> parameters.put(1, "myungoh")).when(preparedStatement).setObject(1, "myungoh");
        doAnswer((a) -> parameters.put(2, 25)).when(preparedStatement).setObject(2, 25);

        PreparedStatementSetter preparedStatementSetter = new DefaultPreparedStatementSetter("myungoh", 25);

        // when
        preparedStatementSetter.setValues(preparedStatement);

        // then
        assertThat(parameters.get(1)).isEqualTo("myungoh");
        assertThat(parameters.get(2)).isEqualTo(25);
    }

    @DisplayName("타입과 순서를 포함한 입력값에 따라 preparedStatement의 파라미터를 설정한다.")
    @Test
    void setValues_initBySQLParameter() throws SQLException {
        // given
        doAnswer((a) -> parameters.put(1, "myungoh")).when(preparedStatement).setObject(1, "myungoh", JDBCType.VARCHAR);
        doAnswer((a) -> parameters.put(2, 25)).when(preparedStatement).setObject(2, 25, JDBCType.INTEGER);

        PreparedStatementSetter preparedStatementSetter = new DefaultPreparedStatementSetter(
                new SQLParameter(1, "myungoh", JDBCType.VARCHAR),
                new SQLParameter(2, 25, JDBCType.INTEGER)
        );

        // when
        preparedStatementSetter.setValues(preparedStatement);

        // then
        assertThat(parameters.get(1)).isEqualTo("myungoh");
        assertThat(parameters.get(2)).isEqualTo(25);
    }
}
