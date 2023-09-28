package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

	private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

	private final DataSource dataSource;

	public JdbcTemplate(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void update(String sql, Object... args) {
		try (Connection connection = dataSource.getConnection();
			 PreparedStatement preparedStatement = getPreparedStatement(sql, connection, args)) {
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new IllegalArgumentException(e);
		}
	}

	public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
		try (Connection connection = dataSource.getConnection();
			 PreparedStatement preparedStatement = getPreparedStatement(sql, connection, args)) {

			ResultSet resultSet = preparedStatement.executeQuery();
			List<T> result = new ArrayList<>();

			while (resultSet.next()) {
				result.add(rowMapper.mapRow(resultSet));
			}

			return result;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new IllegalArgumentException(e);
		}
	}

	public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
		try (Connection connection = dataSource.getConnection();
			 PreparedStatement preparedStatement = getPreparedStatement(sql, connection, args)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			T result = null;

			while (resultSet.next()) {
				if (result != null) {
					throw new IllegalArgumentException("1개 이상의 결과가 존재합니다.");
				}
				result = rowMapper.mapRow(resultSet);
			}

			if (result == null) {
				throw new IllegalArgumentException("결과가 존재하지 않습니다.");
			}
			return result;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new IllegalArgumentException(e);
		}
	}

	private PreparedStatement getPreparedStatement(String sql, Connection connection, Object... args) throws
		SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		for (int i = 0; i < args.length; i++) {
			preparedStatement.setObject(i + 1, args[i]);
		}
		return preparedStatement;
	}
}
