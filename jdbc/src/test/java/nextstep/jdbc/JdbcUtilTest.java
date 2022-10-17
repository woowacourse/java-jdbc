package nextstep.jdbc;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import nextstep.jdbc.callback.ExecuteCallback;
import nextstep.jdbc.callback.RowMapperCallback;

class JdbcUtilTest {

	private JdbcUtil jdbcUtil;

	private Connection connection;
	private PreparedStatement statement;
	private ResultSet resultSet;

	private String sql;
	private ExecuteCallback executeCallback;
	private RowMapperCallback rowMapperCallback;

	@BeforeEach
	void init() throws SQLException {
		DataSource dataSource = mock(DataSource.class);
		jdbcUtil = new JdbcUtil(dataSource);

		connection = mock(Connection.class);
		statement = mock(PreparedStatement.class);
		resultSet = mock(ResultSet.class);

		executeCallback = mock(ExecuteCallback.class);
		sql = "select * from user";

		given(dataSource.getConnection())
			.willReturn(connection);

		given(connection.prepareStatement(anyString()))
			.willReturn(statement);
	}

	@DisplayName("Connection, Statement 자원 관리를 하며 callback들을 실행한다.")
	@Test
	void execute() {
		// given
		rowMapperCallback = mock(RowMapperCallback.class);

		// when
		jdbcUtil.execute(sql, executeCallback, rowMapperCallback);

		// then
		assertAll(
			() -> verify(executeCallback).execute(statement),
			() -> verify(rowMapperCallback).map(any()),

			() -> verify(statement).close(),
			() -> verify(resultSet, never()).close()
		);
	}

	@DisplayName("ResultSet 자원 관리를 하며 callback들을 실행한다.")
	@Test
	void execute_result() throws SQLException {
		// given
		given(statement.executeQuery())
			.willReturn(resultSet);
		given(resultSet.next())
			.willReturn(true);
		given(resultSet.getString(1))
			.willReturn("result");

		rowMapperCallback = rs -> {
			if (rs.next()) {
				return rs.getString(1);
			}
			return null;
		};

		// when
		String result = (String)jdbcUtil.execute(sql, PreparedStatement::executeQuery, rowMapperCallback);

		// then
		assertAll(
			() -> assertThat(result).isEqualTo("result"),

			() -> verify(statement).close(),
			() -> verify(resultSet).close()
		);
	}
}
