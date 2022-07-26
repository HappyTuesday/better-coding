package io.volantis.plugin.better.coding.utils;

import com.intellij.psi.*;
import io.volantis.plugin.better.coding.proxy.DtoField;

import java.util.ArrayList;
import java.util.List;

public class EntityHelper {
    public static PsiField findEntityFieldForDTO(PsiClass entityClass, DtoField dtoField) {
        return entityClass.findFieldByName(dtoField.getName(), true);
    }

    public static List<PsiClass> entityClassesInDir(PsiDirectory dir) {
        List<PsiClass> entityClasses = new ArrayList<>();
        for (PsiFile psiFile : dir.getFiles()) {
            if (psiFile instanceof PsiJavaFile) {
                for (PsiClass psiClass : ((PsiJavaFile) psiFile).getClasses()) {
                    String className = psiClass.getName();
                    if (className == null
                        || className.endsWith("Factory")
                        || className.endsWith("Repository")
                        || className.endsWith("Persister")
                        || psiClass.isEnum()
                        || psiClass.isAnnotationType()
                        || psiClass.isInterface()) {
                        continue;
                    }
                    entityClasses.add(psiClass);
                }
            }
        }
        return entityClasses;
    }

    public static List<PsiClass> listRelatedClasses(PsiDirectory directory, String suffix) {
        List<PsiClass> classes = new ArrayList<>();
        for (PsiFile psiFile : directory.getFiles()) {
            if (psiFile instanceof PsiJavaFile) {
                for (PsiClass psiClass : ((PsiJavaFile) psiFile).getClasses()) {
                    String className = psiClass.getName();
                    if (className != null && className.endsWith(suffix)) {
                        classes.add(psiClass);
                    }
                }
            }
        }
        return classes;
    }
}
