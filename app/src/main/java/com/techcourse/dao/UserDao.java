package com.techcourse.dao;

import java.util.List;

import com.techcourse.domain.User;

import nextstep.jdbc.JdbcTemplate;

public class UserDao {

	private final JdbcTemplate jdbcTemplate;

	public UserDao(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void insert(final User user) {
		final var sql = "insert into users (account, password, email) values (?, ?, ?)";
		jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
	}

	public void update(final User user) {
		final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
		jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
	}

	public List<User> findAll() {
		final var sql = "select id, account, password, email from users";
		return jdbcTemplate.queryForList(sql, User.class);
	}

	public User findById(final Long id) {
		final var sql = "select id, account, password, email from users where id = ?";
		return jdbcTemplate.queryForObject(sql, User.class, id);
	}

	public User findByAccount(final String account) {
		final var sql = "select id, account, password, email from users where account = ?";
		return jdbcTemplate.queryForObject(sql, User.class, account);
	}
}
