package aop.stage2;

import aop.DataAccessException;
import aop.StubUserHistoryDao;
import aop.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Stage2Test {

    private static final Logger log = LoggerFactory.getLogger(Stage2Test.class);

    @Autowired
    private UserService userService;

    @Autowired
    private StubUserHistoryDao stubUserHistoryDao;

    @BeforeEach
    void setUp() {
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userService.insert(user);
    }

    @Test
    void testChangePassword() {
        String newPassword = "qqqqq";
        String createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        userService.setUserHistoryDao(stubUserHistoryDao);

        String newPassword = "newPassword";
        String createBy = "gugu";
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
