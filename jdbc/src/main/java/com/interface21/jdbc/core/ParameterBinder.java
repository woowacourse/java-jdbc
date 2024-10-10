package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import com.interface21.dao.DataAccessException;

public class ParameterBinder {

	public void bindParameters(PreparedStatement preparedStatement, Object... args) {
		AtomicInteger index = new AtomicInteger(1);
		Arrays.stream(args)
			.forEach(arg -> bindParameter(preparedStatement, index.getAndIncrement(), arg));
	}

	private void bindParameter(PreparedStatement preparedStatement, int index, Object arg) {
		try {
			preparedStatement.setObject(index, arg);
		} catch (SQLException e) {
			throw new DataAccessException("파라미터 값이 잘못 되었습니다.");
		}
	}
}
