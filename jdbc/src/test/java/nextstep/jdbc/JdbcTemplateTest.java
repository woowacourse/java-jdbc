package nextstep.jdbc;

import static nextstep.jdbc.Fixture.비비;
import static nextstep.jdbc.Fixture.카더가든;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users (name) VALUES(?)";

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private ResultSetMetaData resultSetMetaData;

    @BeforeEach
    void setUp() throws SQLException {
        this.dataSource = mock(DataSource.class);
        this.connection = mock(Connection.class);
        this.preparedStatement = mock(PreparedStatement.class);
        this.resultSet = mock(ResultSet.class);
        this.resultSetMetaData = mock(ResultSetMetaData.class);
        this.jdbcTemplate = new JdbcTemplate(dataSource);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.getMetaData()).willReturn(resultSetMetaData);

        final var fields = User.class.getDeclaredFields();
        given(resultSetMetaData.getColumnCount()).willReturn(fields.length);
        final var fieldTypeNames = Arrays.stream(fields).peek(it -> it.setAccessible(true))
                .map(it -> it.getType().getName())
                .collect(Collectors.toList());
        final var firstValue = fieldTypeNames.get(0);
        final var secondValues = fieldTypeNames.subList(1, fields.length);
        secondValues.addAll(fieldTypeNames);
        given(resultSetMetaData.getColumnClassName(anyInt())).willReturn(firstValue,
                secondValues.toArray(String[]::new));
    }

    @Test
    void 쿼리를_실행시켜_한_개의_엔티티를_반환한다() throws SQLException {
        // given
        given(resultSet.next()).willReturn(true, false);
        given(resultSet.getObject(anyInt())).willReturn(1L, "차정원");

        // when
        final var user = jdbcTemplate.queryForObject(FIND_BY_ID_QUERY, User.class, 1L);

        // then
        assertThat(user).usingRecursiveComparison()
                .isEqualTo(카더가든);
    }

    @Test
    void 쿼리를_실행시켜_한_개의_데이터를_찾아오는데_결과가_없는_경우_예외가_발생한다() throws SQLException {
        // given
        given(resultSet.next()).willReturn(false);

        // when, then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(FIND_BY_ID_QUERY, User.class))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("일치하는 데이터가 없습니다.");
    }

    @Test
    void 쿼리를_실행시켜_한_개의_데이터를_찾아오는데_결과가_두_개_이상일_경우_예외가_발생한다() throws SQLException {
        // given
        given(resultSet.next()).willReturn(true, true, false);

        // when, then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(FIND_BY_ID_QUERY, User.class))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("조회 데이터 갯수가 2 입니다.");
    }

    @Test
    void 쿼리를_실행시켜_두_개_이상의_엔티티를_반환한다() throws SQLException {
        // given
        given(resultSet.next()).willReturn(true, true, false);
        given(resultSet.getObject(anyInt())).willReturn(1L, "차정원", 2L, "김형서");

        // when
        final var result = jdbcTemplate.query(FIND_ALL_QUERY, User.class);

        // then
        assertThat(result).usingRecursiveFieldByFieldElementComparator()
                .containsExactly(카더가든, 비비);
    }

    @Test
    void 쿼리를_실행시켜_데이터를_수정한다() throws SQLException {
        // given
        given(preparedStatement.executeUpdate()).willReturn(1);

        // when
        jdbcTemplate.execute(INSERT_QUERY, "안영윤");

        // then
        assertAll(
                () -> verify(connection).prepareStatement(INSERT_QUERY),
                () -> verify(preparedStatement).executeUpdate()
        );
    }
}
