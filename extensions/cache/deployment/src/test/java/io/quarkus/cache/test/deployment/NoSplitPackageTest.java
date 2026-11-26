package io.quarkus.cache.test.deployment;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.stream.Collectors;

import jakarta.enterprise.context.Dependent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.arc.deployment.SplitPackageProcessor;
import io.quarkus.cache.CacheResult;
import io.quarkus.test.QuarkusExtensionTest;

/**
 * The cache extension must not split the {@code io.quarkus.cache} package between {@code quarkus-cache-api} and
 * {@code quarkus-cache}.
 */
public class NoSplitPackageTest {

    @RegisterExtension
    static final QuarkusExtensionTest TEST = new QuarkusExtensionTest()
            .withApplicationRoot((jar) -> jar.addClass(CachedService.class))
            .setLogRecordPredicate(r -> r.getLoggerName().equals(SplitPackageProcessor.class.getName()))
            .assertLogRecords(records -> {
                // the package list is passed as a log parameter, not inlined in the message
                String messages = records.stream()
                        .map(r -> r.getMessage() + " " + Arrays.toString(r.getParameters()))
                        .collect(Collectors.joining("\n"));
                assertTrue(!messages.contains("\"io.quarkus.cache\""), messages);
            });

    @Test
    public void testNoSplitPackageWarning() {
        // the assertion is done in assertLogRecords
    }

    @Dependent
    static class CachedService {

        @CacheResult(cacheName = "test-cache")
        public Object cachedMethod(String key) {
            return new Object();
        }
    }
}
