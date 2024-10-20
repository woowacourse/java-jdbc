package aop.stage2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import aop.DataAccessException;
import aop.StubUserHistoryDao;
import aop.domain.User;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import transaction.stage1.jdbc.JdbcTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Stage2Test {

    @Autowired
    private UserService userService;

    @Autowired
    private StubUserHistoryDao stubUserHistoryDao;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("DELETE FROM users;");
        jdbcTemplate.update("ALTER TABLE users AUTO_INCREMENT=1;");
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userService.insert(user);
    }

    @Test
    void testChangePassword() {
        String newPassword = "newPassword";
        String createdBy = "gugu";
        userService.changePassword(1L, newPassword, createdBy);

        User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        userService.setUserHistoryDao(stubUserHistoryDao);

        String newPassword = "newPassword";
        String createdBy = "gugu";
        assertThatThrownBy(() -> userService.changePassword(1L, newPassword, createdBy))
                .isInstanceOf(DataAccessException.class);
        User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
