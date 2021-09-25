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
import nextstep.jdbc.exception.SimpleJdbcException;
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
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s", table));
            ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            List<String> names = new ArrayList<>();

            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                names.add(columnName.toLowerCase());
            }

            return names;
        } catch (Exception e) {
            throw new SimpleJdbcException(String.format("초기화 중 에러가 발생했습니다. : %s", e.getMessage()));
        }
    }

    public Object executeAndReturnKey(Map<String, String> parameters) {
        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(convertToSql(parameters), Statement.RETURN_GENERATED_KEYS)
        ) {
            preparedStatement.executeUpdate();
            return convertKey(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
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
                throw new SimpleJdbcException(String.format("테이블에 컬럼이 존재하지 않습니다. [%s]", columnName));
            }
        }

        return String.format("insert into %s %s values %s", table, columnNames, columnValues);
    }

    private Object convertKey(PreparedStatement preparedStatement) {
        try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getObject(keyColumn);
            } else {
                throw new SimpleJdbcException(String.format("테이블에 Key로 지정된 컬럼이 존재하지 않습니다. [%s]", keyColumn));
            }
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }
}
