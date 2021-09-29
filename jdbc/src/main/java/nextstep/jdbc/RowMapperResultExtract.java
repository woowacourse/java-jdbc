package nextstep.jdbc;

import nextstep.jdbc.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RowMapperResultExtract<T> {

    private static final Logger log = LoggerFactory.getLogger(RowMapperResultExtract.class);

    private final RowMapper<T> rowMapper;

    public RowMapperResultExtract(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public List<T> execute(PreparedStatement preparedStatement) {
        try (ResultSet rs = preparedStatement.executeQuery()) {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            log.error("executeQuery Data Access Failed!!", e);
            throw new DataAccessException("executeQuery Data Access Failed!!");
        }
    }
}
