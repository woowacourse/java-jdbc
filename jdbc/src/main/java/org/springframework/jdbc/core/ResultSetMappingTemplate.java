package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetMappingTemplate {

    private static final Logger log = LoggerFactory.getLogger(ResultSetMappingTemplate.class);

    public <T> T mappingOne(final ResultSet rs, final RowMapper<T> rowMapper) {
        try {
            if (rs.next()) {
                return rowMapper.toObject(rs);
            }

            return null;
        } catch (SQLException e) {
            log.warn("조회된 값을 객체에 매핑하던 도중에 오류가 발생하였습니다.", e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> mapping(final ResultSet rs, final RowMapper<T> rowMapper) {
        try {
            final List<T> queriedData = new ArrayList<>();
            while (rs.next()) {
                queriedData.add(rowMapper.toObject(rs));
            }

            return queriedData;
        } catch (SQLException e) {
            log.warn("조회된 값을 객체에 매핑하던 도중에 오류가 발생하였습니다.", e);
            throw new RuntimeException(e);
        }
    }
}
