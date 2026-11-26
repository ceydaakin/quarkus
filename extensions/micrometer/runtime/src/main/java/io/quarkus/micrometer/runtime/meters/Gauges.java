package io.quarkus.micrometer.runtime.meters;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

public class Gauges<T> {

    private final ConcurrentMap<Meter.Id, T> map;
    private final Supplier<T> factory;

    private Gauges(Supplier<T> factory) {
        this.map = new ConcurrentHashMap<>();
        this.factory = factory;
    }

    public static Gauges<LongAdder> longAdder() {
        return new Gauges<>(LongAdder::new);
    }

    public static Gauges<AtomicInteger> atomicInteger() {
        return new Gauges<>(AtomicInteger::new);
    }

    public static <T> Gauges<T> of(Supplier<T> factory) {
        return new Gauges<>(factory);
    }

    public GaugeBuilder<T> builder(String name, ToDoubleFunction<T> func) {
        return new GaugeBuilder<>(name, map, factory, func);
    }

    /**
     * Removes a gauge registered by this instance from the registry and drops its value,
     * so registering the same name and tags again starts from a fresh value.
     */
    public void remove(MeterRegistry registry, String name, Tags tags) {
        Meter removed = registry.removeByPreFilterId(new Meter.Id(name, tags, null, null, Meter.Type.GAUGE));
        if (removed != null) {
            map.remove(removed.getId());
        }
    }
}
