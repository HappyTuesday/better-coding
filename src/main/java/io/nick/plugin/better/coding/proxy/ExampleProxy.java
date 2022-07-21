package io.nick.plugin.better.coding.proxy;

import com.intellij.psi.PsiClass;
import io.nick.plugin.better.coding.utils.CodingUtils;

public class ExampleProxy extends PsiClassProxy {
    public ExampleProxy(PsiClass psiClass) {
        super(psiClass);
    }

    @Override
    protected PsiClass doCreateClass() {
        throw new UnsupportedOperationException();
    }

    public static ExampleProxy forDto(DtoProxy dtoProxy) {
        String fullClassName = dtoProxy.getQualifiedName() + "Example";
        PsiClass psiClass = CodingUtils.findClassInProjectByFullName(fullClassName, dtoProxy.getProject());
        if (psiClass == null) {
            return null;
        }
        return new ExampleProxy(psiClass);
    }
}
