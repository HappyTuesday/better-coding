package io.volantis.plugin.better.coding.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;

import java.util.LinkedHashMap;
import java.util.Map;

public class FieldTemplate {
    private final PsiClass declaringClass;
    private final Map<String, Object> templateParams;

    public FieldTemplate(PsiClass declaringClass) {
        this.declaringClass = declaringClass;
        this.templateParams = new LinkedHashMap<>();
    }

    public FieldTemplate pass(String name, Object value) {
        templateParams.put(name, value);
        return this;
    }

    public PsiField generate(String templateName) {
        String text = CodeTemplate.INSTANCE.render(templateName, templateParams).stripLeading();
        if (StringUtil.isEmptyOrSpaces(text)) {
            return null;
        }
        return JavaPsiFacade.getElementFactory(declaringClass.getProject()).createFieldFromText(text, declaringClass);
    }

    public PsiField generate(String templateName, String templateBody) {
        String text = CodeTemplate.INSTANCE.render(templateName, templateBody, templateParams).stripLeading();
        if (StringUtil.isEmptyOrSpaces(text)) {
            return null;
        }
        return JavaPsiFacade.getElementFactory(declaringClass.getProject()).createFieldFromText(text, declaringClass);
    }

    public void generateAndAdd(String templateName) {
        PsiField field = generate(templateName);
        if (field != null) {
            CodingUtils.addFieldToClass(field, declaringClass);
        }
    }

    public void generateAndAdd(String templateName, String templateBody) {
        PsiField field = generate(templateName, templateBody);
        if (field != null) {
            CodingUtils.addFieldToClass(field, declaringClass);
        }
    }
}
