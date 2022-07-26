package io.volantis.plugin.better.coding.proxy;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.util.PropertyUtil;

import java.util.ArrayList;
import java.util.List;

public class DtoProxy extends PsiClassProxy {
    private DtoProxy(PsiDirectory directory, String className) {
        super(directory, className);
    }

    public DtoProxy(PsiClass psiClass) {
        super(psiClass);
    }

    @Override
    protected PsiClass doCreateClass() {
        throw new UnsupportedOperationException();
    }

    public DtoField getDtoField(String fieldName) {
        PsiField psiField = psiClass.findFieldByName(fieldName, true);
        return psiField != null && isDtoField(psiField) ? new DtoField(this, psiField) : null;
    }

    public List<DtoField> getDtoFields() {
        List<DtoField> dtoFields = new ArrayList<>();
        for (PsiField psiField : getAllFields()) {
            if (isDtoField(psiField)) {
                DtoField dtoField = new DtoField(this, psiField);
                dtoFields.add(dtoField);
            }
        }
        return dtoFields;
    }

    private boolean isDtoField(PsiField psiField) {
        PsiModifierList modifierList = psiField.getModifierList();
        if (modifierList == null) return true;
        if (modifierList.hasModifierProperty(PsiModifier.STATIC)) return false;
        if (modifierList.hasExplicitModifier(PsiModifier.FINAL)) return false;
        PsiMethod getter = PropertyUtil.findGetterForField(psiField);
        PsiMethod setter = PropertyUtil.findSetterForField(psiField);
        return getter != null && setter != null;
    }

    public DtoField getLogicalDeleteField() {
        for (DtoField dtoField : getDtoFields()) {
            if (dtoField.isLogicalDeleteField()) {
                return dtoField;
            }
        }
        return null;
    }

    public MapperProxy getMapperProxy() {
        return MapperProxy.forDto(this);
    }

    public ExampleProxy getExampleProxy() {
        return ExampleProxy.forDto(this);
    }

    public static DtoProxy createForName(String dtoClassName, Project project) {
        PsiShortNamesCache shortNamesCache = PsiShortNamesCache.getInstance(project);
        PsiClass[] dtoClasses = shortNamesCache.getClassesByName(dtoClassName, GlobalSearchScope.projectScope(project));
        if (dtoClasses.length == 0) {
            return null;
        }
        return new DtoProxy(dtoClasses[0]);
    }

    public static List<DtoProxy> listAllDTOsInProject(Project project) {
        PsiShortNamesCache shortNamesCache = PsiShortNamesCache.getInstance(project);
        List<DtoProxy> list = new ArrayList<>();
        shortNamesCache.processAllClassNames(className -> {
            if (!isDtoClass(className)) return true;
            DtoProxy dtoProxy = createForName(className, project);
            if (dtoProxy != null) {
                list.add(dtoProxy);
            }
            return true;
        });
        return list;
    }

    public static boolean isDtoClass(String className) {
        return className != null && className.endsWith("DTO");
    }

    public static boolean isDtoClass(PsiClass psiClass) {
        return isDtoClass(psiClass.getName());
    }
}
