package io.nick.plugin.better.coding.utils;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.SimpleListCellRenderer;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class MyComboBox<E> extends ComboBox<MyComboBox.Item<E>> {
    private final ComboBoxCustomizer<E> customizer;

    public MyComboBox(ComboBoxCustomizer<E> customizer) {
        this.customizer = customizer;
        setRenderer(SimpleListCellRenderer.create((label, value, index) ->
            customizer.customize(label, value != null ? value.data : null, index)
        ));
    }

    public MyComboBox(ComboBoxCustomizer<E> customizer, List<E> items) {
        this(customizer);
        setElements(items);
    }

    public void addSelectedListener(Consumer<E> consumer) {
        addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                //noinspection unchecked
                Item<E> item = (Item<E>) e.getItem();
                if (item != null) {
                    consumer.accept(item.data);
                }
            }
        });
    }

    public void setElements(List<E> elements) {
        //noinspection unchecked
        Item<E>[] items = elements.stream().map(e -> new Item<>(e, customizer)).toArray(Item[]::new);
        setModel(new DefaultComboBoxModel<>(items));
    }

    public E getElement() {
        Item<E> item = getItem();
        return item != null ? item.data : null;
    }

    public void select(E element) {
        ComboBoxModel<Item<E>> model = getModel();
        for (int i = 0; i < model.getSize(); i++) {
            Item<E> item = model.getElementAt(i);
            if (Objects.equals(item.data, element)) {
                setItem(item);
                break;
            }
        }
    }

    public static class Item<E> {
        public final E data;
        public final ComboBoxCustomizer<E> customizer;

        public Item(E data, ComboBoxCustomizer<E> customizer) {
            this.data = data;
            this.customizer = customizer;
        }

        @Override
        public String toString() {
            return customizer.toString(data);
        }
    }
}
