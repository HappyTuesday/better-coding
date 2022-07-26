package io.nick.plugin.better.coding.model.repository;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.*;
import io.nick.plugin.better.coding.proxy.DtoProxy;
import io.nick.plugin.better.coding.proxy.EntityProxy;
import io.nick.plugin.better.coding.proxy.RepoProxy;

public class RestoreEntityCompletionContributor extends RepoMemberForEntityContributor {
    @Override
    protected void collectSuggestedMembersForEntity(CompletionResultSet result, RepoProxy repoProxy, EntityProxy entityProxy, DtoProxy dtoProxy) {
        ClassMemberHandler handlerOfEntity = new MethodHandlerOfEntityImpl(repoProxy, entityProxy, dtoProxy);
        LookupElement elementOfEntity = handlerOfEntity.buildLookupElement();
        if (elementOfEntity != null) {
            result.addElement(elementOfEntity);
        }
        ClassMemberHandler handlerOfEntities = new MethodHandlerOfEntitiesImpl(repoProxy, entityProxy, dtoProxy);
        LookupElement elementOfEntities = handlerOfEntities.buildLookupElement();
        if (elementOfEntities != null) {
            result.addElement(elementOfEntities);
        }
    }

    public static class MethodHandlerOfEntityImpl extends ClassMethodHandler {
        private final RepoProxy repoProxy;
        private final EntityProxy entityProxy;
        private final DtoProxy dtoProxy;

        public MethodHandlerOfEntityImpl(RepoProxy repoProxy, EntityProxy entityProxy, DtoProxy dtoProxy) {
            this.repoProxy = repoProxy;
            this.entityProxy = entityProxy;
            this.dtoProxy = dtoProxy;
        }

        public String getMethodName() {
            return repoProxy.getRestoreEntityMethodName(entityProxy);
        }

        public String getMethodSignature() {
            return String.format("private %s %s(%s %s)",
                entityProxy.getClassName(), getMethodName(), dtoProxy.getQualifiedName(), "dto"
            );
        }

        @Override
        public String getLookupText() {
            return getMethodName();
        }

        @Override
        public PsiMethod generateMember() {
            return repoProxy.createRestoreEntityMethod(entityProxy, dtoProxy);
        }
    }

    public static class MethodHandlerOfEntitiesImpl extends ClassMethodHandler {
        private final RepoProxy repoProxy;
        private final EntityProxy entityProxy;
        private final DtoProxy dtoProxy;

        public MethodHandlerOfEntitiesImpl(RepoProxy repoProxy, EntityProxy entityProxy, DtoProxy dtoProxy) {
            this.repoProxy = repoProxy;
            this.entityProxy = entityProxy;
            this.dtoProxy = dtoProxy;
        }

        public String getMethodName() {
            return repoProxy.getRestoreEntitiesMethodName(entityProxy);
        }

        public String getMethodSignature() {
            return String.format("private java.util.List<%s> %s(java.util.List<%s> %s)",
                entityProxy.getClassName(), getMethodName(), dtoProxy.getQualifiedName(), "dtos"
            );
        }

        @Override
        public String getLookupText() {
            return getMethodName();
        }

        @Override
        public PsiMethod generateMember() {
            return repoProxy.createRestoreEntitiesMethod(entityProxy, dtoProxy);
        }
    }
}
