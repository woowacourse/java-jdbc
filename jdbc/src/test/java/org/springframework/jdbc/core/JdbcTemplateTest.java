package org.springframework.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.springframework.jdbc.core.TestUser.TEST_USER_ROW_MAPPER;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JdbcTemplateTest {

    private PreparedStatement preparedStatement;
    private JdbcTemplate sut;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
        preparedStatement = mock(PreparedStatement.class);
        given(connection.prepareStatement(any())).willReturn(preparedStatement);

        sut = new JdbcTemplate(dataSource);
    }

    @Test
    void 쿼리를_실행한다() throws SQLException {
        // when
        sut.update("insert into users (account) values (?)", "hello");

        // then
        then(preparedStatement)
                .should(times(1))
                .executeUpdate();
    }

    @Test
    void 단일_조회시_결과가_두개_이상이라면_예외를_던진다() throws SQLException {
        // given
        final ResultSet resultSet = mock(ResultSet.class);
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true, true, false);
        given(resultSet.getString(any(Integer.class))).willReturn("hello", "world");
        final String query = "select * from users where name = hello";

        // expect
        assertThatThrownBy(() -> sut.queryForObject(query, TEST_USER_ROW_MAPPER))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("2개 이상의 결과를 반환할 수 없습니다.");
    }

    @Test
    void 단일_조회시_결과가_하나라면_정상적으로_결과를_반환한다() throws SQLException {
        // given
        final ResultSet resultSet = mock(ResultSet.class);
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true, false);
        given(resultSet.getString(any(Integer.class))).willReturn("hello");
        final String query = "select * from users where name = hello";

        // when
        final Optional<TestUser> user = sut.queryForObject(query, TEST_USER_ROW_MAPPER);

        // then
        assertThat(user).isPresent();
    }

    @Test
    void 전체_조회시_List_형태의_결과를_반환한다() throws SQLException {
        // given
        final ResultSet resultSet = mock(ResultSet.class);
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true, true, false);
        given(resultSet.getString(any(Integer.class))).willReturn("hello", "world");
        final String query = "select * from users";

        // when
        final List<TestUser> result = sut.queryForList(query, TEST_USER_ROW_MAPPER);

        // then
        assertThat(result).hasSize(2);
    }
}
