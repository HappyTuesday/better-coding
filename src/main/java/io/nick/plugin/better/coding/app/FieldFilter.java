package io.nick.plugin.better.coding.app;

import com.intellij.openapi.util.text.StringUtil;

import java.util.Objects;

public class FieldFilter {
    public final String fieldName;
    public final FieldFilterMode filterMode;

    public FieldFilter(String fieldName, FieldFilterMode filterMode) {
        this.fieldName = fieldName;
        this.filterMode = filterMode;
    }

    public String getVar() {
        switch (filterMode) {
            case EQUAL_TO:
                return fieldName;
            case IN_LIST:
                return StringUtil.pluralize(fieldName);
            case LIKE:
                return fieldName + "Like";
            default:
                throw new IllegalArgumentException("unsupported operator " + filterMode);
        }
    }

    public String getExampleCriteriaIsNullMethod() {
        return "and" + StringUtil.capitalize(fieldName) + "IsNull";
    }

    public String getExampleCriteriaMethod() {
        String suffix;
        switch (filterMode) {
            case EQUAL_TO:
                suffix = "EqualTo";
                break;
            case IN_LIST:
                suffix = "In";
                break;
            case LIKE:
                suffix = "Like";
                break;
            default:
                throw new IllegalArgumentException("unsupported operator " + filterMode);
        }
        return "and" + StringUtil.capitalize(fieldName) + suffix;
    }

    public boolean isInListMode() {
        return filterMode == FieldFilterMode.IN_LIST;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldFilter filter = (FieldFilter) o;
        return fieldName.equals(filter.fieldName) && filterMode == filter.filterMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, filterMode);
    }
}
