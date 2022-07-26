package io.volantis.plugin.better.coding.model.repository;

import com.intellij.psi.PsiClass;
import io.volantis.plugin.better.coding.utils.ClassMemberContributor;

import java.util.Arrays;

public abstract class RepoMemberContributor extends ClassMemberContributor {
    @Override
    protected boolean shouldSuggest(PsiClass psiClass) {
        return psiClass != null
            && Arrays.stream(psiClass.getAnnotations())
            .anyMatch(a -> a.getQualifiedName() != null && a.getQualifiedName().endsWith("Repository"));
    }
}
