package nextstep.jdbc;

import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import nextstep.jdbc.exception.JdbcTemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private DataSource dataSource;

    public JdbcTemplate() {
    }

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            int index = 1;
            for (Object param : params) {
                pstmt.setObject(index++, param);
            }

            List<T> results = new ArrayList<>();
            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                results.add(mapper.mapRow(resultSet));
            }

            if (results.size() != 1) {
                throw new IncorrectResultSizeDataAccessException(1, results.size());
            }
            return results.get(0);
        } catch (SQLException sqlException) {
            log.error(sqlException.getMessage());
            throw new JdbcTemplateException();
        }
    }
}
