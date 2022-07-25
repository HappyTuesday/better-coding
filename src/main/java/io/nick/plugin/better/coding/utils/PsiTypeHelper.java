package io.nick.plugin.better.coding.utils;

import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;

import java.util.Collection;
import java.util.Objects;

public class PsiTypeHelper {
    public boolean isPrimitive(PsiType psiType) {
        return psiType instanceof PsiPrimitiveType;
    }

    public boolean isString(PsiType psiType) {
        return psiType != null && String.class.getName().equals(psiType.getCanonicalText());
    }

    public boolean isCollectionOrArray(PsiType psiType) {
        return isCollection(psiType) || isArray(psiType);
    }

    public boolean isCollection(PsiType psiType) {
        if (psiType instanceof PsiClassType) {
            PsiClassType classType = (PsiClassType) psiType;
            if (Objects.equals(Collection.class.getCanonicalName(), classType.rawType().getCanonicalText())) {
                return true;
            }
            for (PsiType superType : classType.getSuperTypes()) {
                if (isCollection(superType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isArray(PsiType psiType) {
        return psiType instanceof PsiArrayType;
    }
}
