package io.nick.plugin.better.coding.proxy;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiJavaDirectoryFactory;
import com.intellij.psi.util.PropertyUtil;
import io.nick.plugin.better.coding.utils.CodingUtils;
import io.nick.plugin.better.coding.utils.EntityHelper;
import io.nick.plugin.better.coding.utils.FieldTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class PsiClassProxy {
    protected final PsiDirectory directory;
    protected final String className;
    protected PsiClass psiClass;

    protected PsiClassProxy(PsiDirectory directory, String className) {
        this.directory = directory;
        this.className = className;
    }

    protected PsiClassProxy(PsiClass psiClass) {
        this.directory = psiClass.getContainingFile().getContainingDirectory();
        this.className = Objects.requireNonNull(psiClass.getName());
        this.psiClass = psiClass;
    }

    public PsiClass getPsiClass() {
        return Objects.requireNonNull(psiClass);
    }

    public abstract PsiClass createClassIfNotExist();

    public boolean isClassCreated() {
        return psiClass != null;
    }

    public String getClassName() {
        return className;
    }

    public String getVarName() {
        return StringUtil.decapitalize(className);
    }

    public String getVarsName() {
        return StringUtil.pluralize(StringUtil.decapitalize(className));
    }

    public String getMapVarName() {
        return StringUtil.decapitalize(className) + "Map";
    }

    public String getQualifiedName() {
        if (psiClass != null) {
            return psiClass.getQualifiedName();
        }
        String packageName = PsiJavaDirectoryFactory.getInstance(getProject()).getQualifiedName(directory, false);
        return StringUtil.getQualifiedName(packageName, className);
    }

    public Project getProject() {
        return directory.getProject();
    }

    public PsiElementFactory getFactory() {
        return JavaPsiFacade.getElementFactory(directory.getProject());
    }

    public PsiDirectory getDirectory() {
        return directory;
    }

    public PsiMethod[] getMethods() {
        return psiClass.getMethods();
    }

    public PsiField[] getFields() {
        return psiClass.getFields();
    }

    public PsiField[] getAllFields() {
        // 需要父类的字段在前
        List<PsiField> psiFields = new ArrayList<>();
        for (PsiClass c = psiClass; c != null; c = c.getSuperClass()) {
            psiFields.addAll(0, Arrays.asList(c.getFields()));
        }
        return psiFields.toArray(PsiField[]::new);
    }

    public PsiClassType createType() {
        createClassIfNotExist();
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        return factory.createType(psiClass);
    }

    public PsiField createInjectedField(String fieldName, PsiType fieldType) {
        return new FieldTemplate(createClassIfNotExist())
            .pass("fieldName", fieldName)
            .pass("fieldType", fieldType)
            .generate("common/injected-resource-field.ftl");
    }

    public void addInjectedField(String fieldName, PsiType fieldType) {
        if (containsField(fieldName)) {
            return;
        }
        PsiField psiField = createInjectedField(fieldName, fieldType);
        if (psiField != null) {
            addField(psiField);
        }
    }

    public void addField(PsiField field) {
        createClassIfNotExist();
        CodingUtils.addFieldToClass(field, psiClass);
    }

    public boolean addMethod(PsiMethod method) {
        createClassIfNotExist();
        if (psiClass.findMethodBySignature(method, true) != null) {
            return false;
        }
        PsiMethod created = (PsiMethod) psiClass.addBefore(method, psiClass.getLastChild());
        CodingUtils.shortenClassReferences(created);
        return true;
    }

    public boolean containsField(String fieldName) {
        return psiClass != null && psiClass.findFieldByName(fieldName, true) != null;
    }

    public List<PsiClass> listRelatedClasses(String suffix) {
        return EntityHelper.listRelatedClasses(directory, suffix);
    }

    public PsiField getFieldOfSetter(PsiMethod method) {
        return PropertyUtil.getFieldOfSetter(method);
    }

    public PsiField getFieldOfGetter(PsiMethod method) {
        return PropertyUtil.getFieldOfGetter(method);
    }

    public boolean isEnumType(PsiType type) {
        if (type instanceof PsiClassType) {
            PsiClass resolved = ((PsiClassType) type).resolve();
            return resolved != null && resolved.isEnum();
        } else {
            return false;
        }
    }

    public PsiClass getServiceRuntimeExceptionClass() {
        return CodingUtils.findClassInAllScopeByName("ServiceRuntimeException", "entity", directory.getProject());
    }

    public PsiClass getApiReturnCodeClass() {
        return CodingUtils.findClassInAllScopeByName("ApiReturnCode", "entity", directory.getProject());
    }

    public String pluralize(String word) {
        return StringUtil.pluralize(word);
    }

    @Override
    public int hashCode() {
        return getQualifiedName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PsiClassProxy
            && Objects.equals(getQualifiedName(), ((PsiClassProxy) obj).getQualifiedName());
    }
}
