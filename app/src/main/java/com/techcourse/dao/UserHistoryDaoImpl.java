package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHistoryDaoImpl implements UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDaoImpl.class);

    private final DataSource dataSource;

    public UserHistoryDaoImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            setParams(
                    pstmt,
                    userHistory.getUserId(),
                    userHistory.getAccount(),
                    userHistory.getPassword(),
                    userHistory.getEmail(),
                    userHistory.getCreatedAt(),
                    userHistory.getCreateBy()
            );
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setParams(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
