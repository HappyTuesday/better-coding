package io.nick.plugin.better.coding.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

import java.util.LinkedHashMap;
import java.util.Map;

public class MethodTemplate {
    private final PsiClass declaringClass;
    private final Map<String, Object> templateParams;

    public MethodTemplate(PsiClass declaringClass) {
        this.declaringClass = declaringClass;
        this.templateParams = new LinkedHashMap<>();
    }

    public MethodTemplate pass(String name, Object value) {
        templateParams.put(name, value);
        return this;
    }

    public PsiMethod build(String templateName) {
        String text = CodeTemplate.INSTANCE.render(templateName, templateParams).stripLeading();
        if (StringUtil.isEmptyOrSpaces(text)) {
            return null;
        }
        return JavaPsiFacade.getElementFactory(declaringClass.getProject()).createMethodFromText(text, declaringClass);
    }

    public String render(String templateName) {
        String text = CodeTemplate.INSTANCE.render(templateName, templateParams).stripLeading();
        if (StringUtil.isEmptyOrSpaces(text)) {
            return null;
        } else {
            return text;
        }
    }
}
