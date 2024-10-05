package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(InsertJdbcTemplate.class);

    private final DataSource dataSource;

    public InsertJdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(final User user) {
        final var sql = createQueryForInsert();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            setValuesForInsert(user, pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String createQueryForInsert() {
        return "insert into users (account, password, email) values (?, ?, ?)";
    }

    private void setValuesForInsert(User user, PreparedStatement pstmt) throws SQLException {
        pstmt.setObject(1, user.getAccount());
        pstmt.setObject(2, user.getPassword());
        pstmt.setObject(3, user.getEmail());
    }
}
