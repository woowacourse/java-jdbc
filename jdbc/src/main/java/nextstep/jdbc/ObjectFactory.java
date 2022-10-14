package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ObjectFactory<T> {

    private final RowMapper<T> rowMapper;

    public ObjectFactory(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public List<T> build(ResultSet resultSet) {
        try {
            List<T> objects = new ArrayList<>();
            while (resultSet.next()) {
                objects.add(rowMapper.makeObject(resultSet));
            }
            return objects;
        } catch (SQLException e) {
            throw new IllegalStateException("Object Factory: fail to build object");
        }
    }
}
