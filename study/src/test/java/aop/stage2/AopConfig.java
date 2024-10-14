package aop.stage2;

import aop.StubUserHistoryDao;
import aop.repository.UserDao;
import aop.repository.UserHistoryDao;
import aop.service.AppUserService;
import aop.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class AopConfig {

    @Bean
    public UserService userService(UserDao userDao, UserHistoryDao userHistoryDao) {
        return new AppUserService(userDao, userHistoryDao);
    }

    @Bean
    public StubUserHistoryDao stubUserHistoryDao(JdbcTemplate jdbcTemplate) {
        return new StubUserHistoryDao(jdbcTemplate);
    }
}
