package io.nick.plugin.better.coding.model.repository;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import io.nick.plugin.better.coding.proxy.DtoProxy;
import io.nick.plugin.better.coding.proxy.EntityProxy;
import io.nick.plugin.better.coding.proxy.PersisterProxy;
import io.nick.plugin.better.coding.proxy.RepoProxy;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityOfKeyCompletionContributor extends RepoMemberForEntityContributor {
    private static final Set<String> SUPPORTED_KEY_TYPES = Stream.of(
        byte.class, Byte.class,
        short.class, Short.class,
        int.class, Integer.class,
        long.class, Long.class,
        String.class
    ).map(Class::getSimpleName).collect(Collectors.toSet());

    @Override
    protected void collectSuggestedMembersForEntity(CompletionResultSet result, RepoProxy repoProxy, EntityProxy entityProxy, DtoProxy dtoProxy) {
        for (PsiField field : entityProxy.getFields()) {
            PsiType type = field.getType();
            if (!(type instanceof PsiClassType)) continue;
            String className = ((PsiClassType) type).getClassName();
            if (!SUPPORTED_KEY_TYPES.contains(className)) continue;
            for (PersisterProxy persisterProxy : PersisterProxy.suggestedForRepo(repoProxy, true)) {
                ClassMemberHandler handlerOfKey = new MethodHandlerOfKeyImpl(repoProxy, entityProxy, dtoProxy, persisterProxy, field);
                LookupElement elementOfKey = handlerOfKey.buildLookupElement();
                if (elementOfKey != null) {
                    result.addElement(elementOfKey);
                }
                ClassMemberHandler handlerOfKeys = new MethodHandlerOfKeysImpl(repoProxy, entityProxy, dtoProxy, persisterProxy, field);
                LookupElement elementOfKeys = handlerOfKeys.buildLookupElement();
                if (elementOfKeys != null) {
                    result.addElement(elementOfKeys);
                }
            }
        }
    }

    public static class MethodHandlerOfKeyImpl extends ClassMethodHandler {
        private final RepoProxy repoProxy;
        private final EntityProxy entityProxy;
        private final DtoProxy dtoProxy;
        private final PersisterProxy persisterProxy;
        private final PsiField keyField;

        public MethodHandlerOfKeyImpl(RepoProxy repoProxy, EntityProxy entityProxy, DtoProxy dtoProxy, PersisterProxy persisterProxy, PsiField keyField) {
            this.repoProxy = repoProxy;
            this.entityProxy = entityProxy;
            this.dtoProxy = dtoProxy;
            this.persisterProxy = persisterProxy;
            this.keyField = keyField;
        }

        public String getMethodName() {
            return repoProxy.getEntityOfKeyMethodName(entityProxy, keyField);
        }

        public String getMethodSignature() {
            return String.format("private %s %s(%s %s)",
                entityProxy.getClassName(), getMethodName(), keyField.getType().getPresentableText(), keyField.getName()
            );
        }

        @Override
        public String getLookupText() {
            return getMethodName() + "By" + persisterProxy.getClassName();
        }

        @Override
        public PsiMethod generateMember() {
            return repoProxy.createEntityOfKeyMethod(entityProxy, persisterProxy, dtoProxy, keyField);
        }
    }

    public static class MethodHandlerOfKeysImpl extends ClassMethodHandler {
        private final RepoProxy repoProxy;
        private final EntityProxy entityProxy;
        private final DtoProxy dtoProxy;
        private final PersisterProxy persisterProxy;
        private final PsiField keyField;

        public MethodHandlerOfKeysImpl(RepoProxy repoProxy, EntityProxy entityProxy, DtoProxy dtoProxy, PersisterProxy persisterProxy, PsiField keyField) {
            this.repoProxy = repoProxy;
            this.entityProxy = entityProxy;
            this.dtoProxy = dtoProxy;
            this.persisterProxy = persisterProxy;
            this.keyField = keyField;
        }

        public String getMethodName() {
            return repoProxy.getEntityOfKeysMethodName(entityProxy, keyField);
        }

        public String getMethodSignature() {
            return String.format("private java.lang.List<%s> %s(java.lang.List<%s> %s)",
                entityProxy.getClassName(), getMethodName(), keyField.getType().getPresentableText(), StringUtil.pluralize(keyField.getName())
            );
        }

        @Override
        public String getLookupText() {
            return getMethodName() + "By" + persisterProxy.getClassName();
        }

        @Override
        public PsiMethod generateMember() {
            return repoProxy.createEntityOfKeysMethod(entityProxy, persisterProxy, dtoProxy, keyField);
        }
    }
}
