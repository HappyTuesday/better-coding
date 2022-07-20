package io.nick.plugin.better.coding.model.repository;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import io.nick.plugin.better.coding.proxy.PersisterProxy;
import io.nick.plugin.better.coding.proxy.RepoProxy;

public class PersisterFieldCompletionContributor extends RepoMemberContributor {
    @Override
    protected void collectSuggestedMembers(CompletionResultSet result, PsiClass psiClass) {
        RepoProxy repoProxy = new RepoProxy(psiClass);
        for (PersisterProxy persisterProxy : PersisterProxy.suggestedForRepo(repoProxy, true)) {
            ClassMemberHandler handler = new FieldHandlerImpl(repoProxy, persisterProxy);
            LookupElement lookupElement = handler.buildLookupElement();
            if (lookupElement != null) {
                result.addElement(lookupElement);
            }
        }
    }

    public static class FieldHandlerImpl extends ClassFieldHandler {
        private final RepoProxy repoProxy;
        private final PersisterProxy persisterProxy;
        public FieldHandlerImpl(RepoProxy repoProxy, PersisterProxy persisterProxy) {
            this.repoProxy = repoProxy;
            this.persisterProxy = persisterProxy;
        }

        @Override
        protected String getFieldName() {
            return repoProxy.getPersisterFieldName(persisterProxy);
        }

        @Override
        protected String getFieldTypeText() {
            return persisterProxy.getClassName();
        }

        @Override
        public String getLookupText() {
            return getFieldTypeText() + " " + getFieldName();
        }

        @Override
        public PsiField generateMember() {
            return repoProxy.createPersisterField(persisterProxy);
        }
    }
}
