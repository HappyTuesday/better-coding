package io.volantis.plugin.better.coding.model.repository;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.*;
import io.volantis.plugin.better.coding.proxy.DtoProxy;
import io.volantis.plugin.better.coding.proxy.EntityProxy;
import io.volantis.plugin.better.coding.proxy.RepoProxy;

public class EntityInitFromDTOCompletionContributor extends RepoMemberForEntityContributor {
    @Override
    protected void collectSuggestedMembersForEntity(CompletionResultSet result, RepoProxy repoProxy, EntityProxy entityProxy, DtoProxy dtoProxy) {
        ClassMemberHandler handler = new MethodHandlerImpl(repoProxy, entityProxy, dtoProxy);
        LookupElement lookupElement = handler.buildLookupElement();
        if (lookupElement != null) {
            result.addElement(lookupElement);
        }
    }

    public static class MethodHandlerImpl extends ClassMethodHandler {
        private final RepoProxy repoProxy;
        private final EntityProxy entityProxy;
        private final DtoProxy dtoProxy;

        public MethodHandlerImpl(RepoProxy repoProxy, EntityProxy entityProxy, DtoProxy dtoProxy) {
            this.repoProxy = repoProxy;
            this.entityProxy = entityProxy;
            this.dtoProxy = dtoProxy;
        }

        @Override
        public String getMethodName() {
            return repoProxy.initFromDTOMethodName(entityProxy);
        }

        @Override
        public String getLookupText() {
            return getMethodName();
        }

        @Override
        public String getMethodSignature() {
            return String.format("%s(%s entity, %s dto)",
                getMethodName(), entityProxy.getClassName(), dtoProxy.getClassName()
            );
        }

        @Override
        public PsiMethod generateMember() {
            return repoProxy.createInitFromDTOMethod(entityProxy, dtoProxy);
        }
    }
}
