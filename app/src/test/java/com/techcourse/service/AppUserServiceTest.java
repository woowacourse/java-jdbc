package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppUserServiceTest {

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
    void changePassword() throws SQLException {
        // given
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userDao = new UserDao(jdbcTemplate);
        final var userService = new AppUserService(userDao, userHistoryDao);

        // when
        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        // then - user 테이블 확인
        final var actual = userService.findById(1L);
        assertThat(actual.getPassword()).isEqualTo(newPassword);

        // then - user_history 테이블 확인
        Connection conn = DataSourceConfig.getInstance().getConnection();
        PreparedStatement pstmt = conn.prepareStatement("select password from user_history");
        ResultSet rs = pstmt.executeQuery();
        rs.next();
        String historyActual = rs.getString(1);
        conn.close();
        pstmt.close();
        assertThat(historyActual).isEqualTo(newPassword);
    }
}
