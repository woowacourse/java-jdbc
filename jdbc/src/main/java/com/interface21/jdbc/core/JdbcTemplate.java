package com.interface21.jdbc.core;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryOne(String sql, ResultSetCallBack<T> callBack, Object... args) {
        log.debug("query : {}", sql);
        try (var connection = dataSource.getConnection(); var pstmt = connection.prepareStatement(sql)) {
            int index = 1;
            for (Object arg : args) {
                pstmt.setObject(index++, arg);
            }

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                T result = callBack.callback(rs);
                rs.close();
                return result;
            }

            rs.close();
        } catch (Exception ignored) {
        }

        return null;
    }

    public <T> List<T> query(String sql, ResultSetCallBack<T> callBack, Object... args) {
        log.debug("query : {}", sql);
        List<T> results = new ArrayList<>();

        try (var connection = dataSource.getConnection(); var pstmt = connection.prepareStatement(sql)) {
            int index = 1;
            for (Object arg : args) {
                pstmt.setObject(index++, arg);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(callBack.callback(rs));
            }

            rs.close();
        } catch (Exception ignored) {
        }

        return results;
    }

    public void update(String sql, PreparedStatementCallBack callBack) {
        log.debug("query : {}", sql);
        try (var connection = dataSource.getConnection(); var pstmt = connection.prepareStatement(sql)) {
            callBack.callback(pstmt);
            pstmt.executeUpdate();
        } catch (Exception ignored) {
        }
    }
}
