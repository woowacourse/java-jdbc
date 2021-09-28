package nextstep.jdbc;

import java.sql.SQLException;

import static org.mockito.Mockito.doAnswer;

public class TestUserMockResultSetBuilder extends MockResultSetBuilder {
    public TestUserMockResultSetBuilder(Object[][] data) {
        super(data);
    }

    @Override
    public void stubbingRowMapping() throws SQLException {
        doAnswer(invocation -> {
            String columnLabel = invocation.getArgument(0, String.class);
            return data[currentRow][0];
        }).when(mockResultSet).getLong("id");

        doAnswer(invocation -> {
            String columnLabel = invocation.getArgument(0, String.class);
            return data[currentRow][1];
        }).when(mockResultSet).getString("account");

        doAnswer(invocation -> {
            String columnLabel = invocation.getArgument(0, String.class);
            return data[currentRow][2];
        }).when(mockResultSet).getString("password");

        doAnswer(invocation -> {
            String columnLabel = invocation.getArgument(0, String.class);
            return data[currentRow][3];
        }).when(mockResultSet).getString("email");
    }
}
