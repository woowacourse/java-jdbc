package nextstep.jdbc.functional.executor;

public class QueryExecuteResult {

    private static final int QUERY_FAIL = -1;
    private final int effectedRow;

    public QueryExecuteResult(int effectedRow) {
        this.effectedRow = effectedRow;
    }

    public boolean isSuccess() {
        return effectedRow != QUERY_FAIL;
    }

    public int effectedRow() {
        return effectedRow;
    }
}
