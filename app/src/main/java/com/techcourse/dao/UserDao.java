package com.techcourse.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.techcourse.domain.User;

import nextstep.jdbc.JdbcTemplate;

public class UserDao {

	private static final Logger log = LoggerFactory.getLogger(UserDao.class);

	private final DataSource dataSource;
	private final InsertJdbcTemplate insertJdbcTemplate;
	private final UpdateJdbcTemplate updateJdbcTemplate;

	public UserDao(final DataSource dataSource, InsertJdbcTemplate insertJdbcTemplate,
		UpdateJdbcTemplate updateJdbcTemplate) {
		this.dataSource = dataSource;
		this.insertJdbcTemplate = insertJdbcTemplate;
		this.updateJdbcTemplate = updateJdbcTemplate;
	}

	public UserDao(final JdbcTemplate jdbcTemplate, InsertJdbcTemplate insertJdbcTemplate,
		UpdateJdbcTemplate updateJdbcTemplate) {
		this.insertJdbcTemplate = insertJdbcTemplate;
		this.updateJdbcTemplate = updateJdbcTemplate;
		this.dataSource = null;
	}

	public void insert(final User user) {
		insertJdbcTemplate.insert(user);
	}

	public void update(final User user) {
		updateJdbcTemplate.update(user);
	}

	public List<User> findAll() {
		List<User> users = new ArrayList<>();
		final var sql = "select id, account, password, email from users";

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			log.debug("query : {}", sql);

			if (rs.next()) {
				users.add(new User(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4)));
			}
			return users;
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException ignored) {
			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (SQLException ignored) {
			}

			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ignored) {
			}
		}
	}

	public User findById(final Long id) {
		final var sql = "select id, account, password, email from users where id = ?";

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, id);
			rs = pstmt.executeQuery();

			log.debug("query : {}", sql);

			if (rs.next()) {
				return new User(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4));
			}
			return null;
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException ignored) {
			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (SQLException ignored) {
			}

			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ignored) {
			}
		}
	}

	public User findByAccount(final String account) {
		final var sql = "select id, account, password, email from users where account = ?";

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, account);
			rs = pstmt.executeQuery();

			log.debug("query : {}", sql);

			if (rs.next()) {
				return new User(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4));
			}
			return null;
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException ignored) {
			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (SQLException ignored) {
			}

			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ignored) {
			}
		}
	}
}
