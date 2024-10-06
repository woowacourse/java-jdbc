package com.techcourse.dao;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

class UserHistoryDaoTest {

	private UserHistoryDao userHistoryDao;

	@BeforeEach
	void setup() {
		DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

		userHistoryDao = new UserHistoryDao(DataSourceConfig.getInstance());
	}

	@Test
	void log() {
		final User user = new User(1L, "gugu", "password", "hkkang@woowahan.com");
		final String createBy = "sangdol";
		final UserHistory userHistory = new UserHistory(user, createBy);

		assertDoesNotThrow(() -> userHistoryDao.log(userHistory));
	}
}
