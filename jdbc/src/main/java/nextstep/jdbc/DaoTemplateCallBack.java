package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DaoTemplateCallBack {

    private static final Logger log = LoggerFactory.getLogger(DaoTemplateCallBack.class);

    private final DataSource dataSource;

    public DaoTemplateCallBack(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DaoTemplateCallBack(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
    }

    protected void execute(final String sql, final ThrowingConsumer<PreparedStatement, SQLException> pstmtConsumer) {
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            pstmtConsumer.accept(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    protected <RET> RET queryOneLong(final String sql, final Long deliminator, final ThrowingBiFunction<PreparedStatement, ResultSet, RET, SQLException> pstmtFunction) {
        ResultSet rs = null;
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setLong(1, deliminator);
            rs = pstmt.executeQuery();
            log.debug("query : {}", sql);
            return pstmtFunction.apply(pstmt, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected <RET> RET queryOneString(final String sql, final String deliminator, final ThrowingBiFunction<PreparedStatement, ResultSet, RET, SQLException> pstmtFunction) {
        ResultSet rs = null;
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setString(1, deliminator);
            rs = pstmt.executeQuery();
            log.debug("query : {}", sql);
            return pstmtFunction.apply(pstmt, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected <RET> List<RET> queryAll(final String sql, final ThrowingBiFunction<PreparedStatement, ResultSet, List<RET>, SQLException> pstmtFunction) {
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql);
             final var rs = pstmt.executeQuery()
        ) {
            log.debug("query : {}", sql);
            return pstmtFunction.apply(pstmt, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
