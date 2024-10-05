package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PreparedStatementResolverTest {

    @DisplayName("PreparedStatement에 파라미터 값을 세팅해준다")
    @Test
    void resolve() throws SQLException {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        PreparedStatementResolver resolver = new PreparedStatementResolver();
        PreparedStatement statement = mock(PreparedStatement.class);

        doNothing()
                .when(statement)
                .setString(anyInt(), captor.capture());

        resolver.resolve(statement, "test1", "test2", "test3");

        assertThat(captor.getAllValues())
                .containsExactly("test1", "test2", "test3");
    }
}
