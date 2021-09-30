package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

	private static final Logger LOGGER = LoggerFactory.getLogger(JdbcTemplate.class);

	private final DataSource dataSource;

	public JdbcTemplate(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void executeQuery(String sql, Object... values) {
		try (Connection connection = dataSource.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			setValues(preparedStatement, values);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			throw new InvalidDataException(e.getMessage());
		}
	}

	private void setValues(PreparedStatement preparedStatement, Object[] values)
		throws SQLException {
		for (int i = 0; i < values.length; i++) {
			preparedStatement.setObject(i + 1, values[i]);
		}
	}

	public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... values) {
		try (Connection connection = dataSource.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			if (Objects.nonNull(values)) {
				setValues(preparedStatement, values);
			}

			List<T> result = new ArrayList<>();

			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				result.add(rowMapper.mapRow(resultSet));
			}

			return result;
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			throw new InvalidDataException(e.getMessage());
		}
	}

	public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... values) {
		List<T> result = query(sql, rowMapper, values);

		if (result.isEmpty()) {
			String message = "존재하지 않는 데이터입니다.";
			LOGGER.error(message);
			throw new InvalidDataException(message);
		}

		if (result.size() > 1) {
			String message = "두 개 이상의 데이터가 존재합니다.";
			LOGGER.error(message);
			throw new InvalidDataException(message);
		}

		return result.get(0);
	}
}
