package aop;

import aop.domain.UserHistory;
import aop.repository.UserHistoryDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StubUserHistoryDao extends UserHistoryDao {

    public StubUserHistoryDao(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void log(UserHistory userHistory) {
        throw new DataAccessException();
    }
}
