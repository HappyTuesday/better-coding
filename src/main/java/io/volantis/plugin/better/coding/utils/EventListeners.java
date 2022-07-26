package io.volantis.plugin.better.coding.utils;

import java.util.ArrayList;
import java.util.List;

public class EventListeners<T> {
    private final List<EventListener<T>> listeners = new ArrayList<>();

    public void listen(EventListener<T> listener) {
        this.listeners.add(listener);
    }

    public void onChange(T event) {
        for (EventListener<T> listener : listeners) {
            listener.onChange(event);
        }
    }
}
