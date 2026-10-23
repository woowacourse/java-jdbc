package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private static final String SELECT_SQL = "select account from users where id = ?";
    private static final String SELECT_ALL_SQL = "select account from users";
    private static final String INSERT_SQL = "insert into users (account) values (?)";
    private static final String UPDATE_SQL = "update users set password = ? where id = ?";
    private static final RowMapper<String> ACCOUNT_ROW_MAPPER = (rs, rowNum) -> rs.getString("account");

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final var dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void 목록_조회_결과가_없으면_빈_리스트를_반환한다() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        final var actual = jdbcTemplate.query(SELECT_ALL_SQL, ACCOUNT_ROW_MAPPER);

        assertThat(actual).isEmpty();
    }

    @Test
    void 목록_조회는_모든_행을_순서대로_매핑한다() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "other-gugu");
        final RowMapper<String> rowNumAndAccountMapper = (rs, rowNum) -> rowNum + ":" + rs.getString("account");

        final var actual = jdbcTemplate.query(SELECT_ALL_SQL, rowNumAndAccountMapper);

        assertThat(actual).containsExactly("0:gugu", "1:other-gugu");
    }

    @Test
    void 갱신은_파라미터를_순서대로_바인딩하고_영향받은_행_수를_반환한다() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        final var actual = jdbcTemplate.update(UPDATE_SQL, "password99", 1L);

        final var inOrder = inOrder(preparedStatement);
        inOrder.verify(preparedStatement).setObject(1, "password99");
        inOrder.verify(preparedStatement).setObject(2, 1L);
        inOrder.verify(preparedStatement).executeUpdate();
        assertThat(actual).isEqualTo(1);
    }

    @Test
    void 갱신은_PreparedStatementSetter로_직접_값을_설정할_수_있다() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        final var actual = jdbcTemplate.update(UPDATE_SQL, pstmt -> {
            pstmt.setString(1, "password99");
            pstmt.setNull(2, Types.BIGINT);
        });

        final var inOrder = inOrder(preparedStatement);
        inOrder.verify(preparedStatement).setString(1, "password99");
        inOrder.verify(preparedStatement).setNull(2, Types.BIGINT);
        inOrder.verify(preparedStatement).executeUpdate();
        verify(preparedStatement, never()).setObject(anyInt(), any());
        assertThat(actual).isEqualTo(1);
    }

    @Test
    void 목록_조회는_PreparedStatementSetter로_직접_값을_설정할_수_있다() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var actual = jdbcTemplate.query(SELECT_SQL, ACCOUNT_ROW_MAPPER, pstmt -> pstmt.setLong(1, 1L));

        final var inOrder = inOrder(preparedStatement);
        inOrder.verify(preparedStatement).setLong(1, 1L);
        inOrder.verify(preparedStatement).executeQuery();
        assertThat(actual).containsExactly("gugu");
    }

    @Test
    void 단건_조회_결과가_없으면_빈_Optional을_반환한다() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        final var actual = jdbcTemplate.queryForOptional(SELECT_SQL, ACCOUNT_ROW_MAPPER, 1L);

        assertThat(actual).isEmpty();
    }

    @Test
    void 단건_조회_결과가_1건이면_값을_담은_Optional을_반환한다() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var actual = jdbcTemplate.queryForOptional(SELECT_SQL, ACCOUNT_ROW_MAPPER, 1L);

        assertThat(actual).contains("gugu");
    }

    @Test
    void 단건_조회_결과가_2건_이상이면_IncorrectResultSizeDataAccessException이_발생한다() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "gugu");

        assertThatThrownBy(() -> jdbcTemplate.queryForOptional(SELECT_SQL, ACCOUNT_ROW_MAPPER, 1L))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class)
                .satisfies(e -> assertThat(((IncorrectResultSizeDataAccessException) e).getActualSize()).isEqualTo(2));
    }

    @Test
    void SQLException이_발생하면_실행한_SQL을_담은_DataAccessException으로_변환한다() throws SQLException {
        final var sqlException = new SQLException("syntax error");
        when(connection.prepareStatement(anyString())).thenThrow(sqlException);

        assertThatThrownBy(() -> jdbcTemplate.update(INSERT_SQL, "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(INSERT_SQL)
                .hasCause(sqlException);
    }

    @Test
    void 예외가_발생해도_자원을_닫는다() throws SQLException {
        when(resultSet.next()).thenThrow(new SQLException("connection lost"));

        assertThatThrownBy(() -> jdbcTemplate.queryForOptional(SELECT_SQL, ACCOUNT_ROW_MAPPER, 1L))
                .isInstanceOf(DataAccessException.class);

        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void DataSource가_null이면_생성할_수_없다() {
        assertThatThrownBy(() -> new JdbcTemplate(null))
                .isInstanceOf(NullPointerException.class);
    }
}
