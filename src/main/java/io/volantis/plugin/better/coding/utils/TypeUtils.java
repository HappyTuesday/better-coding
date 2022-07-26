package io.volantis.plugin.better.coding.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;

public class TypeUtils {
    public static PsiType listOf(Project project, PsiType elementType) {
        PsiClass list = JavaPsiFacade.getInstance(project).findClass("java.util.List", GlobalSearchScope.allScope(project));
        if (list == null) {
            return null;
        }
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        return factory.createType(list, elementType);
    }

    public static boolean isListType(PsiType type) {
        if (!(type instanceof PsiClassType)) {
            return false;
        }
        PsiClass psiClass = ((PsiClassType) type).resolve();
        return psiClass != null && "java.util.List".equals(psiClass.getQualifiedName());
    }

    public static boolean isMapType(PsiType type) {
        if (!(type instanceof PsiClassType)) {
            return false;
        }
        PsiClass psiClass = ((PsiClassType) type).resolve();
        return psiClass != null && "java.util.Map".equals(psiClass.getQualifiedName());
    }
}
