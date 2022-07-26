package io.volantis.plugin.better.coding.model.repository;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiClass;
import io.volantis.plugin.better.coding.proxy.DtoProxy;
import io.volantis.plugin.better.coding.proxy.EntityProxy;
import io.volantis.plugin.better.coding.proxy.RepoProxy;

public abstract class RepoMemberForEntityContributor extends RepoMemberContributor {
    @Override
    protected void collectSuggestedMembers(CompletionResultSet result, PsiClass psiClass) {
        RepoProxy repoProxy = new RepoProxy(psiClass);
        for (PsiClass entityClass : repoProxy.relatedEntityClasses()) {
            EntityProxy entityProxy = new EntityProxy(entityClass);
            PsiClass dtoClass = entityProxy.relatedDTOClass();
            if (dtoClass == null) continue;
            DtoProxy dtoProxy = new DtoProxy(dtoClass);
            collectSuggestedMembersForEntity(result, repoProxy, entityProxy, dtoProxy);
        }
    }

    protected abstract void collectSuggestedMembersForEntity(CompletionResultSet result, RepoProxy repoProxy, EntityProxy entityProxy, DtoProxy dtoProxy);
}
