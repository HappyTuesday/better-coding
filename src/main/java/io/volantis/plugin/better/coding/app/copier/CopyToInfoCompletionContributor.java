package io.volantis.plugin.better.coding.app.copier;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import io.volantis.plugin.better.coding.proxy.ConverterProxy;
import io.volantis.plugin.better.coding.proxy.CopierProxy;
import io.volantis.plugin.better.coding.proxy.DtoProxy;
import io.volantis.plugin.better.coding.proxy.InfoProxy;
import io.volantis.plugin.better.coding.utils.ClassMemberContributor;

import java.util.List;

public class CopyToInfoCompletionContributor extends ClassMemberContributor {
    @Override
    protected boolean shouldSuggest(PsiClass psiClass) {
        return psiClass.getName() != null && psiClass.getName().endsWith("Copier");
    }

    @Override
    protected void collectSuggestedMembers(CompletionResultSet result, PsiClass psiClass) {
        Project project = psiClass.getProject();
        CopierProxy copierProxy = new CopierProxy(psiClass);
        List<DtoProxy> dtoProxies = DtoProxy.listAllDTOsInProject(project);
        for (InfoProxy target : InfoProxy.listAllInfosInProject(project)) {
            for (DtoProxy source : dtoProxies) {
                ClassMemberHandler handler = new MethodHandlerImpl(copierProxy, target, source);
                LookupElement lookupElement = handler.buildLookupElement();
                if (lookupElement != null) {
                    result.addElement(lookupElement);
                }
            }
        }
    }

    public static class MethodHandlerImpl extends ClassMethodHandler {
        private final CopierProxy copierProxy;
        private final InfoProxy target;
        private final DtoProxy source;
        public MethodHandlerImpl(CopierProxy copierProxy, InfoProxy target, DtoProxy source) {
            this.copierProxy = copierProxy;
            this.target = target;
            this.source = source;
        }

        public String getMethodName() {
            return ConverterProxy.getCopyToInfoMethodName(source);
        }

        public String getMethodSignature() {
            return String.format("public %s %s(%s %s, %s %s)",
                target.getClassName(), getMethodName(), target.getClassName(), target.getVarName(), source.getClassName(), source.getVarName()
            );
        }

        @Override
        public PsiMethod generateMember() {
            return copierProxy.createCopyToInfoMethod(target, source);
        }

        @Override
        public String getLookupText() {
            return getMethodName() + target.getClassName() + "From" + source.getClassName();
        }
    }
}
