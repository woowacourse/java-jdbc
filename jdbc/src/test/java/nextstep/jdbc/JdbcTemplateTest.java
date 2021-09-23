package nextstep.jdbc;

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

class JdbcTemplateTest {

    @DisplayName("queryForObject로 단일 조회 쿼리를 시행한다")
    @Test
    void queryForObject() throws SQLException {
        String selectQuery = "select * from user where name = ?";

        TestEntity persistedData = new TestEntity(25, "ecsimsw");
        RowMapper<TestEntity> rowMapper = rs -> new TestEntity(rs.getInt(1), rs.getString(2));

        DataSource mockSource = Mockito.mock(DataSource.class);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(mockSource);

        Connection connection = Mockito.mock(Connection.class);
        Mockito.when(mockSource.getConnection()).thenReturn(connection);

        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        Mockito.when(connection.prepareStatement(selectQuery)).thenReturn(preparedStatement);

        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);

        Mockito.when(resultSet.next()).thenReturn(true).thenReturn(false);
        Mockito.when(resultSet.getInt(1)).thenReturn(persistedData.age);
        Mockito.when(resultSet.getString(2)).thenReturn(persistedData.name);

        TestEntity testEntity = jdbcTemplate.queryForObject(selectQuery, rowMapper, persistedData.name);
        assertThat(testEntity).isEqualTo(persistedData);
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