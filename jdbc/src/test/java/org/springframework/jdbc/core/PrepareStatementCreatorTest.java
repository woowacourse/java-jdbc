package org.springframework.jdbc.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PrepareStatementCreatorTest {

    private final PrepareStatementCreator sut = new PrepareStatementCreator();

    @Test
    void 파라미터를_세팅한_PrepareStatement를_생성하여_반환한다() throws SQLException {
        // given
        final Connection connection = mock(Connection.class);
        final String sql = "select * from woowacourse where name = ? and course = ?;";
        final Object[] parameters = {"호이", "backend"};
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        given(connection.prepareStatement(any(String.class)))
                .willReturn(preparedStatement);

        // when
        sut.create(connection, sql, parameters);

        // then
        then(preparedStatement)
                .should(times(parameters.length))
                .setObject(any(Integer.class), any(Object.class));
    }
}
