package aop.stage0;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import aop.DataAccessException;
import aop.StubUserHistoryDao;
import aop.domain.User;
import aop.repository.UserDao;
import aop.repository.UserHistoryDao;
import aop.service.AppUserService;
import aop.service.TxUserService;
import aop.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class Stage0Test {

    private static final Logger log = LoggerFactory.getLogger(Stage0Test.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserHistoryDao userHistoryDao;

    @Autowired
    private StubUserHistoryDao stubUserHistoryDao;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @BeforeEach
    void setUp() {
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        // 실제 서비스 로직을 담당하는 AppUserService
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        // 트랜잭션 처리를 담당하는 TxUserService(데코레이터)로 감싸서 사용
        final UserService userService =
                new TxUserService(platformTransactionManager, appUserService);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        // 비밀번호 변경 실행 → 성공해야 커밋
        userService.changePassword(1L, newPassword, createBy);

        // 변경된 유저를 다시 조회
        final var actual = userService.findById(1L);

        // 비밀번호가 정상적으로 변경되었는지 확인
        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        // 비즈니스 로직은 동일하지만, UserHistoryDao 대신 Stub DAO 사용
        final var appUserService = new AppUserService(userDao, stubUserHistoryDao);
        // Stub DAO는 log() 호출 시 DataAccessException 발생
        final UserService userService =
                new TxUserService(platformTransactionManager, appUserService);

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // changePassword 실행 중 DataAccessException 발생 → TxUserService가 rollback 수행
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        // 롤백되었으므로 비밀번호는 변경되지 않음
        final var actual = userService.findById(1L);

        // 원래 비밀번호가 그대로 남아 있는지 확인
        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
