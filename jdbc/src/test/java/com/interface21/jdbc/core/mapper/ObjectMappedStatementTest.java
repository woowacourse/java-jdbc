package com.interface21.jdbc.core.mapper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ObjectMappedStatementTest {

    @DisplayName("파라미터 매핑이 잘되었는지 확인한다.")
    @Test
    void setStatement() throws SQLException {
        PreparedStatement pss = mock(PreparedStatement.class);
        Object[] params = new Object[]{"id", "name", "value"};

        ObjectMapper oms = new ObjectMapper(pss, params);
        oms.setStatement();

        verify(pss, times(2)).setObject(1, params[0]);
        verify(pss, times(2)).setObject(2, params[1]);
        verify(pss, times(2)).setObject(3, params[2]);
    }
}
