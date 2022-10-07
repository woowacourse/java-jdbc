package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = Mockito.mock(DataSource.class);
        connection = Mockito.mock(Connection.class);
        statement = Mockito.mock(PreparedStatement.class);
        resultSet = Mockito.mock(ResultSet.class);

        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        Mockito.when(statement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    void queryForObject() throws SQLException {
        final String sql = "select * from member where name=m1";

        Mockito.when(connection.prepareStatement(sql)).thenReturn(statement);
        Mockito.when(resultSet.next()).thenReturn(true).thenReturn(false);
        Mockito.when(resultSet.getString(1)).thenReturn("m1");

        this.jdbcTemplate = new JdbcTemplate(dataSource);
        Member member = jdbcTemplate.queryForObject(sql,
                (rSet, rowNum) -> new Member(rSet.getString(1)));

        assertThat(member.getName()).isEqualTo("m1");
    }

    @Test
    void query() throws SQLException {
        final String sql = "select * from member";
        Mockito.when(connection.prepareStatement(sql)).thenReturn(statement);
        Mockito.when(resultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        Mockito.when(resultSet.getString(1))
                .thenReturn("m1")
                .thenReturn("m2");

        this.jdbcTemplate = new JdbcTemplate(dataSource);
        List<Member> members = jdbcTemplate.query(sql,
                (rSet, rowNum) -> new Member(rSet.getString(1)));

        assertAll(
                () -> assertThat(members.get(0).getName()).isEqualTo("m1"),
                () -> assertThat(members.get(1).getName()).isEqualTo("m2")
        );
    }

    @Test
    @DisplayName("Connection이 성공적으로 종료되는지 테스트한다.")
    void connection() throws SQLException {
        final String sql = "select * from member";
        Mockito.when(connection.prepareStatement(sql)).thenReturn(statement);
        Mockito.when(resultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        Mockito.when(resultSet.getString(1))
                .thenReturn("m1")
                .thenReturn("m2");

        this.jdbcTemplate = new JdbcTemplate(dataSource);
        List<Member> members = jdbcTemplate.query(sql,
                (rSet, rowNum) -> new Member(rSet.getString(1)));

        Mockito.verify(connection).close();
        Mockito.verify(resultSet).close();
        Mockito.verify(statement).close();
    }

    static class Member {
        private final String name;

        Member(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}