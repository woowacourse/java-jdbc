package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import nextstep.jdbc.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
    private final UserService userService = new UserService(jdbcTemplate);

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        final User user = new User("gugu", "password", "hkkang@woowahan.com");
        userService.insert(user);
    }

    @Test
    void test() {
        final var jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        final var userService = new UserService(jdbcTemplate);

        final var user = userService.findById(1L);
        System.out.println("######## " + user);
        user.changePassword("qqqqq");

        try {
            userService.edit(user, "hkkang");
        } catch (Exception e) {}


        System.out.println("######## " + userService.findById(1L));
    }
}
