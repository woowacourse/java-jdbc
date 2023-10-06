package org.springframework.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.TestDataSourceConfig;
import org.springframework.jdbc.core.JdbcTemplateException.DatabaseAccessException;
import org.springframework.jdbc.core.JdbcTemplateException.MoreDataAccessException;
import org.springframework.jdbc.core.JdbcTemplateException.NoDataAccessException;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate = new JdbcTemplate(TestDataSourceConfig.getInstance());

    @Test
    void sql문을_실행할_수_있다() {
        // 생성
        jdbcTemplate.execute("insert into member (name) values ('콩하나 인서트');");

        try (Connection conn = TestDataSourceConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("select id from member where name = '콩하나 인서트';")) {

            assertThat(ps.executeQuery().next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // 삭제
        jdbcTemplate.execute("delete from member where name = ('콩하나 인서트');");
        try (Connection conn = TestDataSourceConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("select id from member where name = '콩하나 인서트';")) {

            assertThat(ps.executeQuery().next()).isFalse();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void sql문을_통해_객체를_조회할_수_있다() {
        String expectedName = "콩하나";

        TestMember testMember = jdbcTemplate.find("select id, name from member where name = '콩하나';",
                (rs) -> new TestMember(
                        rs.getLong("id"),
                        rs.getString("name")
                ));

        assertThat(testMember.getName()).isEqualTo(expectedName);
    }

    @Test
    void sql문을_통해_하나의_객체를_조회할_때_여러_객체가_존재하면_예외가_발생한다() {
        Assertions.assertThatThrownBy(() -> jdbcTemplate.find("select id, name from member;",
                        (rs) -> new TestMember(
                                rs.getLong("id"),
                                rs.getString("name")
                        )))
                .isInstanceOf(MoreDataAccessException.class);
    }

    @Test
    void sql문을_통해_하나의_객체를_조회할_때_객체가_존재하지_않으면_예외가_발생한다() {
        Assertions.assertThatThrownBy(
                        () -> jdbcTemplate.find("select id, name from member where id = " + Long.MAX_VALUE + ";",
                                (rs) -> new TestMember(
                                        rs.getLong("id"),
                                        rs.getString("name")
                                )))
                .isInstanceOf(NoDataAccessException.class);
    }

    @Test
    void sql문을_통해_여러_객체를_조회할_수_있다() {
        String expectedName1 = "콩하나";
        String expectedName2 = "콩둘";

        List<TestMember> testMembers = jdbcTemplate.findAll("select id, name from member;",
                (rs) -> new TestMember(
                        rs.getLong("id"),
                        rs.getString("name")
                ));

        assertThat(testMembers).map(TestMember::getName).containsExactlyInAnyOrder(expectedName1, expectedName2);
    }

    @Test
    void connection을_전달하지_않는_경우_내부에서_트랜잭션이_설정된다() throws SQLException {
        DataSource mockDataSource = mock(DataSource.class);
        Connection mockConnection = mock(Connection.class);
        JdbcTemplate mockJdbcTemplate = new JdbcTemplate(mockDataSource);

        String sql = "select id, name from member where name = '콩하나';";
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(sql))
                .thenReturn(TestDataSourceConfig.getInstance().getConnection().prepareStatement(sql));

        mockJdbcTemplate.find(sql,
                (rs) -> new TestMember(
                        rs.getLong("id"),
                        rs.getString("name")
                ));

        assertAll(
                () -> verify(mockConnection, times(1)).setAutoCommit(false),
                () -> verify(mockConnection, times(1)).setAutoCommit(true),
                () -> verify(mockConnection, never()).rollback()
        );
    }

    @Test
    void connection을_전달하지_않는_경우_예외가_발생하면_롤백이_호출된다() throws SQLException {
        DataSource mockDataSource = mock(DataSource.class);
        Connection mockConnection = mock(Connection.class);
        JdbcTemplate mockJdbcTemplate = new JdbcTemplate(mockDataSource);

        String sql = "insert into member (name) values ('kong');";
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(sql)).thenThrow(new DatabaseAccessException("공습 경보! 공습 경보!"));

        assertThatThrownBy(() -> mockJdbcTemplate.execute(sql))
                .isInstanceOf(JdbcTemplateException.class);

        assertAll(
                () -> verify(mockConnection, times(1)).setAutoCommit(false),
                () -> verify(mockConnection, times(1)).setAutoCommit(true),
                () -> verify(mockConnection, times(1)).rollback()
        );
    }

    @Test
    void connection을_전달하는_경우_트랜잭션이_걸리지_않고_autocommit이_유지된다() throws SQLException {
        DataSource mockDataSource = mock(DataSource.class);
        Connection mockConnection = mock(Connection.class);
        JdbcTemplate mockJdbcTemplate = new JdbcTemplate(mockDataSource);

        String sql = "select id, name from member where name = '콩하나';";
        when(mockConnection.prepareStatement(sql))
                .thenReturn(TestDataSourceConfig.getInstance().getConnection().prepareStatement(sql));

        mockJdbcTemplate.find(mockConnection,
                sql,
                (rs) -> new TestMember(
                        rs.getLong("id"),
                        rs.getString("name")
                ));

        assertAll(
                () -> verify(mockConnection, never()).setAutoCommit(false),
                () -> verify(mockConnection, never()).setAutoCommit(true),
                () -> verify(mockConnection, never()).rollback()
        );
    }
}
