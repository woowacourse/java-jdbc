package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

class PreparedStatementSetterTest {

    @Test
    void setValues() throws SQLException {
        // given
        PreparedStatement pstmt = mock(PreparedStatement.class);
        PreparedStatementSetter preparedStatementSetter = ps -> ps.setObject(1, 1L);

        // when
        preparedStatementSetter.setValues(pstmt);

        // then
        verify(pstmt).setObject(1, 1L);
    }
}
