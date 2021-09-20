package nextstep.jdbc.utils.preparestatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nextstep.jdbc.utils.preparestatement.PreparedStatementHelper.LongHelper;
import nextstep.jdbc.utils.preparestatement.PreparedStatementHelper.StringHelper;

public class PreparedStatementSetter {

    private static final List<PreparedStatementHelper> PREPARED_STATEMENT_HELPERS = new ArrayList<>();

    static {
        PREPARED_STATEMENT_HELPERS.add(new StringHelper());
        PREPARED_STATEMENT_HELPERS.add(new LongHelper());
    }

    public static void psmtSet(PreparedStatement preparedStatement, Object... args)
        throws SQLException {
        for (int i = 0; i < args.length; i++) {
            final Object targetArg = args[i];
            PREPARED_STATEMENT_HELPERS.stream()
                .filter(helpers -> helpers.isAssignable(targetArg))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("not supported arg type"))
                .setArgToPsmt(i + 1, preparedStatement, targetArg);
        }
    }
}
