package nextstep.jdbc;

import nextstep.exception.ColumnMappingException;
import nextstep.util.LinkedCaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Map;
import java.util.Objects;

public class ColumnMapRowMapper implements RowMapper<Map<String, Object>> {

    private final Logger log = LoggerFactory.getLogger(ColumnMapRowMapper.class);

    @Override
    public Map<String, Object> mapRow(ResultSet rs, int rowNum) {
        try {
            final ResultSetMetaData rsmd = rs.getMetaData();
            final int columnCount = rsmd.getColumnCount();
            final Map<String, Object> mapOfColumnValues = new LinkedCaseInsensitiveMap<>(columnCount);
            for (int idx = 1; idx <= columnCount; idx++) {
                final String columnName = lookupColumnName(rsmd, idx);
                mapOfColumnValues.putIfAbsent(columnName, getColumnValue(rs, idx));
            }
            return mapOfColumnValues;
        } catch (SQLException e) {
            log.info("ColumnMap을 만드는데 실패했습니다. {} {}", e.getMessage(), e);
            throw new ColumnMappingException("ColumnMap을 만드는데 실패했습니다. " + e.getMessage(), e.getCause());
        }
    }

    private String lookupColumnName(ResultSetMetaData rsmd, int idx) throws SQLException {
        String column = rsmd.getColumnLabel(idx);
        if (!hasLength(column)) {
            column = rsmd.getColumnName(idx);
        }
        return column;
    }

    private boolean hasLength(String column) {
        return Objects.nonNull(column) && !column.isEmpty();
    }

    private Object getColumnValue(ResultSet rs, int idx) {
        try {
            Object obj = rs.getObject(idx);
            String className = null;
            if (obj != null) {
                className = obj.getClass().getName();
            }
            if (obj instanceof Blob) {
                Blob blob = (Blob) obj;
                obj = blob.getBytes(1, (int) blob.length());
            } else if (obj instanceof Clob) {
                Clob clob = (Clob) obj;
                obj = clob.getSubString(1, (int) clob.length());
            } else if ("oracle.sql.TIMESTAMP".equals(className) || "oracle.sql.TIMESTAMPTZ".equals(className)) {
                obj = rs.getTimestamp(idx);
            } else if (className != null && className.startsWith("oracle.sql.DATE")) {
                String metaDataClassName = rs.getMetaData().getColumnClassName(idx);
                if ("java.sql.Timestamp".equals(metaDataClassName) || "oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
                    obj = rs.getTimestamp(idx);
                } else {
                    obj = rs.getDate(idx);
                }
            } else if (obj instanceof java.sql.Date) {
                if ("java.sql.Timestamp".equals(rs.getMetaData().getColumnClassName(idx))) {
                    obj = rs.getTimestamp(idx);
                }
            }
            return obj;
        } catch (SQLException e) {
            log.info("Column의 값을 가져오는데 실패했습니다. {} {}", e.getMessage(), e);
            throw new ColumnMappingException("Column의 값을 가져오는데 실패했습니다. " + e.getMessage(), e.getCause());
        }
    }
}
