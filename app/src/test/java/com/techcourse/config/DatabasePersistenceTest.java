package com.techcourse.config;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.dao.UserDao;
import org.h2.Driver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class DatabasePersistenceTest {

    @TempDir
    Path directory;

    @Test
    void preservesUsersAcrossJvmRestarts() throws Exception {
        final var url = "jdbc:h2:file:" + directory.resolve("users");
        runProbe(url, "write");
        runProbe(url, "read");
        assertThat(directory.resolve("users.mv.db")).exists();
    }

    private void runProbe(final String url, final String operation) throws Exception {
        final var classpath = new LinkedHashSet<String>();
        for (final var type : new Class<?>[]{PersistenceProbe.class, UserDao.class,
                JdbcTemplate.class, Driver.class, Logger.class}) {
            classpath.add(Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI()).toString());
        }
        classpath.add(Path.of(UserDao.class.getResource("/schema.sql").toURI()).getParent().toString());
        final var process = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-Djdbc.url=" + url, "-cp", String.join(File.pathSeparator, classpath),
                PersistenceProbe.class.getName(), operation)
                .redirectErrorStream(true).start();
        try {
            assertThat(process.waitFor(20, TimeUnit.SECONDS)).as("별도 JVM 종료").isTrue();
            final var output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            assertThat(process.exitValue()).as(output).isZero();
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly().waitFor();
            }
        }
    }
}
