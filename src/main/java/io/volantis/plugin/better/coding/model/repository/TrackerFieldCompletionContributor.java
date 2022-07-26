package io.volantis.plugin.better.coding.model.repository;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiField;
import io.volantis.plugin.better.coding.proxy.DtoProxy;
import io.volantis.plugin.better.coding.proxy.EntityProxy;
import io.volantis.plugin.better.coding.proxy.PersisterProxy;
import io.volantis.plugin.better.coding.proxy.RepoProxy;

public class TrackerFieldCompletionContributor extends RepoMemberForEntityContributor {
    @Override
    protected void collectSuggestedMembersForEntity(CompletionResultSet result, RepoProxy repoProxy, EntityProxy entityProxy, DtoProxy dtoProxy) {
        for (PersisterProxy persisterProxy : PersisterProxy.suggestedForRepo(repoProxy, true)) {
            ClassMemberHandler handler = new FieldHandlerImpl(repoProxy, entityProxy, dtoProxy, persisterProxy);
            LookupElement lookupElement = handler.buildLookupElement();
            if (lookupElement != null) {
                result.addElement(lookupElement);
            }
        }
    }

    public static class FieldHandlerImpl extends ClassFieldHandler {
        private final RepoProxy repoProxy;
        private final EntityProxy entityProxy;
        private final DtoProxy dtoProxy;
        private final PersisterProxy persisterProxy;
        public FieldHandlerImpl(RepoProxy repoProxy, EntityProxy entityProxy, DtoProxy dtoProxy, PersisterProxy persisterProxy) {
            this.repoProxy = repoProxy;
            this.entityProxy = entityProxy;
            this.dtoProxy = dtoProxy;
            this.persisterProxy = persisterProxy;
        }

        @Override
        protected String getFieldName() {
            return repoProxy.getTrackerFieldName(entityProxy);
        }

        @Override
        protected String getFieldTypeText() {
            return "EntityTracker<" + entityProxy.getClassName() + ">";
        }

        @Override
        public String getLookupText() {
            return getFieldTypeText() + " " + getFieldName() + " by " + persisterProxy.getClassName();
        }

        @Override
        public PsiField generateMember() {
            return repoProxy.createTrackerField(entityProxy, dtoProxy, persisterProxy);
        }
    }
}
