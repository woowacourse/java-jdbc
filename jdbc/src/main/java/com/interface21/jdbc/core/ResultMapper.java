package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResultMapper {

	public <T> Optional<T> findResult(final ResultSet resultSet, final RowMapper<T> rowMapper) {
		if (existsNext(resultSet)) {
			return Optional.ofNullable(getResult(resultSet, rowMapper, 1));
		}
		return Optional.empty();
	}

	public <T> List<T> getResults(final ResultSet resultSet, final RowMapper<T> rowMapper) {
		int rowNum = 1;
		List<T> results = new ArrayList<>();

		while (existsNext(resultSet)) {
			results.add(getResult(resultSet, rowMapper, rowNum++));
		}

		return results;
	}

	private boolean existsNext(final ResultSet resultSet) {
		try {
			return resultSet.next();
		} catch (Exception e) {
			throw new IllegalStateException("ResultSet을 가져오는 중 에러가 발생했습니다.");
		}
	}

	public <T> T getResult(final ResultSet resultSet, final RowMapper<T> rowMapper, final int rowNum) {
		try {
			return rowMapper.mapRow(resultSet, rowNum);
		} catch (SQLException e) {
			throw new IllegalStateException("ResultSet을 가져오는 중 에러가 발생했습니다.");
		}
	}
}
