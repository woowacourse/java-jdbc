package nextstep.jdbc.support;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.RowMapper;

public class DataAccessUtils {

    private static final String INCORRECT_RESULT_SIZE_MESSAGE = "Incorrect result size: expected 1, actual %d";

    private DataAccessUtils() {
    }

    public static <T> List<T> getResults(final RowMapper<T> rowMapper, final ResultSet rs)
            throws SQLException {
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.mapRow(rs, rs.getRow()));
        }
        return result;
    }

    public static <T> T getSingleResult(final List<T> result) {
        int size = result.size();
        if (size != 1) {
            throw new DataAccessException(String.format(INCORRECT_RESULT_SIZE_MESSAGE, size));
        }
        return result.get(0);
    }
}
