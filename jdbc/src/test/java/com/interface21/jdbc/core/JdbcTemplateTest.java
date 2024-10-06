package com.interface21.jdbc.core;

import static com.interface21.jdbc.core.fixture.UserFixture.DORA;
import static com.interface21.jdbc.core.fixture.UserFixture.GUGU;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.jdbc.core.fixture.User;
import com.interface21.jdbc.core.fixture.UserRowMapper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class JdbcTemplateTest {

    JdbcTemplate jdbcTemplate;
    @Mock
    DataSource dataSource = mock(DataSource.class);
    @Mock
    Connection connection = mock(Connection.class);
    @Mock
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    @Mock
    ResultSet resultSet = mock(ResultSet.class);
    @Mock
    UserRowMapper rowMapper = mock(UserRowMapper.class);

    @BeforeEach
    void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Nested
    class Query {
        @Test
        void 데이터_여러개를_조회할_수_있다() throws SQLException {
            // given
            String sql = "SELECT * FROM users";
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, true, false);
            when(rowMapper.mapRow(resultSet)).thenReturn(GUGU.user(), DORA.user());

            // when
            List<User> results = jdbcTemplate.query(sql, rowMapper);

            // then
            assertAll(
                    () -> assertThat(results).hasSize(2),
                    () -> assertThat(results).containsExactly(GUGU.user(), DORA.user()),
                    () -> verify(dataSource).getConnection(),
                    () -> verify(connection).prepareStatement(sql),
                    () -> verify(preparedStatement).executeQuery(),
                    () -> verify(resultSet, times(3)).next(),
                    () -> verify(rowMapper, times(2)).mapRow(resultSet),
                    () -> verify(resultSet).close(),
                    () -> verify(preparedStatement).close(),
                    () -> verify(connection).close()
            );
        }

        @Test
        void 데이터가_없을_땐_빈_리스트를_반환한다() throws SQLException {
            // given
            String sql = "SELECT * FROM users";
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);

            // when
            List<User> results = jdbcTemplate.query(sql, rowMapper);

            // then
            assertAll(
                    () -> assertThat(results).hasSize(0),
                    () -> verify(dataSource).getConnection(),
                    () -> verify(connection).prepareStatement(sql),
                    () -> verify(preparedStatement).executeQuery(),
                    () -> verify(resultSet).next(),
                    () -> verify(resultSet).close(),
                    () -> verify(preparedStatement).close(),
                    () -> verify(connection).close()
            );
        }
    }

    @Nested
    class Update {
        @Test
        void 데이터를_업데이트할_수_있다() {
            
        }
    }
}
