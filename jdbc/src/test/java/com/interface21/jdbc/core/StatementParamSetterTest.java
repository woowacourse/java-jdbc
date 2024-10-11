package com.interface21.jdbc.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class StatementParamSetterTest {

    PreparedStatement prepareStatement = mock(PreparedStatement.class);

    @DisplayName("파라미터를 순서대로 설정한다.")
    @Test
    void setParams() throws SQLException {
        StatementParamSetter.setParams(prepareStatement, "potato", "hi");

        verify(prepareStatement).setObject(1, "potato");
        verify(prepareStatement).setObject(2, "hi");
    }
}
