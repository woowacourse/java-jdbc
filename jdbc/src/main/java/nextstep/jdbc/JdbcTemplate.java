package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public <T> T queryForObject(String sql, Object[] parameters, RowMapper<T> rowMapper) {
        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            for (int i = 1; i <= parameters.length; i++) {
                pstmt.setObject(i, parameters[i - 1]);
            }

            ResultSet rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            if (!rs.next()) {
                throw new RuntimeException("쿼리 결과가 존재하지 않습니다.");
            }

            T result = rowMapper.mapRow(rs, 1);

            if (rs.next()) {
                throw new RuntimeException("쿼리 결과가 2개 이상입니다.");
            }

            return result;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
