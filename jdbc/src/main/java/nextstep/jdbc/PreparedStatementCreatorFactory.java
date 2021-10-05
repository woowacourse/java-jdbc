package nextstep.jdbc;

import java.sql.PreparedStatement;

public class PreparedStatementCreatorFactory {

    public static PreparedStatementCreator create(String sql, Object... args) {
        return connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
            return preparedStatement;
        };
    }

    private PreparedStatementCreatorFactory() {
    }
}
