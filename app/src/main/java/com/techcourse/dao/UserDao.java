package com.techcourse.dao;

import java.util.List;

import javax.sql.DataSource;

import com.techcourse.domain.User;

import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;

public class UserDao {

	private static final RowMapper<User> USER_MAPPER = (rs -> new User(
		rs.getLong("id"),
		rs.getString("account"),
		rs.getString("password"),
		rs.getString("email")
	));

	private final JdbcTemplate jdbcTemplate;

	public UserDao(final DataSource dataSource) {
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
		return jdbcTemplate.createQuery("select id, account, password, email from users")
			.executeQuery()
			.getResultList(USER_MAPPER);
	}

	public User findById(final Long id) {
		return jdbcTemplate.createQuery("select id, account, password, email from users where id = ?")
			.setLong(1, id)
			.executeQuery()
			.getResult(USER_MAPPER);
	}

	public User findByAccount(final String account) {
		return jdbcTemplate.createQuery("select id, account, password, email from users where account = ?")
			.setString(1, account)
			.executeQuery()
			.getResult(USER_MAPPER);
	}
}
