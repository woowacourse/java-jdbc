package nextstep.jdbc;

import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JdbcTemplateTest {

    private final RowMapper<TestEntity> rowMapper = rs -> new TestEntity(rs.getInt("age"), rs.getString("name"));
    private final TestEntity testEntity = new TestEntity(25, "ecsimsw");

    @DisplayName("queryForObject로 단일 조회 쿼리를 시행한다")
    @Test
    void queryForObject() throws SQLException {
        String selectQuery = "select * from user where name = ?";

        DataSource mockSource = Mockito.mock(DataSource.class);
        ResultSet resultSet = getMockResultFromDataSource(selectQuery, mockSource);

        Mockito.when(resultSet.next()).thenReturn(true).thenReturn(false);
        Mockito.when(resultSet.getInt("age")).thenReturn(testEntity.age);
        Mockito.when(resultSet.getString("name")).thenReturn(testEntity.name);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(mockSource);
        TestEntity result = jdbcTemplate.queryForObject(selectQuery, rowMapper, testEntity.name);
        assertThat(testEntity).isEqualTo(result);
    }

    @DisplayName("조회 결과가 1개가 아닌 경우 예외를 반환한다")
    @Test
    void queryForObjectWithInvalidSize() throws SQLException {
        String selectQuery = "select * from user where name = ?";

        DataSource mockSource = Mockito.mock(DataSource.class);
        ResultSet resultSet = getMockResultFromDataSource(selectQuery, mockSource);

        Mockito.when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        Mockito.when(resultSet.getInt("age")).thenReturn(testEntity.age);
        Mockito.when(resultSet.getString("name")).thenReturn(testEntity.name);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(mockSource);
        assertThatThrownBy(()-> jdbcTemplate.queryForObject(selectQuery, rowMapper, testEntity.name))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    private ResultSet getMockResultFromDataSource(String selectQuery, DataSource mockSource) throws SQLException {
        Connection connection = Mockito.mock(Connection.class);
        Mockito.when(mockSource.getConnection()).thenReturn(connection);

        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        Mockito.when(connection.prepareStatement(selectQuery)).thenReturn(preparedStatement);

        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
        return resultSet;
    }

}

class TestEntity {
    int age;
    String name;

    public TestEntity(int age, String name) {
        this.age = age;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestEntity user = (TestEntity) o;
        return age == user.age && Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(age, name);
    }
}