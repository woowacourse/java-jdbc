package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public InsertJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(User user) throws SQLException {
        final String sql = createQueryForInsert();

        Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);

        try (conn; pstmt) {
            log.debug("query : {}", sql);

            setValuesForInsert(user, pstmt);
            pstmt.executeUpdate();
        }
    }

    private String createQueryForInsert() {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        return sql;
    }

    private void setValuesForInsert(User user, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
    }

}
