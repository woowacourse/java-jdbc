package nextstep.jdbc.resultset;

import nextstep.exception.resultset.ResultSetExtractFailureException;
import nextstep.jdbc.mapper.ObjectMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetExtractor<T> {

    private final ObjectMapper<T> objectMapper;

    public ResultSetExtractor(ObjectMapper<T> objectMapper) {
        this.objectMapper = objectMapper;
    }

    public T extractSingleObject(ResultSet resultSet) throws SQLException {
        List<T> list = extractList(resultSet);
        if (list.isEmpty()) {
            throw new ResultSetExtractFailureException("ResultSet 이 비어있어 객체를 추출하는데 실패했습니다.");
        }
        if (list.size() > 1) {
            throw new ResultSetExtractFailureException("ResultSet 의 결과가 하나 이상입니다.");
        }
        return list.get(0);
    }

    public List<T> extractList(ResultSet resultSet) throws SQLException {
        List<T> list = new ArrayList<>();
        while (resultSet.next()) {
            list.add(objectMapper.mapObject(resultSet));
        }
        return list;
    }
}
