package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Parameters {

	private final Map<Integer, Object> parameters;

	public Parameters() {
		this.parameters = new HashMap<>();
	}

	public void add(int index, Object value) {
		parameters.put(index, value);
	}

	public void setPreparedStatement(PreparedStatement pstmt) throws SQLException {
		for(Map.Entry<Integer, Object> entry : parameters.entrySet()) {
			if(entry.getValue() instanceof String) {
				pstmt.setString(entry.getKey(), (String) entry.getValue());
			} else if(entry.getValue() instanceof Long) {
				pstmt.setLong(entry.getKey(), (Long) entry.getValue());
			}
		}
	}
}
