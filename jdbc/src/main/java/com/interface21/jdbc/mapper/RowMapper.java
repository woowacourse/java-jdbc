package com.interface21.jdbc.mapper;

import java.sql.ResultSet;

public interface RowMapper {

    <T> T map(ResultSet rs, Class<T> clazz) throws Exception;
}
