package io.quarkus.micrometer.runtime.binder.vertx;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.quarkus.micrometer.runtime.meters.Gauges;

public class VertxEventBusMetricsTest {

    final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    final VertxEventBusMetrics metrics = new VertxEventBusMetrics(registry, Tags.empty(), Gauges.longAdder());

    private Gauge gauge(String name, String address) {
        return registry.find(name).tags("address", address).gauge();
    }

    @Test
    public void testGaugesRemovedWhenLastHandlerUnregisters() {
        VertxEventBusMetrics.Handler first = metrics.handlerRegistered("transient");
        VertxEventBusMetrics.Handler second = metrics.handlerRegistered("transient");
        metrics.messageDelivered(first, true);
        Assertions.assertEquals(2, gauge("eventBus.handlers", "transient").value());

        metrics.handlerUnregistered(first);
        Assertions.assertEquals(1, gauge("eventBus.handlers", "transient").value());
        Assertions.assertEquals(1, gauge("eventBus.delivered", "transient").value());

        metrics.handlerUnregistered(second);
        Assertions.assertNull(gauge("eventBus.handlers", "transient"));
        Assertions.assertNull(gauge("eventBus.delivered", "transient"));
        Assertions.assertNull(gauge("eventBus.discarded", "transient"));
    }

    @Test
    public void testAddressRegisteredAgainStartsFresh() {
        VertxEventBusMetrics.Handler handler = metrics.handlerRegistered("transient");
        metrics.messageDelivered(handler, true);
        metrics.discardMessage(handler, true, null);
        metrics.handlerUnregistered(handler);

        VertxEventBusMetrics.Handler again = metrics.handlerRegistered("transient");
        Assertions.assertEquals(1, gauge("eventBus.handlers", "transient").value());
        // The old values were dropped from the Gauges state, not just from the registry
        Assertions.assertEquals(0, gauge("eventBus.delivered", "transient").value());
        Assertions.assertEquals(0, gauge("eventBus.discarded", "transient").value());

        metrics.messageDelivered(again, true);
        Assertions.assertEquals(1, gauge("eventBus.delivered", "transient").value());
    }

    @Test
    public void testManyTransientAddressesDoNotGrowRegistry() {
        for (int i = 0; i < 100; i++) {
            VertxEventBusMetrics.Handler handler = metrics.handlerRegistered("transient." + i);
            metrics.handlerUnregistered(handler);
        }
        Assertions.assertTrue(registry.find("eventBus.handlers").gauges().isEmpty());
        Assertions.assertTrue(registry.find("eventBus.delivered").gauges().isEmpty());
        Assertions.assertTrue(registry.find("eventBus.discarded").gauges().isEmpty());
    }

    @Test
    public void testInternalAddressesAreIgnored() {
        VertxEventBusMetrics.Handler handler = metrics.handlerRegistered("__vertx.reply.1");
        metrics.handlerUnregistered(handler);
        Assertions.assertTrue(registry.getMeters().isEmpty());
    }
}
