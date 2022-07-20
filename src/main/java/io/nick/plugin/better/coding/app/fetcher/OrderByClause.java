package io.nick.plugin.better.coding.app.fetcher;

import io.nick.plugin.better.coding.utils.PropertyUtils;

import java.util.Objects;

public class OrderByClause {
    public final String fieldName;
    public boolean descending;

    public OrderByClause(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getColumnName() {
        return PropertyUtils.getColumnName(fieldName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderByClause that = (OrderByClause) o;
        return descending == that.descending && fieldName.equals(that.fieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, descending);
    }
}
