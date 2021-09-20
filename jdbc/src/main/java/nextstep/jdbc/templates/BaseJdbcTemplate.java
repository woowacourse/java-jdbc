package nextstep.jdbc.templates;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import nextstep.jdbc.utils.statement.StatementCallback;

public abstract class BaseJdbcTemplate {

    private final DataSource dataSource;

    public BaseJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected <T> T execute(StatementCallback<T> action) throws SQLException{
        try (Connection con = dataSource.getConnection();
            Statement stmt = con.createStatement()) {

            // TODO : 외부에서 타임아웃 설정 할 수 있게 만들기
            stmt.setQueryTimeout(30);
            return action.getResult(stmt);
        }
    }
}
