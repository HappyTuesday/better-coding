package io.nick.plugin.better.coding.utils;

import org.apache.commons.lang3.StringUtils;

public class PropertyUtils {
    public static String getSetterName(String fieldName) {
        return "set" + StringUtils.capitalize(fieldName);
    }

    public static String getGetterName(String fieldName, boolean booleanType) {
        if (booleanType) {
            return "is" + StringUtils.capitalize(fieldName);
        } else {
            return "get" + StringUtils.capitalize(fieldName);
        }
    }

    public static String getPluralName(String name) {
        return name + "s";
    }

    public static String getVarName(String name) {
        return StringUtils.uncapitalize(name);
    }

    public static String getPluralVarName(String name) {
        return getPluralName(getVarName(name));
    }

    public static String getColumnName(String fieldName) {
        StringBuilder sb = new StringBuilder(fieldName.length());
        boolean lastUppercase = true;
        for (int i = 0; i < fieldName.length(); i++) {
            char c = fieldName.charAt(i);
            if (Character.isUpperCase(c)) {
                if (!lastUppercase) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
                lastUppercase = true;
            } else {
                sb.append(c);
                lastUppercase = false;
            }
        }
        return sb.toString();
    }
}
