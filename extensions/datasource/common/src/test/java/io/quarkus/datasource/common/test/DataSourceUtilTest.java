package io.quarkus.datasource.common.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.quarkus.datasource.common.runtime.DataSourceUtil;

public class DataSourceUtilTest {

    private static final String QUOTED = "quarkus.datasource.\"readonly\".jdbc.url";
    private static final String UNQUOTED = "quarkus.datasource.readonly.jdbc.url";

    @Test
    public void keysMatchingConfigUseQuotedSpellingWhenUserUsesIt() {
        assertEquals(List.of(QUOTED), DataSourceUtil.dataSourcePropertyKeysMatchingConfig("readonly", "jdbc.url",
                List.of("quarkus.datasource.db-kind", "quarkus.datasource.\"readonly\".db-kind")));
    }

    @Test
    public void keysMatchingConfigUseUnquotedSpellingWhenUserUsesIt() {
        assertEquals(List.of(UNQUOTED), DataSourceUtil.dataSourcePropertyKeysMatchingConfig("readonly", "jdbc.url",
                List.of("quarkus.datasource.db-kind", "quarkus.datasource.readonly.db-kind")));
    }

    @Test
    public void keysMatchingConfigUseBothSpellingsWhenUserUsesNeither() {
        assertEquals(List.of(QUOTED, UNQUOTED), DataSourceUtil.dataSourcePropertyKeysMatchingConfig("readonly", "jdbc.url",
                List.of("quarkus.datasource.db-kind", "quarkus.datasource.\"other\".db-kind")));
    }

    @Test
    public void keysMatchingConfigUseBothSpellingsWhenUserUsesBoth() {
        assertEquals(List.of(QUOTED, UNQUOTED), DataSourceUtil.dataSourcePropertyKeysMatchingConfig("readonly", "jdbc.url",
                List.of("quarkus.datasource.\"readonly\".db-kind", "quarkus.datasource.readonly.username")));
    }

    @Test
    public void keysMatchingConfigForDefaultDatasource() {
        assertEquals(List.of("quarkus.datasource.jdbc.url"),
                DataSourceUtil.dataSourcePropertyKeysMatchingConfig(DataSourceUtil.DEFAULT_DATASOURCE_NAME, "jdbc.url",
                        List.of("quarkus.datasource.db-kind")));
    }
}
