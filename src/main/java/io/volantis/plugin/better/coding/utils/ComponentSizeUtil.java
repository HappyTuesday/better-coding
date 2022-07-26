package io.volantis.plugin.better.coding.utils;

import com.intellij.util.ui.JBDimension;

import java.awt.*;

public class ComponentSizeUtil {
    public static void setPreferredWidth(Component component, int width) {
        Dimension dim = component.getPreferredSize();
        component.setPreferredSize(new JBDimension(width, dim.height));
    }

    public static void setPreferredHeight(Component component, int height) {
        Dimension dim = component.getPreferredSize();
        component.setPreferredSize(new JBDimension(dim.width, height));
    }
}
