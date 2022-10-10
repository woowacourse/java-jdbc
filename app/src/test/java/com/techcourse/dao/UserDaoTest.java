package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

import nextstep.jdbc.JdbcTemplate;

class UserDaoTest {

	private static UserDao userDao;

	@BeforeAll
	static void setUp() {
		DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
		userDao = new UserDao(new JdbcTemplate(DataSourceConfig.getInstance()));
		userDao.insert(new User("조시", "password", "조시@바보"));
	}

	@Test
	void findAll() {
		final var users = userDao.findAll();
		for (User user : users) {
			System.out.println(user.getAccount());
		}
	}

	@Test
	void findById() {
		final var user = userDao.findById(1L);
		assertThat(user.getAccount()).isEqualTo("조시");
	}

	@Test
	void findByAccount() {
		final var account = "조시";
		final var user = userDao.findByAccount(account);

		assertThat(user.getAccount()).isEqualTo(account);
	}

	@Test
	void insert() {
		final var account = "insert-조시";
		final var user = new User(account, "password", "hkkang@woowahan.com");
		userDao.insert(user);

		final var actual = userDao.findById(2L);

		assertThat(actual.getAccount()).isEqualTo(account);
	}

	@Test
	void update() {
		final var newPassword = "password99";
		final var user = userDao.findById(1L);
		user.changePassword(newPassword);

		userDao.update(user);

		final var actual = userDao.findById(1L);

		assertThat(actual.getPassword()).isEqualTo(newPassword);
	}
}
