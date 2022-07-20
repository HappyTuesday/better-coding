package io.nick.plugin.better.coding.utils;

@FunctionalInterface
public interface EventListener<T> {
    void onChange(T event);
}
