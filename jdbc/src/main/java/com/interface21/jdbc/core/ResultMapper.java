package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResultMapper {

	public <T> Optional<T> findResult(ResultSet resultSet, RowMapper<T> rowMapper) {
		if (existsNext(resultSet)) {
			return Optional.ofNullable(getResult(resultSet, rowMapper, 1));
		}
		return Optional.empty();
	}

	public <T> List<T> getResults(ResultSet resultSet, RowMapper<T> rowMapper) {
		int rowNum = 1;
		List<T> results = new ArrayList<>();

		while (existsNext(resultSet)) {
			results.add(getResult(resultSet, rowMapper, rowNum++));
		}

		return results;
	}

	private boolean existsNext(ResultSet resultSet) {
		try {
			return resultSet.next();
		} catch (Exception e) {
			throw new IllegalStateException("결과 매핑 과정에서 실패했습니다.", e);
		}
	}

	public <T> T getResult(ResultSet resultSet, RowMapper<T> rowMapper, int rowNum) {
		try {
			return rowMapper.mapRow(resultSet, rowNum);
		} catch (SQLException e) {
			throw new IllegalStateException("결과 매핑 과정에서 실패했습니다.", e);
		}
	}
}
