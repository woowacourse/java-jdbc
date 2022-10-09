package nextstep.jdbc;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import nextstep.jdbc.callback.ExecuteCallback;
import nextstep.jdbc.callback.RowMapperCallback;
import nextstep.jdbc.callback.StatementCallback;

public class JdbcTemplate {

	private final JdbcUtil jdbcUtil;

	public JdbcTemplate(final DataSource dataSource) {
		this.jdbcUtil = new JdbcUtil(dataSource);
	}

	public void update(final String sql, final StatementCallback statementCallback) {
		ExecuteCallback executeCallback = pstmt -> {
			statementCallback.prepare(pstmt);
			pstmt.executeUpdate();
			return null;
		};
		jdbcUtil.execute(sql, executeCallback, rs -> null);
	}

	public <T> T queryForObject(final String sql, final StatementCallback statementCallback,
		final RowMapper<T> rowMapper) {
		RowMapperCallback rowMapperCallback = rs -> {
			T result = null;
			if (rs.next()) {
				result = rowMapper.mapRow(rs);
			}
			return result;
		};
		//noinspection unchecked
		return (T)jdbcUtil.execute(sql, getStatementExecuteCallback(statementCallback), rowMapperCallback);
	}

	public <T> List<T> queryForList(final String sql, final StatementCallback statementCallback,
		final RowMapper<T> rowMapper) {
		RowMapperCallback rowMapperCallback = rs -> {
			List<T> results = new ArrayList<>();
			while (rs.next()) {
				results.add(rowMapper.mapRow(rs));
			}
			return results;
		};
		//noinspection unchecked
		return (List<T>)jdbcUtil.execute(sql, getStatementExecuteCallback(statementCallback), rowMapperCallback);
	}

	private ExecuteCallback getStatementExecuteCallback(StatementCallback statementCallback) {
		return pstmt -> {
			statementCallback.prepare(pstmt);
			return pstmt.executeQuery();
		};
	}
}
