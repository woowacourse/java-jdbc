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
	private final JdbcTemplate jdbcTemplate;

	public UserDao(final DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public void insert(final User user) {
		jdbcTemplate.createQuery("insert into users (account, password, email) values (?, ?, ?)")
			.setString(1, user.getAccount())
			.setString(2, user.getPassword())
			.setString(3, user.getEmail())
			.executeUpdate();
	}

	public void update(final User user) {
		jdbcTemplate.createQuery("update users set account = ?, password = ?, email = ? where id = ?")
			.setString(1, user.getAccount())
			.setString(2, user.getPassword())
			.setString(3, user.getEmail())
			.setLong(4, user.getId())
			.executeUpdate();
	}

	public List<User> findAll() {
		final var sql = "select id, account, password, email from users";

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			log.debug("query : {}", sql);

			List<User> users = new ArrayList<>();
			while (rs.next()) {
				users.add(new User(
					rs.getLong(1),
					rs.getString(2),
					rs.getString(3),
					rs.getString(4))
				);
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
		return jdbcTemplate.createQuery("select id, account, password, email from users where id = ?")
			.setLong(1, id)
			.executeQuery()
			.getResult(User.class);
	}

	public User findByAccount(final String account) {
		return jdbcTemplate.createQuery("select id, account, password, email from users where account = ?")
			.setString(1, account)
			.executeQuery()
			.getResult(User.class);
	}
}
