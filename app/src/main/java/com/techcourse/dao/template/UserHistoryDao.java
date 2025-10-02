package com.techcourse.dao.template;

import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final InsertUserHistoryJdbcTemplate insertUserHistoryJdbcTemplate;

    public UserHistoryDao(final DataSource dataSource) {
        this.insertUserHistoryJdbcTemplate = new InsertUserHistoryJdbcTemplate(dataSource);
    }

    public void log(final UserHistory userHistory) {
        insertUserHistoryJdbcTemplate.update(userHistory);
    }
}
