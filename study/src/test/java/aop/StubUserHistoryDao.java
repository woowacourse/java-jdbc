package aop;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import aop.domain.UserHistory;
import aop.repository.UserHistoryDao;

@Primary
@Repository
public class StubUserHistoryDao extends UserHistoryDao {

    public StubUserHistoryDao(final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void log(final UserHistory userHistory) {
        throw new DataAccessException();
    }
}
