package io.quarkus.datasource.common.runtime;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkus.runtime.configuration.ConfigurationException;

public final class DataSourceUtil {

    public static final String DEFAULT_DATASOURCE_NAME = "<default>";

    public static boolean isDefault(String dataSourceName) {
        return dataSourceName == null || DEFAULT_DATASOURCE_NAME.equals(dataSourceName);
    }

    public static boolean hasDefault(Collection<String> dataSourceNames) {
        return dataSourceNames.contains(DEFAULT_DATASOURCE_NAME);
    }

    public static String dataSourcePropertyKey(String datasourceName, String radical) {
        if (DataSourceUtil.isDefault(datasourceName)) {
            return "quarkus.datasource." + radical;
        } else {
            return "quarkus.datasource.\"" + datasourceName + "\"." + radical;
        }
    }

    public static List<String> dataSourcePropertyKeys(String datasourceName, String radical) {
        if (DataSourceUtil.isDefault(datasourceName)) {
            return List.of("quarkus.datasource." + radical);
        } else {
            // Two possible syntaxes: with or without quotes
            return List.of(
                    "quarkus.datasource.\"" + datasourceName + "\"." + radical,
                    "quarkus.datasource." + datasourceName + "." + radical);
        }
    }

    /**
     * Returns the keys to use when setting a datasource property on the user's behalf (e.g. from Dev Services).
     * <p>
     * Setting both the quoted and the unquoted spelling of a named datasource while the user only uses one of them
     * makes SmallRye Config warn about ambiguous map keys (SRCFG01009),
     * so this returns only the spelling the user uses for this datasource,
     * and both spellings if the user uses neither or both.
     *
     * @param configuredPropertyNames the names of the properties the user configured
     */
    public static List<String> dataSourcePropertyKeysMatchingConfig(String datasourceName, String radical,
            Iterable<String> configuredPropertyNames) {
        List<String> keys = dataSourcePropertyKeys(datasourceName, radical);
        if (keys.size() == 1) {
            return keys;
        }
        String quotedPrefix = "quarkus.datasource.\"" + datasourceName + "\".";
        String unquotedPrefix = "quarkus.datasource." + datasourceName + ".";
        boolean quotedUsed = false;
        boolean unquotedUsed = false;
        for (String propertyName : configuredPropertyNames) {
            quotedUsed |= propertyName.startsWith(quotedPrefix);
            unquotedUsed |= propertyName.startsWith(unquotedPrefix);
        }
        if (quotedUsed == unquotedUsed) {
            return keys;
        }
        return List.of(quotedUsed ? keys.get(0) : keys.get(1));
    }

    /**
     * Same as {@link #dataSourcePropertyKeysMatchingConfig(String, String, Iterable)},
     * looking at the property names of the current configuration.
     */
    public static List<String> dataSourcePropertyKeysMatchingConfig(String datasourceName, String radical) {
        return dataSourcePropertyKeysMatchingConfig(datasourceName, radical,
                ConfigProvider.getConfig().getPropertyNames());
    }

    public static ConfigurationException dataSourceNotConfigured(String dataSourceName) {
        return new ConfigurationException(String.format(Locale.ROOT,
                "Datasource '%s' is not configured."
                        + " To solve this, configure datasource '%s'."
                        + " Refer to https://quarkus.io/guides/datasource for guidance.",
                dataSourceName, dataSourceName),
                Set.of(dataSourcePropertyKey(dataSourceName, "db-kind"),
                        dataSourcePropertyKey(dataSourceName, "username"),
                        dataSourcePropertyKey(dataSourceName, "password"),
                        dataSourcePropertyKey(dataSourceName, "jdbc.url")));
    }

    /**
     * @deprecated Simply call {@code io.quarkus.arc.ClientProxy#unwrap(Object)} on a datasource bean instance:
     *             it will throw a similar exception, with more details and an actionable message.
     */
    @Deprecated
    public static ConfigurationException dataSourceInactive(String dataSourceName) {
        return new ConfigurationException(dataSourceInactiveReasonDeactivated(dataSourceName),
                Set.of(dataSourcePropertyKey(dataSourceName, "db-kind"),
                        dataSourcePropertyKey(dataSourceName, "username"),
                        dataSourcePropertyKey(dataSourceName, "password"),
                        dataSourcePropertyKey(dataSourceName, "jdbc.url")));
    }

    public static String dataSourceInactiveReasonDeactivated(String dataSourceName) {
        return String.format(Locale.ROOT,
                "Datasource '%s' was deactivated through configuration properties."
                        + " To activate the datasource, set configuration property '%s' to 'true' and configure datasource '%s'."
                        + " Refer to https://quarkus.io/guides/datasource for guidance.",
                dataSourceName, dataSourcePropertyKey(dataSourceName, "active"), dataSourceName);
    }

    public static String dataSourceInactiveReasonUrlMissing(String dataSourceName, String urlPropertyRadical) {
        return String.format(Locale.ROOT,
                "Datasource '%s' was deactivated automatically because its URL is not set."
                        + " To activate the datasource, set configuration property '%s'."
                        + " Refer to https://quarkus.io/guides/datasource for guidance.",
                dataSourceName, dataSourcePropertyKey(dataSourceName, urlPropertyRadical));
    }

    private DataSourceUtil() {
    }

}
