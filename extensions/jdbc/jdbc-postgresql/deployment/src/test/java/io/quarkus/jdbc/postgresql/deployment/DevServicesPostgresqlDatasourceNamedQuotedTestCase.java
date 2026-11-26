package io.quarkus.jdbc.postgresql.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.agroal.api.AgroalDataSource;
import io.quarkus.test.QuarkusExtensionTest;

/**
 * Dev Services must set the URL and credentials of a named datasource with the same spelling
 * (quoted datasource name) as the application, otherwise SmallRye Config warns about ambiguous
 * Map keys (SRCFG01009).
 */
public class DevServicesPostgresqlDatasourceNamedQuotedTestCase {

    @RegisterExtension
    static QuarkusExtensionTest test = new QuarkusExtensionTest()
            .withEmptyApplication()
            .overrideConfigKey("quarkus.datasource.\"readonly\".db-kind", "postgresql")
            .setLogRecordPredicate(record -> record.getLevel().intValue() >= Level.WARNING.intValue()
                    && record.getMessage().contains("SRCFG01009"))
            .assertLogRecords(records -> assertThat(records)
                    // This is just to get meaningful error messages, as LogRecord doesn't have a toString()
                    .extracting(LogRecord::getMessage)
                    .isEmpty());

    @Inject
    @Named("readonly")
    AgroalDataSource dataSource;

    @Test
    public void testDatasource() throws Exception {
        assertThat(dataSource.getConfiguration().connectionPoolConfiguration().connectionFactoryConfiguration().jdbcUrl())
                .contains("jdbc:postgresql:");
        try (Connection connection = dataSource.getConnection()) {
        }
    }
}
