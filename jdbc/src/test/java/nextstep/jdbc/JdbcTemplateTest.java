package nextstep.jdbc;

import static nextstep.jdbc.TestEntity.rowMapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import utils.MockResultSet;

class JdbcTemplateTest {

    private final DataSource dataSource = Mockito.mock(DataSource.class);

    @DisplayName("사용 후 커넥션을 자동 반환한다")
    @Test
    public void closeConnectionTest() throws SQLException {
        DataSource dataSource = Mockito.mock(DataSource.class);
        Connection conn = Mockito.mock(Connection.class);
        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);

        Mockito.when(dataSource.getConnection()).thenReturn(conn);
        Mockito.when(conn.prepareStatement(any())).thenReturn(preparedStatement);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("mockQuery");

        Mockito.verify(conn).close();
        Mockito.verify(preparedStatement).close();
    }

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

            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            TestEntity result = jdbcTemplate.queryForObject(mockPreparedStatement, rowMapper, params);
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

            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            assertThatThrownBy(() -> jdbcTemplate.queryForObject(mockPreparedStatement, rowMapper, params))
                    .isInstanceOf(IncorrectResultSizeDataAccessException.class);
        }
    }

    @DisplayName("queryForObject()")
    @Nested
    class TestQuery {

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

            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            List<TestEntity> results = jdbcTemplate.query(mockPreparedStatement, rowMapper, params);
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

            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            int result = jdbcTemplate.update(mockPreparedStatement, params);
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestEntity user = (TestEntity) o;
        return age == user.age && Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(age, name);
    }
}
