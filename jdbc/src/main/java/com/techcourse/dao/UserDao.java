package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;

public class UserDao {

	private static final RowMapper<User> ROW_MAPPER = (resultSet) -> {
		long id = resultSet.getLong("id");
		String account = resultSet.getString("account");
		String password = resultSet.getString("password");
		String email = resultSet.getString("email");
		return new User(id, account, password, email);
	};

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
		return jdbcTemplate.query(sql, ROW_MAPPER);
	}

	public User findById(Long id) {
		String sql = "select id, account, password, email from users where id = ?";
		return jdbcTemplate.queryForObject(sql, ROW_MAPPER, id);
	}

	public User findByAccount(String account) {
		String sql = "select id, account, password, email from users where account = ?";
		return jdbcTemplate.queryForObject(sql, ROW_MAPPER, account);
	}
}
