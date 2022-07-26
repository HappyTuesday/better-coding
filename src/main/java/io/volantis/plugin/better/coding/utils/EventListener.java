package io.volantis.plugin.better.coding.utils;

@FunctionalInterface
public interface EventListener<T> {
    void onChange(T event);
}
