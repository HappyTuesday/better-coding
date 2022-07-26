package io.volantis.plugin.better.coding.utils;

import java.awt.*;

public class FontStyleUtil {
    public static void bold(Component component) {
        Font font = component.getFont();
        component.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
    }
}
