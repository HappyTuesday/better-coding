package io.volantis.plugin.better.coding.proxy;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.javadoc.PsiDocTagValue;
import io.volantis.plugin.better.coding.utils.CodingUtils;

public class InfoField {
    public final PsiField psiField;

    public InfoField(PsiField psiField) {
        this.psiField = psiField;
    }

    public DtoField getSourceField() {
        Source source = getSource();
        if (source == null) {
            return null;
        }
        PsiClass dtoClass = CodingUtils.findClassInProjectByName(source.dtoClassName, psiField.getProject());
        if (dtoClass == null) {
            return null;
        }
        DtoProxy dtoProxy = new DtoProxy(dtoClass);
        return dtoProxy.getDtoField(source.dtoFieldName);
    }

    public Source getSource() {
        PsiDocComment comment = psiField.getDocComment();
        if (comment == null) {
            return null;
        }
        PsiDocTag docTag = comment.findTagByName("source");
        if (docTag == null) {
            return null;
        }
        PsiDocTagValue tagValue = docTag.getValueElement();
        if (tagValue == null) {
            return null;
        }
        String text = tagValue.getText();
        if (StringUtil.isEmptyOrSpaces(text)) {
            return null;
        }
        int hashIndex = text.indexOf('#');
        if (hashIndex < 0) {
            return null;
        }
        return new Source(text.substring(0, hashIndex), text.substring(hashIndex + 1));
    }

    public String getName() {
        return psiField.getName();
    }

    public PsiType getType() {
        return psiField.getType();
    }

    public static class Source {
        public final String dtoClassName;
        public final String dtoFieldName;

        public Source(String dtoClassName, String dtoFieldName) {
            this.dtoClassName = dtoClassName;
            this.dtoFieldName = dtoFieldName;
        }
    }
}
