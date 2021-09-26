package nextstep.jdbc;

import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import utils.MockResultSet;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import static nextstep.jdbc.TestEntity.rowMapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class JdbcTemplateTest {

    @DisplayName("query, queryForObject를 테스트한다.")
    @Nested
    class TestQueryForObject {
        @DisplayName("단일 조회 쿼리를 시행한다")
        @Test
        void queryForObject() throws SQLException {
            String selectQuery = "select * from testEntity where name = ?";

            DataSource mockSource = mock(DataSource.class);

            TestEntity expectedEntities = new TestEntity(25, "ecsimsw");
            mockQueryResults(mockSource, selectQuery, expectedEntities);

            JdbcTemplate jdbcTemplate = new JdbcTemplate(mockSource);
            TestEntity result = jdbcTemplate.queryForObject(selectQuery, rowMapper, expectedEntities.name);
            assertThat(expectedEntities).isEqualTo(result);
        }

        @DisplayName("조회 결과가 1개가 아닌 경우 예외를 반환한다")
        @Test
        void queryForObjectWithInvalidSize() throws SQLException {
            String selectQuery = "select * from testEntity where age = ?";

            DataSource mockSource = mock(DataSource.class);

            TestEntity[] expectedEntities = new TestEntity[]{
                    new TestEntity(25, "ecsimsw"),
                    new TestEntity(25, "jinhwan")
            };
            mockQueryResults(mockSource, selectQuery, expectedEntities);

            JdbcTemplate jdbcTemplate = new JdbcTemplate(mockSource);
            assertThatThrownBy(() -> jdbcTemplate.queryForObject(selectQuery, rowMapper, 25))
                    .isInstanceOf(IncorrectResultSizeDataAccessException.class);
        }

        @DisplayName("다중 조회 쿼리를 수행한다")
        @Test
        void query() throws SQLException {
            String selectQuery = "select * from testEntity where age = 25";
            DataSource mockSource = mock(DataSource.class);

            TestEntity[] expectedEntities = new TestEntity[]{
                    new TestEntity(25, "ecsimsw"),
                    new TestEntity(25, "jinhwan")
            };

            mockQueryResults(mockSource, selectQuery, expectedEntities);

            JdbcTemplate jdbcTemplate = new JdbcTemplate(mockSource);
            List<TestEntity> results = jdbcTemplate.query(selectQuery, rowMapper, 25);
            assertThat(results.toArray()).isEqualTo(expectedEntities);
        }

        private void mockQueryResults(DataSource source, String query, TestEntity... results) throws SQLException {
            PreparedStatement preparedStatement = mockPreparedStatement(source, query);
            ResultSet resultSet = mockTestEntityResults(results);
            Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
        }
    }

    @DisplayName("update를 테스트한다")
    @Nested
    class TestUpdate {

        @DisplayName("update 쿼리 호출시 수정된 row의 수를 반환한다.")
        @Test
        void update() throws SQLException {
            String selectQuery = "update testEntity SET name = hi WHERE age = ?";
            DataSource mockSource = mock(DataSource.class);

            TestEntity[] updatedEntities = new TestEntity[]{
                    new TestEntity(25, "ecsimsw"),
                    new TestEntity(25, "jinhwan")
            };

            mockUpdateResults(mockSource, selectQuery, updatedEntities);

            JdbcTemplate jdbcTemplate = new JdbcTemplate(mockSource);
            int result = jdbcTemplate.update(selectQuery, rowMapper, 25);
            assertThat(result).isEqualTo(updatedEntities.length);
        }

        private void mockUpdateResults(DataSource mockSource, String selectQuery, TestEntity... updated) throws SQLException {
            PreparedStatement preparedStatement = mockPreparedStatement(mockSource, selectQuery);
            Mockito.when(preparedStatement.executeUpdate()).thenReturn(updated.length);
        }
    }

    private PreparedStatement mockPreparedStatement(DataSource mockSource, String selectQuery) throws SQLException {
        Connection connection = mock(Connection.class);
        Mockito.when(mockSource.getConnection()).thenReturn(connection);

        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        Mockito.when(connection.prepareStatement(selectQuery)).thenReturn(preparedStatement);
        return preparedStatement;
    }

    private ResultSet mockTestEntityResults(TestEntity... expectedEntities) throws SQLException {
        final String[] columnNames = {"age", "name"};
        final Object[][] objects = new Object[expectedEntities.length][columnNames.length];
        for (int i = 0; i < expectedEntities.length; i++) {
            objects[i][0] = expectedEntities[i].age;
            objects[i][1] = expectedEntities[i].name;
        }
        return MockResultSet.create(columnNames, objects);
    }
}

class TestEntity {
    int age;
    String name;

    public static final RowMapper<TestEntity> rowMapper = rs -> new TestEntity(
            rs.getInt("age"),
            rs.getString("name")
    );

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
