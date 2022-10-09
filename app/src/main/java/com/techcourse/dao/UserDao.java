package com.techcourse.dao;

import java.util.List;

import javax.sql.DataSource;

import com.techcourse.domain.User;

import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import nextstep.jdbc.StatementCallback;

public class UserDao {

	private static final RowMapper<User> USER_MAPPER = (rs -> new User(
		rs.getLong("id"),
		rs.getString("account"),
		rs.getString("password"),
		rs.getString("email")
	));

	private final JdbcTemplate jdbcTemplate;

	public UserDao(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void insert(final User user) {
		String sql = "insert into users (account, password, email) values (?, ?, ?)";
		StatementCallback callback = pstmt -> {
			pstmt.setString(1, user.getAccount());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getEmail());
		};
		jdbcTemplate.update(sql, callback);
	}

	public void update(final User user) {
		String sql = "update users set account = ?, password = ?, email = ? where id = ?";
		StatementCallback callback = pstmt -> {
			pstmt.setString(1, user.getAccount());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getEmail());
			pstmt.setLong(4, user.getId());
		};
		jdbcTemplate.update(sql, callback);
	}

	public User findById(final Long id) {
		String sql = "select id, account, password, email from users where id = ?";
		StatementCallback callback = pstmt -> pstmt.setLong(1, id);
		return jdbcTemplate.queryForObject(sql, callback, USER_MAPPER);
	}

	public User findByAccount(final String account) {
		String sql = "select id, account, password, email from users where account = ?";
		StatementCallback callback = pstmt -> pstmt.setString(1, account);
		return jdbcTemplate.queryForObject(sql, callback, USER_MAPPER);
	}

	public List<User> findAll() {
		String sql = "select id, account, password, email from users";
		return jdbcTemplate.queryForList(sql, pstmt -> {}, USER_MAPPER);
	}
}
