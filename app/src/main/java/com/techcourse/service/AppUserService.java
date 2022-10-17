package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class AppUserService implements UserService {

	private final UserDao userDao;
	private final UserHistoryDao userHistoryDao;

	public AppUserService(UserDao userDao, UserHistoryDao userHistoryDao) {
		this.userDao = userDao;
		this.userHistoryDao = userHistoryDao;
	}

	@Override
	public User findById(long id) {
		return userDao.findById(id);
	}

	@Override
	public void insert(User user) {
		userDao.insert(user);
	}

	@Override
	public void changePassword(long id, String newPassword, String createBy) {
		final var user = findById(id);
		user.changePassword(newPassword);
		userDao.update(user);
		userHistoryDao.log(new UserHistory(user, createBy));
	}
}
