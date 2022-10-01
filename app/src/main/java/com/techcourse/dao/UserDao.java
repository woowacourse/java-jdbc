package com.techcourse.dao;

import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.techcourse.domain.User;

import nextstep.jdbc.JdbcTemplate;

public class UserDao {

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
			.getResultList(User.class);
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
