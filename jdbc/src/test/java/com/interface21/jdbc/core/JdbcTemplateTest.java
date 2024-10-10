package com.interface21.jdbc.core;

import static org.mockito.Mockito.*;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.mapper.SqlResultSetMapper;
import com.interface21.jdbc.mapper.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class JdbcTemplateTest {

    private DataSource source;
    private ResultSet resultSet;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private JdbcTemplate jdbcTemplate;
    private static MockedStatic<SqlResultSetMapper> sqlResultSetMapperMockedStatic;

    @BeforeAll
    static void beforeAll() {
        sqlResultSetMapperMockedStatic = mockStatic(SqlResultSetMapper.class);

    }

    @AfterAll
    static void afterAll() {
        sqlResultSetMapperMockedStatic.close();
    }

    @BeforeEach
    void setUp() throws SQLException {
        source = mock(DataSource.class);
        resultSet = mock(ResultSet.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);

        when(source.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(source);
    }


    @DisplayName("단건 조회를 할 수 있다.")
    @Test
    void query() throws SQLException {
        when(resultSet.isAfterLast()).thenReturn(false);
        when(resultSet.next()).thenReturn(true, false);

        Assertions.assertThatCode(() -> jdbcTemplate.query(User.class, "select * from user"))
                .doesNotThrowAnyException();
    }


    @DisplayName("단건 조회시 데이터가 없다면 null을 반환한다.")
    @Test
    void queryNull() {
        Assertions.assertThat(jdbcTemplate.query(User.class, "select * from user")).isNull();
    }

    @DisplayName("단건 조회시 데이터가 두 개 이상이라면 예외가 발생한다.")
    @Test
    void queryDuplicatedData() throws SQLException {
        String sql = "select id from users";
        when(SqlResultSetMapper.doQueryMapping(User.class, resultSet)).thenReturn(List.of(new User(),new User()));

        Assertions.assertThatThrownBy(() -> jdbcTemplate.query(User.class, sql))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("두 개 이상의 데이터가 조회되었습니다.");
    }

    @DisplayName("리스트 조회를 할 수 있다.")
    @Test
    void queryList() throws SQLException {
        when(resultSet.isAfterLast()).thenReturn(false);
        when(resultSet.next()).thenReturn(true, true, false);

        Assertions.assertThatCode(() -> jdbcTemplate.queryForAll(User.class, "select * from user"))
                .doesNotThrowAnyException();
    }

    @DisplayName("리스트로 조회시 데이터가 없다면 빈리스트가 반환된다.")
    @Test
    void queryForAllEmptyList() throws SQLException {
        String sql = "select id from users";

        when(SqlResultSetMapper.doQueryMapping(User.class, resultSet)).thenReturn(List.of());

        Assertions.assertThat(jdbcTemplate.queryForAll(User.class, sql)).isEmpty();
    }

    @DisplayName("업데이트가 메서드가 실행되는지 확인한다.")
    @Test
    void queryForAllEmptyList2() throws SQLException {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        PreparedStatement mockStatement = mock(PreparedStatement.class);

        when(source.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        jdbcTemplate.update(sql);

        verify(mockStatement).executeUpdate();

    }

}
