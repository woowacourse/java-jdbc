package com.techcourse.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.techcourse.domain.User;

import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;

public class InsertJdbcTemplate {

	private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

	private final DataSource dataSource;

	public InsertJdbcTemplate(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	private String createQueryForInsert() {
		return "insert into users (account, password, email) values (?, ?, ?)";
	}

	public void insert(final User user) {
		try (final Connection connection = dataSource.getConnection();
			 final PreparedStatement pstmt = connection.prepareStatement(createQueryForInsert())) {
			pstmt.setString(1, user.getAccount());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getEmail());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new DataAccessException(e.getMessage(), e);
		}
	}
}
