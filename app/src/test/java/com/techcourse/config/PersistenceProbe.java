package com.techcourse.config;

import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

public class PersistenceProbe {

    public static void main(final String[] args) {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        final var dao = new UserDao(DataSourceConfig.getInstance());
        if (args[0].equals("write")) {
            dao.insert(new User("persistent-user", "secret", "persistent@example.com"));
        } else {
            final var user = dao.findByAccount("persistent-user");
            if (user == null || !user.getEmail().equals("persistent@example.com")
                    || !user.getPassword().equals("secret") || dao.findAll().size() != 1) {
                throw new AssertionError("재시작 후 사용자 데이터가 보존되지 않았습니다.");
            }
        }
    }
}
