package com.interface21.jdbc.core.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.CannotReleaseJdbcResourceException;
import com.interface21.jdbc.core.extractor.ManualExtractor;
import com.interface21.jdbc.mapper.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PreparedStatementMapperTest {

    @DisplayName("pss가 null이거나 닫혀 있다면 예외가 발생한다.")
    @Test
    void pssNull() throws SQLException {
        PreparedStatement nullPss = null;
        PreparedStatement closedPss = mock(PreparedStatement.class);
        when(closedPss.isClosed()).thenReturn(true);
        Object[] objects = new Object[3];

        assertAll(() -> {
            Assertions.assertThatThrownBy(() -> new ObjectMappedStatement(nullPss, objects))
                    .isInstanceOf(CannotGetJdbcConnectionException.class);
            Assertions.assertThatThrownBy(() -> new ObjectMappedStatement(closedPss, objects))
                    .isInstanceOf(CannotGetJdbcConnectionException.class);
        });
    }

    @DisplayName("자원을 정상적으로 반환하지 못하면 예외가 발생한다.")
    @Test
    void close() throws SQLException {
        PreparedStatement pss = mock(PreparedStatement.class);
        Object[] objects = new Object[3];
        PreparedStatementMapper pssMapper = new ObjectMappedStatement(pss, objects);
        doThrow(new SQLException()).when(pss).close();

        Assertions.assertThatThrownBy(pssMapper::close)
                .isInstanceOf(CannotReleaseJdbcResourceException.class);
    }
}
