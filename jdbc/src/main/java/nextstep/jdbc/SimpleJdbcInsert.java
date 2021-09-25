package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleJdbcInsert {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleJdbcInsert.class);

    private final DataSource dataSource;
    private final String table;
    private final String keyColumn;
    private final List<String> tableColumns;

    public SimpleJdbcInsert(DataSource dataSource, String table, String keyColumn) {
        this.dataSource = dataSource;
        this.table = table;
        this.keyColumn = keyColumn;
        this.tableColumns = getTableColumns(this.dataSource);
    }

    private List<String> getTableColumns(DataSource dataSource) {
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(String.format("select * from %s", table));
            ResultSet rs = pstmt.executeQuery()
        ) {
            ResultSetMetaData metaData = rs.getMetaData();
            List<String> names = new ArrayList<>();

            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                names.add(columnName.toLowerCase());
            }

            return names;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public Object executeAndReturnKey(Map<String, String> parameters) {
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(convertToSql(parameters), Statement.RETURN_GENERATED_KEYS)
        ) {
            pstmt.executeUpdate();
            return convertKey(pstmt);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String convertToSql(Map<String, String> parameters) {
        StringJoiner columnNames = new StringJoiner(", ", "(", ")");
        StringJoiner columnValues = new StringJoiner(", ", "(", ")");

        for (Entry<String, String> entry : parameters.entrySet()) {
            String columnName = entry.getKey();
            String columnValue = entry.getValue().toLowerCase();

            if (tableColumns.contains(columnName)) {
                columnNames.add(columnName);
                columnValues.add(String.format("'%s'", columnValue));
            } else {
                throw new DataAccessException("안맞아!");
            }
        }

        return String.format("insert into %s %s values %s", table, columnNames, columnValues);
    }

    private Object convertKey(PreparedStatement pstmt) {
        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getObject(keyColumn);
            } else {
                throw new DataAccessException("");
            }
        } catch (SQLException e){
            throw new DataAccessException("");
        }
    }
}
