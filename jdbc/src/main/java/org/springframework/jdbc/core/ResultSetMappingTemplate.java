package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetMappingTemplate {

    private static final Logger log = LoggerFactory.getLogger(ResultSetMappingTemplate.class);

    public <T> List<T> mapping(final PreparedStatement pstmt, final RowMapper<T> rowMapper) {
        try {
            final ResultSet rs = pstmt.executeQuery();
            final List<T> queriedData = new ArrayList<>();
            while (rs.next()) {
                queriedData.add(rowMapper.toObject(rs));
            }

            return queriedData;
        } catch (SQLException e) {
            log.warn("조회된 값을 객체에 매핑하던 도중에 오류가 발생하였습니다.", e);
            throw new DataAccessException(e);
        }
    }
}
