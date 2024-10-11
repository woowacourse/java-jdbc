package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

	private JdbcTemplate jdbcTemplate;
	private PreparedStatement preparedStatement;

	@BeforeEach
	void setUp() throws SQLException {
		DataSource dataSource = mock(DataSource.class);
		Connection connection = mock(Connection.class);
		preparedStatement = mock(PreparedStatement.class);
		jdbcTemplate = new JdbcTemplate(dataSource);

		when(dataSource.getConnection()).thenReturn(connection);
		when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
	}

	@Nested
	@DisplayName("단건 조회시 결과에 따른 처리를 확인한다.")
	class queryForObjectTest {

		private ResultSet resultSet;

		@BeforeEach
		void setUp() throws SQLException {
			resultSet = mock(ResultSet.class);
			when(preparedStatement.executeQuery()).thenReturn(resultSet);
		}

		@DisplayName("단건 조회시 결과가 존재하지 않으면 예외를 던진다.")
		@Test
		void noResultFound() throws SQLException {
			String sql = "SELECT id, name FROM users WHERE id = ?";
			when(resultSet.next()).thenReturn(false);

			assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, new TestRowMapper(),
				preparedStatement -> preparedStatement.setObject(1, 1)))
				.isInstanceOf(RuntimeException.class);
		}

		@DisplayName("단건 조회시 결과가 여러 개 존재하면 예외를 던진다.")
		@Test
		void tooManyResultsFound() throws SQLException {
			String sql = "SELECT id, name FROM users WHERE id = ?";
			when(resultSet.next()).thenReturn(true, true, false);

			assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, new TestRowMapper(),
				preparedStatement -> preparedStatement.setObject(1, 1)))
				.isInstanceOf(RuntimeException.class);
		}

		@DisplayName("단건 조회시 결과가 존재하면 결과를 반환한다.")
		@Test
		void resultFound() throws SQLException {
			String sql = "SELECT id, name FROM users WHERE id = ?";
			when(resultSet.next()).thenReturn(true, false);
			when(resultSet.getString("name")).thenReturn("Tebah");

			Object result = jdbcTemplate.queryForObject(sql, new TestRowMapper(),
				preparedStatement -> preparedStatement.setObject(1, 1));

			assertThat(result).isEqualTo("hi");
		}
	}

	public static class TestRowMapper implements RowMapper<Object> {

		@Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			return "hi";
		}
	}
}
