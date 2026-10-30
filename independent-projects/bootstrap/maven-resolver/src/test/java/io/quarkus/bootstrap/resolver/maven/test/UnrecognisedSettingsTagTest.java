package io.quarkus.bootstrap.resolver.maven.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import io.quarkus.bootstrap.resolver.maven.BootstrapMavenContext;

public class UnrecognisedSettingsTagTest extends BootstrapMavenContextTestBase {

    @Test
    public void warningWithoutStackTrace() throws Exception {
        final List<LogRecord> records = new CopyOnWriteArrayList<>();
        final Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        final Logger logger = Logger.getLogger(BootstrapMavenContext.class.getName());
        logger.addHandler(handler);
        try {
            final BootstrapMavenContext mvn = bootstrapMavenContextWithSettings("custom-settings/unrecognised-tag");
            assertThat(mvn.getEffectiveSettings().getServer("my-server")).isNotNull();
        } finally {
            logger.removeHandler(handler);
        }

        final List<LogRecord> warnings = records.stream()
                .filter(r -> r.getLevel().intValue() >= Level.WARNING.intValue())
                .toList();
        assertThat(warnings).hasSize(1);
        final LogRecord warning = warnings.get(0);
        assertThat(warning.getThrown()).isNull();
        assertThat(warning.getMessage()).contains("Settings problem encountered at").contains("Unrecognised tag: 'aliases'");
    }
}
