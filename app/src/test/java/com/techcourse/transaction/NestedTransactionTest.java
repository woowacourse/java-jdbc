package com.techcourse.transaction;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.service.AppUserService;
import com.techcourse.service.TxUserService;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class NestedTransactionTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private UserHistoryDao userHistoryDao;
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        final DataSource dataSource = DataSourceConfig.getInstance();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(dataSource);

        DatabasePopulatorUtils.execute(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
        userHistoryDao = new UserHistoryDao(dataSource);
        transactionTemplate = new TransactionTemplate(dataSource);
    }

    @Test
    @DisplayName("transaction안에서 transaction을 호출하는 경우 기존 transaction에 합류하도록 구현")
    void nestedCaseTest() {
        //given
        final AppUserService appUserService = new AppUserService(userDao, userHistoryDao);
        final TxUserService txUserService = new TxUserService(appUserService, transactionTemplate);
        final Long id = 1L;
        final String newPassword = "newPassword";
        final String createdBy = "hong";

        //when
        assertThatThrownBy(() -> transactionTemplate.execute(connection -> {
            txUserService.changePassword(id, newPassword, createdBy);
            throw new RuntimeException();
        }));

        //then
        final User user = userDao.findById(id);
        assertThat(user.getPassword())
                .isNotEqualTo(newPassword);
    }
}
