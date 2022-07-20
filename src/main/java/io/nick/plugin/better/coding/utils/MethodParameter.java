package io.nick.plugin.better.coding.utils;

import com.intellij.psi.PsiType;

import java.util.Objects;

public class MethodParameter {
    public final String paramName;
    public PsiType paramType;
    public String desc;

    public MethodParameter(String paramName) {
        this.paramName = paramName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodParameter parameter = (MethodParameter) o;
        return paramName.equals(parameter.paramName) && paramType.equals(parameter.paramType) && Objects.equals(desc, parameter.desc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paramName, paramType, desc);
    }
}
