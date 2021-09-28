package nextstep.jdbc;

import nextstep.exception.BadSqlGrammarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentPreparedStatementSetter implements PreparedStatementSetter {

    private final Logger log = LoggerFactory.getLogger(ArgumentPreparedStatementSetter.class);

    @Nullable
    private final Object[] args;

    public ArgumentPreparedStatementSetter(@Nullable Object[] args) {
        this.args = args;
    }

    @Override
    public void setValue(PreparedStatement ps) {
        if (this.args != null) {
            for (int idx = 0; idx < this.args.length; idx++) {
                Object arg = this.args[idx];
                doSetValue(ps, idx + 1, arg);
            }
        }
    }

    private void doSetValue(PreparedStatement ps, int parameterPosition, Object argValue) {
        try {
            if (argValue instanceof String) {
                ps.setString(parameterPosition, (String) argValue);
            }
            if (argValue instanceof Long) {
                ps.setLong(parameterPosition, (Long) argValue);
            }
            if (argValue instanceof Integer) {
                ps.setInt(parameterPosition, (Integer) argValue);
            }
        } catch (SQLException e) {
            log.info("SQL문의 매개변수 마커와 일치하지 않습니다. 입력 순서: {}, 입력 값: {}", parameterPosition, argValue);
            throw new BadSqlGrammarException(
                    String.format("SQL문의 매개변수 마커와 일치하지 않습니다. 입력 순서: %d, 입력 값: %s", parameterPosition, argValue ),
                    e.getCause()
            );
        }
    }
}
