package org.springframework.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.jdbc.core.TestUser.TEST_USER_ROW_MAPPER;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class StatementExecutorTest {

    private final StatementExecutor sut = new StatementExecutor();

    @Test
    void statement를_실행하여_결과를_반환한다() throws SQLException {
        // given
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true, true, false);
        given(resultSet.getString(any(Integer.class))).willReturn("hello", "world");

        // when
        final List<TestUser> result = sut.execute(preparedStatement, TEST_USER_ROW_MAPPER);

        // then
        assertThat(result).containsExactly(new TestUser("hello"), new TestUser("world"));
    }
}
