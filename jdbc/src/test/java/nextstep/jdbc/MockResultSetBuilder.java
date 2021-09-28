package nextstep.jdbc;

import nextstep.jdbc.ImplementedMethodThrowsSQLErrorException;
import org.mockito.Mockito;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.Mockito.doAnswer;

public abstract class MockResultSetBuilder {
    protected final ResultSet mockResultSet;
    protected final Object[][] data;
    protected int currentRow;

    protected MockResultSetBuilder(Object[][] data) {
        this.data = data;
        this.currentRow = -1;
        this.mockResultSet = Mockito.mock(ResultSet.class);
        mockNextMethod(data);
        try {
            stubbingRowMapping();
        } catch (SQLException e) {
            throw new ImplementedMethodThrowsSQLErrorException();
        }
    }

    private void mockNextMethod(Object[][] data) {
        try {
            doAnswer(invocation -> {
                if (currentRow < data.length - 1) {
                    currentRow++;
                    return true;
                }
                return false;
            }).when(mockResultSet).next();
        } catch (SQLException e) {
            throw new IllegalArgumentException();
        }
    }

    public abstract void stubbingRowMapping() throws SQLException;

    public ResultSet build() {
        return this.mockResultSet;
    }
}
