package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
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

    private static final User 카더가든 = new User(1L, "차정원");
    private static final User 비비 = new User(2L, "김형서");

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

        // 쿼리 결과 데이터가 두 개라고 가정한다
        given(resultSet.next()).willReturn(true, true, false);
        final var fields = User.class.getDeclaredFields();
        given(resultSetMetaData.getColumnCount()).willReturn(fields.length);
        final var fieldTypeNames = Arrays.stream(fields).peek(it -> it.setAccessible(true))
                .map(it -> it.getType().getName())
                .collect(Collectors.toList());
        final var firstValue = fieldTypeNames.get(0);
        final var secondValues = fieldTypeNames.subList(1, fields.length);
        secondValues.addAll(fieldTypeNames);
        secondValues.addAll(fieldTypeNames);
        given(resultSetMetaData.getColumnClassName(anyInt())).willReturn(firstValue,
                secondValues.toArray(String[]::new));
        given(resultSet.getObject(anyInt(), any(Class.class))).willReturn(1L, "차정원", 2L, "김형서");
    }

    @Test
    void 한_개의_엔티티를_반환한다() {
        // given
        final var sql = "SELECT * FROM users WHERE id = ?";

        // when
        final var user = jdbcTemplate.queryForObject(sql, User.class, 1L);

        // then
        assertThat(user).usingRecursiveComparison()
                .isEqualTo(카더가든);
    }

    @Test
    void 두_개_이상의_엔티티를_반환한다() throws SQLException {
        // given
        final var sql = "Select * FROM users";

        // when
        final var result = jdbcTemplate.query(sql, User.class);

        // then
        assertThat(result).usingRecursiveFieldByFieldElementComparator()
                .containsExactly(카더가든, 비비);
    }

    @Test
    void DB_값을_수정한다() throws SQLException {
        // given
        final var sql = "UPDATE users SET name = ? WHERE = ?";
        given(preparedStatement.executeUpdate()).willReturn(1);

        // when
        jdbcTemplate.execute(sql, "안영윤", 1L);

        // then
        assertAll(
                () -> verify(connection).prepareStatement("UPDATE users SET name = '안영윤' WHERE = 1"),
                () -> verify(preparedStatement).executeUpdate()
        );
    }

    @Test
    void 수정에_실패할_경우_예외_발생() throws SQLException {
        // given
        final var sql = "UPDATE users SET name = ? WHERE = ?";
        given(preparedStatement.executeUpdate()).willReturn(0);

        // when, then
        assertThatThrownBy(() -> jdbcTemplate.execute(sql, "안영윤", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("적용되지 않았습니다");
    }

    public static class User {
        private Long id;
        private String name;

        public User(final Long id, final String name) {
            this.id = id;
            this.name = name;
        }
    }
}
