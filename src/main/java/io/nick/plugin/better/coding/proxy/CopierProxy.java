package io.nick.plugin.better.coding.proxy;

import com.intellij.ide.fileTemplates.JavaTemplateUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import io.nick.plugin.better.coding.utils.CodingUtils;
import io.nick.plugin.better.coding.utils.MethodTemplate;

public class CopierProxy extends PsiClassProxy {
    public CopierProxy(PsiDirectory directory, String className) {
        super(directory, className);
    }

    public CopierProxy(PsiClass psiClass) {
        super(psiClass);
    }

    @Override
    public PsiClass createClassIfNotExist() {
        if (psiClass != null) {
            return psiClass;
        }
        psiClass = CodingUtils.createJavaClass(className, JavaTemplateUtil.INTERNAL_CLASS_TEMPLATE_NAME, directory);
        afterClassCreated();
        return psiClass;
    }

    public void afterClassCreated() {
        PsiElementFactory factory = getFactory();
        String serviceFullName = "org.springframework.stereotype.Service";
        if (!psiClass.hasAnnotation(serviceFullName)) {
            PsiAnnotation serviceAnnotation = factory.createAnnotationFromText("@" + serviceFullName, psiClass);
            psiClass.addBefore(serviceAnnotation, psiClass.getModifierList());
        }
        CodingUtils.shortenClassReferences(psiClass);
    }

    public static String getCopyToInfoMethodName(InfoProxy target, DtoProxy source) {
        return "copyTo";
    }

    public void addCopyToInfoMethod(InfoProxy target, DtoProxy source) {
        PsiMethod method = createCopyToInfoMethod(target, source);
        if (method != null) {
            addMethod(method);
        }
    }

    public PsiMethod createCopyToInfoMethod(InfoProxy target, DtoProxy source) {
        createClassIfNotExist();
        return new MethodTemplate(psiClass)
            .pass("copierProxy", this)
            .pass("targetProxy", target)
            .pass("sourceProxy", source)
            .build("copier/copy-to-info.ftl");
    }

    public static CopierProxy forDTO(DtoProxy dtoProxy, PsiDirectory defaultFolder) {
        String copierClassName = StringUtil.trimEnd(dtoProxy.className, "DTO") + "Copier";
        PsiClass copierClass = CodingUtils.findClassInProjectByName(copierClassName, dtoProxy.getProject());
        if (copierClass != null) {
            return new CopierProxy(copierClass);
        } else {
            return new CopierProxy(defaultFolder, copierClassName);
        }
    }
}
