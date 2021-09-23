package nextstep.jdbc;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentPreparedStatementSetter implements PreparedStatementSetter {

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
        } catch (SQLException exception) {
            throw new IllegalArgumentException(
                    String.format("SQL문의 매개변수 마커와 일치하지 않습니다. 입력 순서: %d, 입력 값: %s", parameterPosition, args ));
        }
    }
}
