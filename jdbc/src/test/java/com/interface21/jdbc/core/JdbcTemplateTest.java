package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class JdbcTemplateTest {

    // "한 행을 객체로 바꾸는 방법". JdbcTemplate을 쓰는 개발자가 작성해서 넘기는 부분이다.
    private static Crew toCrew(final ResultSet resultSet) throws SQLException {
        return new Crew(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getInt("age")
        );
    }

    private record Crew(long id, String name, int age) {
    }

    @Nested
    @DisplayName("실제 DB에 쿼리를 실행한다")
    class ExecuteOnDatabase {

        private DataSource dataSource;
        private JdbcTemplate jdbcTemplate;

        @BeforeEach
        void setUp() throws SQLException {
            dataSource = createDataSource();
            executeRaw("create table if not exists crew ("
                    + "id bigint auto_increment, "
                    + "name varchar(100) not null, "
                    + "age int not null, "
                    + "primary key(id))");
            executeRaw("truncate table crew restart identity");

            jdbcTemplate = new JdbcTemplate(dataSource);
        }

        private DataSource createDataSource() {
            final var h2DataSource = new JdbcDataSource();
            h2DataSource.setUrl("jdbc:h2:mem:jdbc-template-test;DB_CLOSE_DELAY=-1");
            h2DataSource.setUser("");
            h2DataSource.setPassword("");
            return h2DataSource;
        }

        // 검증 대상인 JdbcTemplate을 거치지 않고 DB 상태를 준비하거나 확인하기 위한 도구다.
        private void executeRaw(final String sql) throws SQLException {
            try (final var connection = dataSource.getConnection();
                 final var statement = connection.createStatement()) {
                statement.execute(sql);
            }
        }

        @Test
        @DisplayName("insert는 인자를 넘긴 순서대로 바인딩하고, 추가된 행 수를 반환한다")
        void insert() throws SQLException {
            final int affectedRows = jdbcTemplate.insert("insert into crew (name, age) values (?, ?)", "pobi", 30);

            assertThat(affectedRows).isEqualTo(1);
            assertThat(readCrew(1L)).isEqualTo("pobi,30");
        }

        private String readCrew(final long id) throws SQLException {
            try (final var connection = dataSource.getConnection();
                 final var statement = connection.createStatement();
                 final var resultSet = statement.executeQuery("select name, age from crew where id = " + id)) {
                if (!resultSet.next()) {
                    return null;
                }
                return resultSet.getString("name") + "," + resultSet.getInt("age");
            }
        }

        @Test
        @DisplayName("update는 where 절의 ?까지 넘긴 순서대로 바인딩해서, 조건에 맞는 행만 바꾼다")
        void update() throws SQLException {
            executeRaw("insert into crew (name, age) values ('pobi', 30)");
            executeRaw("insert into crew (name, age) values ('crong', 25)");

            final int affectedRows = jdbcTemplate.update("update crew set name = ?, age = ? where id = ?", "neo", 31, 2L);

            assertThat(affectedRows).isEqualTo(1);
            assertThat(readCrew(1L)).isEqualTo("pobi,30");
            assertThat(readCrew(2L)).isEqualTo("neo,31");
        }

        @Test
        @DisplayName("update는 여러 행이 바뀌면 바뀐 행 수를 그대로 반환한다")
        void updateMultipleRows() throws SQLException {
            executeRaw("insert into crew (name, age) values ('pobi', 30)");
            executeRaw("insert into crew (name, age) values ('crong', 25)");

            final int affectedRows = jdbcTemplate.update("update crew set age = ? where age < ?", 99, 100);

            assertThat(affectedRows).isEqualTo(2);
        }

        @Test
        @DisplayName("update는 조건에 맞는 행이 없으면 예외 없이 0을 반환한다")
        void updateNoRow() throws SQLException {
            executeRaw("insert into crew (name, age) values ('pobi', 30)");

            final int affectedRows = jdbcTemplate.update("update crew set name = ? where id = ?", "neo", 999L);

            assertThat(affectedRows).isZero();
            assertThat(readCrew(1L)).isEqualTo("pobi,30");
        }

        @Test
        @DisplayName("query는 조회된 모든 행을 넘겨받은 방법으로 변환해서 조회 순서대로 반환한다")
        void query() throws SQLException {
            executeRaw("insert into crew (name, age) values ('pobi', 30)");
            executeRaw("insert into crew (name, age) values ('crong', 25)");

            final List<Crew> crews = jdbcTemplate.query("select id, name, age from crew order by id", JdbcTemplateTest::toCrew);

            assertThat(crews).containsExactly(
                    new Crew(1L, "pobi", 30),
                    new Crew(2L, "crong", 25)
            );
        }

        @Test
        @DisplayName("query는 넘긴 인자를 순서대로 바인딩해서 조건에 맞는 행만 반환한다")
        void queryWithArgs() throws SQLException {
            executeRaw("insert into crew (name, age) values ('pobi', 30)");
            executeRaw("insert into crew (name, age) values ('crong', 25)");
            executeRaw("insert into crew (name, age) values ('neo', 41)");

            final List<Crew> crews = jdbcTemplate.query(
                    "select id, name, age from crew where age > ? and name <> ? order by id",
                    JdbcTemplateTest::toCrew,
                    26, "neo"
            );

            assertThat(crews).containsExactly(new Crew(1L, "pobi", 30));
        }

        @Test
        @DisplayName("query는 조회된 행이 없으면 빈 리스트를 반환한다")
        void queryNoRow() {
            final List<Crew> crews = jdbcTemplate.query("select id, name, age from crew", JdbcTemplateTest::toCrew);

            assertThat(crews).isEmpty();
        }

        @Test
        @DisplayName("query의 변환 방법은 람다로도 넘길 수 있다")
        void queryWithLambda() throws SQLException {
            executeRaw("insert into crew (name, age) values ('pobi', 30)");
            executeRaw("insert into crew (name, age) values ('crong', 25)");

            final List<String> names = jdbcTemplate.query(
                    "select name from crew order by id",
                    resultSet -> resultSet.getString("name")
            );

            assertThat(names).containsExactly("pobi", "crong");
        }

        @Test
        @DisplayName("queryForObject는 조건에 맞는 한 행을 변환해서 반환한다")
        void queryForObject() throws SQLException {
            executeRaw("insert into crew (name, age) values ('pobi', 30)");
            executeRaw("insert into crew (name, age) values ('crong', 25)");

            final Crew crew = jdbcTemplate.queryForObject(
                    "select id, name, age from crew where id = ?",
                    JdbcTemplateTest::toCrew,
                    2L
            );

            assertThat(crew).isEqualTo(new Crew(2L, "crong", 25));
        }

        @Test
        @DisplayName("queryForObject는 조건에 맞는 행이 없으면 DataAccessException을 던진다")
        void queryForObjectNoRow() throws SQLException {
            executeRaw("insert into crew (name, age) values ('pobi', 30)");

            assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                    "select id, name, age from crew where id = ?",
                    JdbcTemplateTest::toCrew,
                    999L
            )).isInstanceOf(DataAccessException.class);
        }

        @Test
        @DisplayName("queryForObject는 조건에 맞는 행이 2개 이상이면 DataAccessException을 던진다")
        void queryForObjectMultipleRows() throws SQLException {
            executeRaw("insert into crew (name, age) values ('pobi', 30)");
            executeRaw("insert into crew (name, age) values ('crong', 30)");

            assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                    "select id, name, age from crew where age = ?",
                    JdbcTemplateTest::toCrew,
                    30
            )).isInstanceOf(DataAccessException.class);
        }

        @Test
        @DisplayName("문법이 틀린 SQL을 실행하면 DB가 던진 SQLException을 원인으로 보존한 DataAccessException을 던진다")
        void invalidSql() {
            assertThatThrownBy(() -> jdbcTemplate.update("update no_such_table set name = ?", "neo"))
                    .isInstanceOf(DataAccessException.class)
                    .hasRootCauseInstanceOf(SQLException.class);
        }

        @Test
        @DisplayName("제약 조건을 위반해도 DataAccessException을 던지고, 테이블은 바뀌지 않는다")
        void constraintViolation() throws SQLException {
            assertThatThrownBy(() -> jdbcTemplate.insert("insert into crew (name, age) values (?, ?)", null, 30))
                    .isInstanceOf(DataAccessException.class)
                    .hasRootCauseInstanceOf(SQLException.class);

            assertThat(readCrew(1L)).isNull();
        }

        @Test
        @DisplayName("queryForObject는 조건에 맞는 행이 아주 많아도 전부 변환하지 않고, 두 번째 행을 확인한 시점에 예외를 던진다")
        void queryForObjectDoesNotMapAllRows() throws SQLException {
            executeRaw("insert into crew (name, age) select concat('crew', x), 20 from system_range(1, 1000)");
            final var mappedRows = new AtomicInteger();

            assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                    "select id, name, age from crew where age = ?",
                    resultSet -> {
                        mappedRows.incrementAndGet();
                        return toCrew(resultSet);
                    },
                    20
            )).isInstanceOf(DataAccessException.class);

            assertThat(mappedRows.get()).isLessThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("사용한 자원을 정리한다")
    class ReleaseResources {

        private DataSource dataSource;
        private Connection connection;
        private PreparedStatement preparedStatement;
        private ResultSet resultSet;
        private JdbcTemplate jdbcTemplate;

        @BeforeEach
        void setUp() throws SQLException {
            dataSource = mock(DataSource.class);
            connection = mock(Connection.class);
            preparedStatement = mock(PreparedStatement.class);
            resultSet = mock(ResultSet.class);

            given(dataSource.getConnection()).willReturn(connection);
            given(connection.prepareStatement(anyString())).willReturn(preparedStatement);
            given(preparedStatement.executeQuery()).willReturn(resultSet);

            jdbcTemplate = new JdbcTemplate(dataSource);
        }

        @Test
        @DisplayName("insert가 성공하면 PreparedStatement, Connection 순서로 닫는다")
        void insertClosesResources() throws SQLException {
            jdbcTemplate.insert("insert into crew (name, age) values (?, ?)", "pobi", 30);

            final var inOrder = inOrder(preparedStatement, connection);
            inOrder.verify(preparedStatement).close();
            inOrder.verify(connection).close();
        }

        @Test
        @DisplayName("update가 성공하면 PreparedStatement, Connection 순서로 닫는다")
        void updateClosesResources() throws SQLException {
            jdbcTemplate.update("update crew set name = ? where id = ?", "neo", 1L);

            final var inOrder = inOrder(preparedStatement, connection);
            inOrder.verify(preparedStatement).close();
            inOrder.verify(connection).close();
        }

        @Test
        @DisplayName("insert 실행 중 SQLException이 나면 원인을 보존한 DataAccessException으로 바꿔 던지고, 그래도 자원은 닫는다")
        void insertClosesResourcesOnFailure() throws SQLException {
            given(preparedStatement.executeUpdate()).willThrow(new SQLException("실행 실패"));

            assertThatThrownBy(() -> jdbcTemplate.insert("insert into crew (name, age) values (?, ?)", "pobi", 30))
                    .isInstanceOf(DataAccessException.class)
                    .hasRootCauseInstanceOf(SQLException.class)
                    .hasRootCauseMessage("실행 실패");

            verify(preparedStatement).close();
            verify(connection).close();
        }

        @Test
        @DisplayName("update 실행 중 SQLException이 나면 원인을 보존한 DataAccessException으로 바꿔 던지고, 그래도 자원은 닫는다")
        void updateClosesResourcesOnFailure() throws SQLException {
            given(preparedStatement.executeUpdate()).willThrow(new SQLException("실행 실패"));

            assertThatThrownBy(() -> jdbcTemplate.update("update crew set name = ? where id = ?", "neo", 1L))
                    .isInstanceOf(DataAccessException.class)
                    .hasRootCauseInstanceOf(SQLException.class)
                    .hasRootCauseMessage("실행 실패");

            verify(preparedStatement).close();
            verify(connection).close();
        }

        @Test
        @DisplayName("query가 성공하면 ResultSet, PreparedStatement, Connection 순서로 닫는다")
        void queryClosesResources() throws SQLException {
            jdbcTemplate.query("select id, name, age from crew", JdbcTemplateTest::toCrew);

            final var inOrder = inOrder(resultSet, preparedStatement, connection);
            inOrder.verify(resultSet).close();
            inOrder.verify(preparedStatement).close();
            inOrder.verify(connection).close();
        }

        @Test
        @DisplayName("queryForObject가 성공하면 ResultSet, PreparedStatement, Connection 순서로 닫는다")
        void queryForObjectClosesResources() throws SQLException {
            given(resultSet.next()).willReturn(true, false);

            jdbcTemplate.queryForObject("select name from crew where id = ?", rs -> "pobi", 1L);

            final var inOrder = inOrder(resultSet, preparedStatement, connection);
            inOrder.verify(resultSet).close();
            inOrder.verify(preparedStatement).close();
            inOrder.verify(connection).close();
        }

        @Test
        @DisplayName("query 실행 중 SQLException이 나면 원인을 보존한 DataAccessException으로 바꿔 던지고, 그래도 자원은 닫는다")
        void queryClosesResourcesOnExecuteFailure() throws SQLException {
            given(preparedStatement.executeQuery()).willThrow(new SQLException("실행 실패"));

            assertThatThrownBy(() -> jdbcTemplate.query("select id, name, age from crew", JdbcTemplateTest::toCrew))
                    .isInstanceOf(DataAccessException.class)
                    .hasRootCauseInstanceOf(SQLException.class)
                    .hasRootCauseMessage("실행 실패");

            verify(preparedStatement).close();
            verify(connection).close();
        }

        @Test
        @DisplayName("행을 변환하다 SQLException이 나도 원인을 보존한 DataAccessException으로 바꿔 던지고, ResultSet까지 전부 닫는다")
        void queryClosesResourcesOnMappingFailure() throws SQLException {
            given(resultSet.next()).willReturn(true, false);

            assertThatThrownBy(() -> jdbcTemplate.query("select id, name, age from crew", rs -> {
                throw new SQLException("변환 실패");
            }))
                    .isInstanceOf(DataAccessException.class)
                    .hasRootCauseInstanceOf(SQLException.class)
                    .hasRootCauseMessage("변환 실패");

            verify(resultSet).close();
            verify(preparedStatement).close();
            verify(connection).close();
        }

        @Test
        @DisplayName("queryForObject가 행 수 때문에 DataAccessException을 던질 때도 자원은 전부 닫는다")
        void queryForObjectClosesResourcesOnWrongRowCount() throws SQLException {
            given(resultSet.next()).willReturn(false);

            assertThatThrownBy(() -> jdbcTemplate.queryForObject("select name from crew where id = ?", rs -> "pobi", 999L))
                    .isInstanceOf(DataAccessException.class);

            verify(resultSet).close();
            verify(preparedStatement).close();
            verify(connection).close();
        }

        @Test
        @DisplayName("PreparedStatement를 만들다 SQLException이 나면 DataAccessException으로 바꿔 던지고, 이미 얻은 Connection은 닫는다")
        void closesConnectionOnPrepareFailure() throws SQLException {
            given(connection.prepareStatement(anyString())).willThrow(new SQLException("준비 실패"));

            assertThatThrownBy(() -> jdbcTemplate.update("update crew set name = ? where id = ?", "neo", 1L))
                    .isInstanceOf(DataAccessException.class)
                    .hasRootCauseInstanceOf(SQLException.class)
                    .hasRootCauseMessage("준비 실패");

            verify(connection).close();
        }

        @Test
        @DisplayName("파라미터를 바인딩하다 SQLException이 나면 DataAccessException으로 바꿔 던지고, 실행하지 않은 채 PreparedStatement, Connection 순서로 닫는다")
        void closesResourcesOnBindingFailure() throws SQLException {
            willThrow(new SQLException("바인딩 실패")).given(preparedStatement).setObject(anyInt(), any());

            assertThatThrownBy(() -> jdbcTemplate.update("update crew set name = ? where id = ?", "neo", 1L))
                    .isInstanceOf(DataAccessException.class)
                    .hasRootCauseInstanceOf(SQLException.class)
                    .hasRootCauseMessage("바인딩 실패");

            verify(preparedStatement, never()).executeUpdate();
            final var inOrder = inOrder(preparedStatement, connection);
            inOrder.verify(preparedStatement).close();
            inOrder.verify(connection).close();
        }

        @Test
        @DisplayName("Connection을 얻지 못하면 원인을 보존한 DataAccessException을 던지고, 쿼리 준비로 넘어가지 않는다")
        void connectionFailure() throws SQLException {
            given(dataSource.getConnection()).willThrow(new SQLException("연결 실패"));

            assertThatThrownBy(() -> jdbcTemplate.update("update crew set name = ? where id = ?", "neo", 1L))
                    .isInstanceOf(DataAccessException.class)
                    .hasRootCauseInstanceOf(SQLException.class)
                    .hasRootCauseMessage("연결 실패");

            verify(connection, never()).prepareStatement(anyString());
        }
    }

    // 트랜잭션처럼 호출한 쪽이 Connection의 수명을 관리하는 경우다. JdbcTemplate은 빌려 쓰기만 하고 닫지 않는다.
    @Nested
    @DisplayName("빌린 Connection으로 실행한다")
    class ExecuteWithBorrowedConnection {

        private DataSource dataSource;
        private Connection connection;
        private PreparedStatement preparedStatement;
        private JdbcTemplate jdbcTemplate;

        @BeforeEach
        void setUp() throws SQLException {
            dataSource = mock(DataSource.class);
            connection = mock(Connection.class);
            preparedStatement = mock(PreparedStatement.class);

            given(connection.prepareStatement(anyString())).willReturn(preparedStatement);

            jdbcTemplate = new JdbcTemplate(dataSource);
        }

        @Test
        @DisplayName("update는 넘겨받은 Connection으로 실행하고, DataSource에서 새 Connection을 얻지 않는다")
        void updateUsesBorrowedConnection() throws SQLException {
            given(preparedStatement.executeUpdate()).willReturn(1);

            final int affectedRows = jdbcTemplate.update(connection, "update crew set name = ? where id = ?", "neo", 1L);

            assertThat(affectedRows).isEqualTo(1);
            verify(connection).prepareStatement("update crew set name = ? where id = ?");
            verify(preparedStatement).setObject(1, "neo");
            verify(preparedStatement).setObject(2, 1L);
            verify(dataSource, never()).getConnection();
        }

        @Test
        @DisplayName("update가 성공하면 PreparedStatement는 닫지만 빌린 Connection은 닫지 않는다")
        void updateDoesNotCloseBorrowedConnection() throws SQLException {
            jdbcTemplate.update(connection, "update crew set name = ? where id = ?", "neo", 1L);

            verify(preparedStatement).close();
            verify(connection, never()).close();
        }

        @Test
        @DisplayName("update 실행 중 SQLException이 나면 DataAccessException으로 바꿔 던지고, 빌린 Connection은 닫지 않는다")
        void updateDoesNotCloseBorrowedConnectionOnFailure() throws SQLException {
            given(preparedStatement.executeUpdate()).willThrow(new SQLException("실행 실패"));

            assertThatThrownBy(() -> jdbcTemplate.update(connection, "update crew set name = ? where id = ?", "neo", 1L))
                    .isInstanceOf(DataAccessException.class)
                    .hasRootCauseInstanceOf(SQLException.class)
                    .hasRootCauseMessage("실행 실패");

            verify(preparedStatement).close();
            verify(connection, never()).close();
        }

        @Test
        @DisplayName("PreparedStatement를 만들다 SQLException이 나도 빌린 Connection은 닫지 않는다")
        void doesNotCloseBorrowedConnectionOnPrepareFailure() throws SQLException {
            given(connection.prepareStatement(anyString())).willThrow(new SQLException("준비 실패"));

            assertThatThrownBy(() -> jdbcTemplate.update(connection, "update crew set name = ? where id = ?", "neo", 1L))
                    .isInstanceOf(DataAccessException.class)
                    .hasRootCauseInstanceOf(SQLException.class)
                    .hasRootCauseMessage("준비 실패");

            verify(connection, never()).close();
        }

        @Test
        @DisplayName("같은 Connection을 넘기면 여러 번 update해도 그 Connection 하나로 실행한다")
        void multipleUpdatesShareBorrowedConnection() throws SQLException {
            jdbcTemplate.update(connection, "update crew set name = ? where id = ?", "neo", 1L);
            jdbcTemplate.update(connection, "update crew set age = ? where id = ?", 31, 1L);

            verify(connection).prepareStatement("update crew set name = ? where id = ?");
            verify(connection).prepareStatement("update crew set age = ? where id = ?");
            verify(connection, never()).close();
            verify(dataSource, never()).getConnection();
        }
    }
}
