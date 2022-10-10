package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final int SINGLE_RESULT = 1;
    private static final int FIRST_ELEMENT = 0;
    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return DataSourceUtils.getConnection(dataSource);
    }

    /**
     * Create, Update, Delete 시 사용할 수 있는 메서드입니다.
     *
     * @param sql    SQL문을 String 형태로 받습니다. 매개변수 위치에는 ?를 표기해주세요.
     * @param params 가변인자로 SQL에 적용될 매개변수를 받습니다. SQL문 내에 ? 표기된 곳에 순서대로 주입합니다.
     * @return 영향 받은 행의 수를 반환합니다.
     */
    public int command(final String sql, final Object... params) {
        return execute(sql, null, ((rowMapper, pstmt) -> pstmt.executeUpdate()), params);
    }

    /**
     * 여러 건을 Select 시 사용할 수 있는 메서드입니다.
     *
     * @param sql       SQL문을 String 형태로 받습니다. 매개변수 위치에는 ?를 표기해주세요.
     * @param rowMapper 하나의 행을 매핑하여 하나의 결과를 반환하는 rowMapper를 구현해서 전달해주세요.
     * @param params    가변인자로 SQL에 적용될 매개변수를 받습니다. SQL문 내에 ? 표기된 곳에 순서대로 주입합니다.
     * @param <T>       RowMapper가 하나의 행을 매핑하여 반환할 타입 파라미터 입니다.
     * @return 조회 결과를 List<T> 로 반환합니다. 결과가 없을 경우 빈 List를 반환합니다.
     * @see nextstep.jdbc.RowMapper
     */
    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return execute(sql, rowMapper, (this::mapRows), params);
    }

    /**
     * 단 건을 Select 시 사용할 수 있는 메서드입니다.
     *
     * @param sql       SQL문을 String 형태로 받습니다. 매개변수 위치에는 ?를 표기해주세요.
     * @param rowMapper 하나의 행을 매핑하여 하나의 결과를 반환하는 rowMapper를 구현해서 전달해주세요.
     * @param params    가변인자로 SQL에 적용될 매개변수를 받습니다. SQL문 내에 ? 표기된 곳에 순서대로 주입합니다.
     * @param <T>       RowMapper가 하나의 행을 매핑하여 반환할 타입 파라미터 입니다.
     * @return 조회 결과를 T로 반환합니다.
     * @throws DataAccessException 조회 결과가 없거나 2건 이상인 경우 예외를 던집니다.
     * @see nextstep.jdbc.RowMapper
     */
    public <T> T queryForOne(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        final var results = queryForList(sql, rowMapper, params);

        if (results.size() != SINGLE_RESULT) {
            throw new DataAccessException(String.format("Expected single result, but %s", results.size()));
        }

        return results.get(FIRST_ELEMENT);
    }

    private <T, R> R execute(final String sql, final RowMapper<T> rowMapper,
                             final ThrowingBiFunction<T, R> template, final Object... params) {
        try (
                final var connection = getConnection();
                final var pstmt = connection.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);
            log.debug("params : {}", params);
            setParams(pstmt, List.of(params));

            return template.apply(rowMapper, pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setParams(final PreparedStatement pstmt, final List<Object> params) throws SQLException {
        if (Objects.isNull(params)) {
            return;
        }

        for (int i = 0; i < params.size(); i++) {
            pstmt.setObject(i + 1, params.get(i));
        }
    }

    private <T> List<T> mapRows(final RowMapper<T> rowMapper, final PreparedStatement pstmt) throws SQLException {
        final var resultSet = pstmt.executeQuery();

        final var results = new ArrayList<T>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }

        return results;
    }
}
