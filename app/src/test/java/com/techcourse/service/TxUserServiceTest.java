package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TxUserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testTransactionRollback() throws SQLException {
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final var userService = new TxUserService(appUserService);

        final var newPassword = "newPassword";

        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, "gugu"));

        // then - users 테이블 롤백 확인
        final var userDaoactual = userService.findById(1L);
        assertThat(userDaoactual.getPassword()).isNotEqualTo(newPassword);

        // then - user_history 테이블 롤백 확인
        Connection conn = DataSourceConfig.getInstance().getConnection();
        PreparedStatement pstmt = conn.prepareStatement("select * from user_history");
        ResultSet userHistory = pstmt.getResultSet();
        conn.close();
        pstmt.close();
        assertThat(userHistory).isNull();
    }
}
