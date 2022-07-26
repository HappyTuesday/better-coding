package io.volantis.plugin.better.coding.utils;

import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.NotNull;

public interface ComboBoxCustomizer<T> {
    String toString(T value);

    void customize(@NotNull JBLabel label, T value, int index);
}
