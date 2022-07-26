package io.volantis.plugin.better.coding.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;

import java.util.LinkedHashMap;
import java.util.Map;

public class ClassTemplate {
    private final PsiDirectory directory;
    private final String className;
    private final Map<String, Object> templateParams;

    public ClassTemplate(PsiDirectory directory, String className) {
        this.directory = directory;
        this.className = className;
        this.templateParams = new LinkedHashMap<>();
    }

    public ClassTemplate pass(String name, Object value) {
        templateParams.put(name, value);
        return this;
    }

    public PsiClass create(String templateName) {
        String text = CodeTemplate.INSTANCE.render(templateName, templateParams).stripLeading();
        if (StringUtil.isEmptyOrSpaces(text)) {
            return null;
        }
        return CodingUtils.createJavaClass(directory, className, text);
    }

    public PsiClass create(String templateName, String templateBody) {
        String text = CodeTemplate.INSTANCE.render(templateName, templateBody, templateParams).stripLeading();
        if (StringUtil.isEmptyOrSpaces(text)) {
            return null;
        }
        return CodingUtils.createJavaClass(directory, className, text);
    }
}
