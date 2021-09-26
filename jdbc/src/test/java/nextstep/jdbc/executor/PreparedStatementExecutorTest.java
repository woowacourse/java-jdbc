package nextstep.jdbc.executor;

import nextstep.jdbc.RowMapper;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import utils.MockResultSet;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import static nextstep.jdbc.executor.TestEntity.rowMapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class PreparedStatementExecutorTest {

    @DisplayName("query()")
    @Nested
    class TestQueryForObject {

        @DisplayName("단일 조회 쿼리를 시행한다")
        @Test
        void queryForObject() throws SQLException {
            PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
            Object[] params = new Object[0];

            TestEntity expectedResult = new TestEntity(25, "ecsimsw");

            ResultSet resultSet = mockTestEntityResults(expectedResult);
            Mockito.when(mockPreparedStatement.executeQuery()).thenReturn(resultSet);

            TestEntity result = PreparedStatementExecutor.queryForObject(mockPreparedStatement, params, rowMapper);
            assertThat(result).isEqualTo(expectedResult);
        }

        @DisplayName("조회 결과가 1개가 아닌 경우 예외를 반환한다")
        @Test
        void queryForObjectWithInvalidSize() throws SQLException {
            PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
            Object[] params = new Object[0];

            TestEntity[] expectedResults = new TestEntity[]{
                    new TestEntity(25, "ecsimsw"),
                    new TestEntity(25, "jinhwan")
            };

            ResultSet resultSet = mockTestEntityResults(expectedResults);
            Mockito.when(mockPreparedStatement.executeQuery()).thenReturn(resultSet);

            assertThatThrownBy(() -> PreparedStatementExecutor.queryForObject(mockPreparedStatement, params, rowMapper))
                    .isInstanceOf(IncorrectResultSizeDataAccessException.class);
        }
    }

    @DisplayName("queryForObject()")
    @Nested
    class TestQuery{

        @DisplayName("다중 조회 쿼리를 수행한다")
        @Test
        void query() throws SQLException {
            PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
            Object[] params = new Object[0];

            TestEntity[] expectedResults = new TestEntity[]{
                    new TestEntity(25, "ecsimsw"),
                    new TestEntity(25, "jinhwan")
            };

            ResultSet resultSet = mockTestEntityResults(expectedResults);
            Mockito.when(mockPreparedStatement.executeQuery()).thenReturn(resultSet);

            List<TestEntity> results = PreparedStatementExecutor.query(mockPreparedStatement, params, rowMapper);
            assertThat(results.toArray()).isEqualTo(expectedResults);
        }
    }

    @DisplayName("update()")
    @Nested
    class TestUpdate {

        @DisplayName("update 쿼리 호출시 수정된 row의 수를 반환한다.")
        @Test
        void update() throws SQLException {
            PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
            Object[] params = new Object[0];

            int expectedUpdatedSize = 1;
            Mockito.when(mockPreparedStatement.executeUpdate()).thenReturn(expectedUpdatedSize);

            int result = PreparedStatementExecutor.update(mockPreparedStatement, params);
            assertThat(result).isEqualTo(expectedUpdatedSize);
        }
    }

    private ResultSet mockTestEntityResults(TestEntity... entities) throws SQLException {
        final String[] columnNames = {"age", "name"};
        final Object[][] objects = new Object[entities.length][columnNames.length];
        for (int i = 0; i < entities.length; i++) {
            objects[i][0] = entities[i].age;
            objects[i][1] = entities[i].name;
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
