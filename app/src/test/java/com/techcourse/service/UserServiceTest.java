package com.techcourse.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.transaction.TransactionTemplate;

class UserServiceTest {

	private JdbcTemplate jdbcTemplate;
	private UserDao userDao;
	private TransactionTemplate transactionTemplate;

	@BeforeEach
	void setUp() {
		DataSource dataSource = DataSourceConfig.getInstance();
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.userDao = new UserDao(jdbcTemplate);
		this.transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));

		DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
		final var user = new User("gugu", "password", "hkkang@woowahan.com");
		userDao.insert(user);
	}

	@Test
	void testChangePassword() {
		final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
		final var appUserService = new AppUserService(userDao, userHistoryDao);
		final var userService = new TxUserService(appUserService, transactionTemplate);

		final var newPassword = "qqqqq";
		final var createBy = "gugu";
		userService.changePassword(1L, newPassword, createBy);

		final var actual = userService.findById(1L);

		assertThat(actual.getPassword()).isEqualTo(newPassword);
	}

	@Test
	void testTransactionRollback2() {
		final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
		final var appUserService = new AppUserService(userDao, userHistoryDao);
		final var userService = new TxUserService(appUserService, transactionTemplate);

		final var newPassword = "newPassword";
		final var createBy = "gugu";
		// 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
		assertThrows(DataAccessException.class,
			() -> userService.changePassword(1L, newPassword, createBy));

		final var actual = userService.findById(1L);

		assertThat(actual.getPassword()).isNotEqualTo(newPassword);
	}
}
