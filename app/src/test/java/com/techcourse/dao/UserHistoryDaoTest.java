package com.techcourse.dao;

import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.jdbc.core.JdbcTemplate;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserHistoryDaoTest {

    @Test
    void log() {
        final var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:history-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        DatabasePopulatorUtils.execute(dataSource);

        final var jdbcTemplate = new JdbcTemplate(dataSource);
        final var userDao = new UserDao(jdbcTemplate);

        userDao.insert(new User("moka", "password", "moka@example.com"));
        final var user = userDao.findByAccount("moka");

        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        userHistoryDao.log(new UserHistory(user, "moka"));

        final var sql = """
                select count(*) from user_history
                where user_id = ?
                  and account = ?
                  and password = ?
                  and email = ?
                  and created_at is not null
                  and created_by = ?
                """;

        final var count = jdbcTemplate.queryForObject(
                sql,
                rs -> rs.getInt(1),
                user.getId(),
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                "moka"
        );

        assertThat(count).isEqualTo(1);
    }
}
