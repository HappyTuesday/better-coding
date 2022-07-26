package io.volantis.plugin.better.coding.proxy;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import io.volantis.plugin.better.coding.utils.FieldTemplate;

import java.util.ArrayList;
import java.util.List;

public class InfoProxy extends PsiClassProxy {
    public InfoProxy(PsiDirectory directory, String className) {
        super(directory, className);
    }

    public InfoProxy(PsiClass psiClass) {
        super(psiClass);
    }

    @Override
    protected PsiClass doCreateClass() {
        return classTemplate()
            .pass("infoProxy", this)
            .create("info/info-class.ftl", getSettings().getInfoClassTemplate());
    }

    public List<InfoField> getInfoFields() {
        List<InfoField> infoFields = new ArrayList<>();
        for (PsiField psiField : getAllFields()) {
            if (isInfoField(psiField)) {
                InfoField infoField = new InfoField(psiField);
                infoFields.add(infoField);
            }
        }
        return infoFields;
    }

    public List<InfoField> getInfoFieldsFromDTO(DtoProxy dtoProxy) {
        List<InfoField> infoFields = new ArrayList<>();
        for (PsiField psiField : getAllFields()) {
            if (isInfoField(psiField)) {
                InfoField infoField = new InfoField(psiField);
                InfoField.Source source = infoField.getSource();
                if (source != null && source.dtoClassName.equals(dtoProxy.getClassName())) {
                    infoFields.add(infoField);
                }
            }
        }
        return infoFields;
    }

    public InfoField findInfoField(String fieldName) {
        PsiField psiField = psiClass.findFieldByName(fieldName, true);
        if (psiField != null && isInfoField(psiField)) {
            return new InfoField(psiField);
        } else {
            return null;
        }
    }

    public InfoField findInfoFieldWithSource(DtoField dtoField) {
        for (PsiField psiField : psiClass.getAllFields()) {
            if (isInfoField(psiField)) {
                InfoField infoField = new InfoField(psiField);
                if (dtoField.equals(infoField.getSourceField())) {
                    return infoField;
                }
            }
        }
        return null;
    }

    private boolean isInfoField(PsiField psiField) {
        PsiModifierList modifierList = psiField.getModifierList();
        if (modifierList == null) return false;
        if (modifierList.hasModifierProperty(PsiModifier.STATIC)) return false;
        if (modifierList.hasExplicitModifier(PsiModifier.FINAL)) return false;
        return modifierList.hasModifierProperty(PsiModifier.PUBLIC);
    }

    public void importFieldsFromDTO(List<DtoField> dtoFields) {
        for (DtoField dtoField : dtoFields) {
            importFieldFromDTO(dtoField);
        }
    }

    private void importFieldFromDTO(DtoField dtoField) {
        if (dtoField.isLogicalDeleteField()) {
            return;
        }
        createClassIfNotExist();
        InfoField infoField = findInfoField(dtoField.getName());
        String fieldPrefix;
        if (infoField != null) {
            DtoField oldDtoField = infoField.getSourceField();
            if (oldDtoField == dtoField) {
                return;
            }
            PsiClass dtoClass = dtoField.getContainingClass();
            if (dtoClass.getName() != null) {
                fieldPrefix = StringUtil.decapitalize(StringUtil.trimEnd(dtoClass.getName(), "DTO"));
            } else {
                fieldPrefix = "dto";
            }
        } else {
            fieldPrefix = null;
        }
        new FieldTemplate(psiClass)
            .pass("dtoField", dtoField)
            .pass("fieldPrefix", fieldPrefix)
            .generateAndAdd("add-field-from-dto", getSettings().getInfoFieldTemplate());
    }

    public static InfoProxy createForName(String infoClassName, Project project) {
        PsiShortNamesCache shortNamesCache = PsiShortNamesCache.getInstance(project);
        PsiClass[] infoClasses = shortNamesCache.getClassesByName(infoClassName, GlobalSearchScope.projectScope(project));
        if (infoClasses.length == 0) {
            return null;
        }
        return new InfoProxy(infoClasses[0]);
    }

    public static List<InfoProxy> listAllInfosInProject(Project project) {
        PsiShortNamesCache shortNamesCache = PsiShortNamesCache.getInstance(project);
        List<InfoProxy> list = new ArrayList<>();
        shortNamesCache.processAllClassNames(className -> {
            if (!className.endsWith("Info")) return true;
            InfoProxy infoProxy = createForName(className, project);
            if (infoProxy != null) {
                list.add(infoProxy);
            }
            return true;
        });
        return list;
    }

    public static InfoProxy forType(PsiType psiType) {
        return new InfoProxy(((PsiClassType) psiType).resolve());
    }

    public static boolean isInfoClassType(PsiType psiType) {
        if (!(psiType instanceof PsiClassType)) {
            return false;
        }
        PsiClass psiClass = ((PsiClassType) psiType).resolve();
        return psiClass != null && psiClass.getName() != null && psiClass.getName().endsWith("Info");
    }
}
