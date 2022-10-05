package nextstep.jdbc;

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

	public void update(final String sql, final StatementCallback callback) {
		try (Connection conn = dataSource.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			callback.prepare(pstmt);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new DataAccessException(e);
		}
	}

	public <T> T queryForObject(final String sql, final StatementCallback callback, final RowMapper<T> rowMapper) {
		T result;
		try (Connection conn = dataSource.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			callback.prepare(pstmt);
			result = extractResult(rowMapper, pstmt);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new DataAccessException(e);
		}
		return result;
	}

	private <T> T extractResult(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
		try (ResultSet rs = pstmt.executeQuery()) {
			if (rs.next()) {
				return rowMapper.mapRow(rs);
			}
		}
		return null;
	}

	public <T> List<T> queryForList(final String sql, final StatementCallback callback, final RowMapper<T> rowMapper) {
		List<T> results = new ArrayList<>();
		try (Connection conn = dataSource.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			callback.prepare(pstmt);
			extractResultList(rowMapper, results, pstmt);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new DataAccessException(e);
		}
		return results;
	}

	private <T> void extractResultList(RowMapper<T> rowMapper, List<T> results, PreparedStatement pstmt)
		throws SQLException {
		try (ResultSet rs = pstmt.executeQuery()) {
			while (rs.next()) {
				results.add(rowMapper.mapRow(rs));
			}
		}
	}
}
