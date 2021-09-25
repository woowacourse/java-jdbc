package com.techcourse.repository;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;

public class UserDaoRepository {

    public static final UserDao USER_DAO = new UserDao(DataSourceConfig.getInstance());

    private UserDaoRepository() {
    }
}
