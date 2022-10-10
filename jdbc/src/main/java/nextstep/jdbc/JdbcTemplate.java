package nextstep.jdbc;

import static nextstep.jdbc.Extractor.extractData;
import static nextstep.jdbc.JdbcTemplateUtils.setValues;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

	private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

	private final DataSource dataSource;

	public JdbcTemplate(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public int update(final String sql, final Object... objects) {
		final StatementCallback<Integer> statementCallback = PreparedStatement::executeUpdate;
		return execute(sql, statementCallback, objects);
	}

	private <T> T execute(final String sql, final StatementCallback<T> statementCallback, final Object... objects) {
		try (final Connection connection = DataSourceUtils.getConnection(dataSource);
			 final PreparedStatement statement = connection.prepareStatement(sql)) {
			return statementCallback.doInStatement(setValues(statement, objects));
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new DataAccessException(e.getMessage(), e);
		}
	}

	public <T> T queryForObject(final String sql, final Class<T> type, final Object... objects) {
		final StatementCallback<List<T>> statementCallback = statement -> extractData(type,
			statement.executeQuery());
		return JdbcTemplateUtils.singleResult(execute(sql, statementCallback, objects));
	}

	public <T> List<T> queryForList(final String sql, final Class<T> type, final Object... objects) {
		final StatementCallback<List<T>> statementCallback = statement -> extractData(type,
			statement.executeQuery());
		return execute(sql, statementCallback, objects);
	}

	public DataSource getDataSource() {
		return dataSource;
	}
}
