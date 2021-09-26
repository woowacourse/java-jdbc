package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String query, Object... querySubject) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(query);
            setValue(preparedStatement, querySubject);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            checkNullStateOfCompositions(connection, preparedStatement);
        }
    }

    public <T> List<T> findAll(String query, RowMapper rowMapper) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(query);
            return translateAllDomain(preparedStatement, rowMapper);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            checkNullStateOfCompositions(connection, preparedStatement);
        }
    }

    public <T> Optional<T> findWithCondition(String query, RowMapper rowMapper, Object... querySubject) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(query);
            setValue(preparedStatement, querySubject);
            ResultSet resultSet = preparedStatement.executeQuery();
            return Optional.ofNullable(rowMapper.rowMappedObject(resultSet));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            checkNullStateOfCompositions(connection, preparedStatement);
        }
    }

    private <T> List<T> translateAllDomain(PreparedStatement preparedStatement, RowMapper rowMapper)
        throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();
        List<T> domains = new LinkedList<>();
        while (resultSet.next()) {
            T t = rowMapper.rowMappedObject(resultSet);
            domains.add(t);
        }
        return domains;
    }

    private void setValue(PreparedStatement preparedStatement, Object... querySubject)
        throws SQLException {
        for (int i = 0; i < querySubject.length; i++) {
            preparedStatement.setObject(i + 1, querySubject[i]);
        }
    }

    private void checkNullStateOfCompositions(Connection connection,
        PreparedStatement preparedStatement) {
        checkNullPrepareStatement(preparedStatement);
        checkNullConnection(connection);
    }

    private void checkNullPrepareStatement(PreparedStatement preparedStatement) {
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (SQLException ignored) {
            log.error(ignored.getMessage(), ignored);
        }
    }

    private void checkNullConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ignored) {
            log.error(ignored.getMessage(), ignored);
        }
    }
}
