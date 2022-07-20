package io.nick.plugin.better.coding.model.repository;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.*;
import io.nick.plugin.better.coding.proxy.DtoProxy;
import io.nick.plugin.better.coding.proxy.EntityProxy;
import io.nick.plugin.better.coding.proxy.RepoProxy;

public class EntityToDTOCompletionContributor extends RepoMemberForEntityContributor {
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

        public String getMethodName() {
            return repoProxy.convertToDTOMethodName(entityProxy);
        }

        public String getMethodSignature() {
            return String.format("private %s %s(%s entity)", dtoProxy.getClassName(), getMethodName(), entityProxy.getClassName());
        }

        @Override
        public PsiMethod generateMember() {
            return repoProxy.createConvertToDTOMethod(entityProxy, dtoProxy);
        }

        @Override
        public String getLookupText() {
            return getMethodName();
        }
    }
}
