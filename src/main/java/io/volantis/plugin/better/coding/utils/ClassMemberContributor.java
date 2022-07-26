package io.volantis.plugin.better.coding.utils;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.codeInsight.generation.GenerateMembersUtil;
import com.intellij.codeInsight.generation.GenerationInfo;
import com.intellij.codeInsight.generation.PsiGenerationInfo;
import com.intellij.codeInsight.generation.TemplateGenerationInfo;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInspection.ex.GlobalInspectionContextBase;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.PlatformIcons;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.FList;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public abstract class ClassMemberContributor extends CompletionContributor {
    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        if (parameters.getCompletionType() != CompletionType.BASIC && parameters.getCompletionType() != CompletionType.SMART) {
            return;
        }
        PsiElement position = parameters.getPosition();
        if (DumbService.getInstance(position.getProject()).isDumb()) {
            return;
        }
        if (psiElement(PsiIdentifier.class).withParents(PsiJavaCodeReferenceElement.class, PsiTypeElement.class, PsiClass.class).
            andNot(JavaKeywordCompletion.AFTER_DOT).accepts(position)) {
            PsiElement prevLeaf = PsiTreeUtil.prevVisibleLeaf(position);
            PsiModifierList modifierList = PsiTreeUtil.getParentOfType(prevLeaf, PsiModifierList.class);
            if (modifierList != null) {
                String fileText = position.getContainingFile().getText();
                result = result.withPrefixMatcher(new ClassMemberContributor.NoMiddleMatchesAfterSpace(
                    fileText.substring(modifierList.getTextRange().getStartOffset(), parameters.getOffset())));
            }
            suggestGeneratedMembers(result, position);
        }
    }

    private void suggestGeneratedMembers(CompletionResultSet result, PsiElement position) {
        PsiClass psiClass = CompletionUtil.getOriginalElement(Objects.requireNonNull(PsiTreeUtil.getParentOfType(position, PsiClass.class)));
        if (shouldSuggest(psiClass)) {
            collectSuggestedMembers(result, psiClass);
        }
    }

    protected abstract boolean shouldSuggest(PsiClass psiClass);

    protected abstract void collectSuggestedMembers(CompletionResultSet result, PsiClass psiClass);

    public static abstract class ClassMemberHandler implements InsertHandler<LookupElement> {
        public LookupElement buildLookupElement() {
            LookupElementBuilder element = LookupElementBuilder
                .create(this, getLookupText())
                .withInsertHandler(this);
            return decorateLookupElement(element);
        }

        public abstract String getLookupText();

        protected abstract LookupElementBuilder decorateLookupElement(LookupElementBuilder builder);

        @Override
        public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
            context.getDocument().deleteString(context.getStartOffset(), context.getTailOffset());
            context.commitDocument();
            PsiMember member = generateMember();
            if (member != null) {
                insertGenerationInfos(context, Collections.singletonList(new PsiGenerationInfo<>(member)));
            }
        }

        public abstract PsiMember generateMember();

        private void insertGenerationInfos(InsertionContext context, List<PsiGenerationInfo<PsiMember>> infos) {
            List<PsiGenerationInfo<PsiMember>> newInfos = GenerateMembersUtil
                .insertMembersAtOffset(context.getFile(), context.getStartOffset(), infos);
            if (!newInfos.isEmpty()) {
                final List<PsiElement> elements = new ArrayList<>();
                for (GenerationInfo member : newInfos) {
                    if (!(member instanceof TemplateGenerationInfo)) {
                        ContainerUtil.addIfNotNull(elements, member.getPsiMember());
                    }
                }
                PsiGenerationInfo<PsiMember> first = newInfos.get(0);
                first.positionCaret(context.getEditor(), first.getPsiMember() instanceof PsiMethod);
                GlobalInspectionContextBase.cleanupElements(context.getProject(), null, elements.toArray(PsiElement.EMPTY_ARRAY));
            }
        }
    }

    public static abstract class ClassMethodHandler extends ClassMemberHandler {
        @Override
        protected LookupElementBuilder decorateLookupElement(LookupElementBuilder builder) {
            return builder
                .withLookupString(getMethodName())
                .withLookupString(getMethodSignature())
                .appendTailText(" {...}", true)
                .withIcon(PlatformIcons.METHOD_ICON);
        }

        public abstract String getMethodName();

        public abstract String getMethodSignature();
    }

    public static abstract class ClassFieldHandler extends ClassMemberHandler {
        protected abstract String getFieldName();

        protected abstract String getFieldTypeText();

        @Override
        protected LookupElementBuilder decorateLookupElement(LookupElementBuilder builder) {
            return builder
                .withLookupString(getFieldTypeText())
                .withLookupString(getFieldName())
                .withIcon(PlatformIcons.FIELD_ICON);
        }
    }

    private static class NoMiddleMatchesAfterSpace extends CamelHumpMatcher {
        NoMiddleMatchesAfterSpace(String prefix) {
            super(prefix);
        }

        @Override
        public boolean prefixMatches(@NotNull LookupElement element) {
            if (!super.prefixMatches(element)) return false;
            if (!myPrefix.contains(" ")) return true;
            String signature = element.getLookupString();
            FList<TextRange> fragments = matchingFragments(signature);
            return fragments == null || !ContainerUtil.exists(fragments, f -> isMiddleMatch(signature, f));
        }

        private static boolean isMiddleMatch(String signature, TextRange fragment) {
            int start = fragment.getStartOffset();
            return start > 0 &&
                Character.isJavaIdentifierPart(signature.charAt(start)) &&
                Character.isJavaIdentifierPart(signature.charAt(start - 1));
        }
    }
}
