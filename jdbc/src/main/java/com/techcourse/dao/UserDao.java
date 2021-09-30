package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;

public class UserDao {

	private final JdbcTemplate jdbcTemplate;

	public UserDao(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public void insert(User user) {
		String sql = "insert into users (account, password, email) values (?, ?, ?)";
		jdbcTemplate.executeQuery(sql, user.getAccount(), user.getPassword(), user.getEmail());
	}

	public void update(User user) {
		String sql = "update users set password = ? where id = ?";
		jdbcTemplate.executeQuery(sql, user.getPassword(), user.getId());
	}

	public List<User> findAll() {
		String sql = "select id, account, password, email from users";
		return jdbcTemplate.query(sql, (ResultSet resultSet) ->
			new User(
				resultSet.getLong(1),
				resultSet.getString(2),
				resultSet.getString(3),
				resultSet.getString(4)
			));
	}

	public User findById(Long id) {
		String sql = "select id, account, password, email from users where id = ?";
		return jdbcTemplate.queryForObject(sql, (ResultSet resultSet) ->
			new User(
				resultSet.getLong(1),
				resultSet.getString(2),
				resultSet.getString(3),
				resultSet.getString(4)
			), id);
	}

	public User findByAccount(String account) {
		String sql = "select id, account, password, email from users where account = ?";
		return jdbcTemplate.queryForObject(sql, (ResultSet resultSet) ->
			new User(
				resultSet.getLong(1),
				resultSet.getString(2),
				resultSet.getString(3),
				resultSet.getString(4)
			), account);
	}
}
