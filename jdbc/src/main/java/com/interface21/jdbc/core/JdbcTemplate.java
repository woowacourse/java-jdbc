package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.dao.DataAccessException;

public class JdbcTemplate {

	private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

	private final DataSource dataSource;

	public JdbcTemplate(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public int update(String sql, PreparedStatementSetter pss) {
		try {
			return update(sql, pss, dataSource.getConnection());
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new DataAccessException("Failed to get Connection", e);
		}
	}

	public int update(String sql, PreparedStatementSetter pss, Connection conn) {
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pss.setValues(pstmt);

			return pstmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new DataAccessException("Failed to execute Query = " + sql, e);
		}
	}

	public <T> T queryForObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
		try (Connection conn = dataSource.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pss.setValues(pstmt);

			return fetchSingleResult(rowMapper, pstmt);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new DataAccessException("Failed to execute Query = " + sql, e);
		}
	}

	public <T> T queryForObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss, Connection conn) {
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pss.setValues(pstmt);

			return fetchSingleResult(rowMapper, pstmt);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new DataAccessException("Failed to execute Query = " + sql, e);
		}
	}

	public <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
		try (Connection conn = dataSource.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pss.setValues(pstmt);

			return fetchResults(rowMapper, pstmt);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new DataAccessException("Failed to execute Query = " + sql, e);
		}
	}

	public <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss, Connection conn) {
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pss.setValues(pstmt);

			return fetchResults(rowMapper, pstmt);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new DataAccessException("Failed to execute Query = " + sql, e);
		}
	}

	private <T> T fetchSingleResult(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
		List<T> results = fetchResults(rowMapper, pstmt);
		if (results.isEmpty()) {
			throw new DataAccessException("No result found");
		}
		if (results.size() > 1) {
			throw new DataAccessException("More than One result found. Expected 1 but found " + results.size());
		}
		return results.getFirst();
	}

	private <T> List<T> fetchResults(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
		try (ResultSet rs = pstmt.executeQuery()) {
			List<T> results = new ArrayList<>();
			while (rs.next()) {
				results.add(rowMapper.mapRow(rs, rs.getRow()));
			}
			return results;
		}
	}
}
