package com.interface21.jdbc.core.mapper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TypeMapperTest {

    @DisplayName("파라미터 매핑이 잘되었는지 확인한다.")
    @Test
    void setStatement() throws SQLException {
        PreparedStatement pss = mock(PreparedStatement.class);
        when(pss.isClosed()).thenReturn(false);

        Object[] params = new Object[]{"id", "name", "value"};
        JDBCType[] types = new JDBCType[]{JDBCType.VARCHAR, JDBCType.VARCHAR, JDBCType.VARCHAR};

        TypeMapper tms = new TypeMapper(pss, params, types);
        tms.setStatement();

        verify(pss).setObject(1, params[0], JDBCType.VARCHAR);
        verify(pss).setObject(2, params[1], JDBCType.VARCHAR);
        verify(pss).setObject(3, params[2], JDBCType.VARCHAR);
    }

}
