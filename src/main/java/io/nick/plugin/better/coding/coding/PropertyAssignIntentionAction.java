package io.nick.plugin.better.coding.coding;

import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PropertyAssignIntentionAction extends BaseElementAtCaretIntentionAction {
    @Override
    public @NotNull String getText() {
        return "Assign properties";
    }

    @Override
    public @NotNull String getFamilyName() {
        return "Coding assist";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (!(element instanceof PsiIdentifier)) {
            return false;
        }
        if (!(element.getParent() instanceof PsiReferenceExpression)) {
            return false;
        }
        PsiReferenceExpression current = (PsiReferenceExpression) element.getParent();
        if (!(current.getParent() instanceof PsiAssignmentExpression)) {
            return false;
        }
        PsiAssignmentExpression assign = (PsiAssignmentExpression) current.getParent();
        PsiExpression left = assign.getLExpression();
        if (!(left.getType() instanceof PsiClassType)) {
            return false;
        }
        PsiExpression right = assign.getRExpression();
        if (right == null) {
            return false;
        }
        return right.getType() instanceof PsiClassType;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiReferenceExpression current = (PsiReferenceExpression) element.getParent();
        if (!(current.getType() instanceof PsiClassType)) {
            return;
        }
        PsiClass currentType = ((PsiClassType) current.getType()).resolve();
        if (currentType == null) {
            return;
        }

        PsiAssignmentExpression assign = (PsiAssignmentExpression) current.getParent();
        PsiExpression left = assign.getLExpression();
        if (!(left.getType() instanceof PsiClassType)) {
            return;
        }
        PsiClass leftType = ((PsiClassType) left.getType()).resolve();
        if (leftType == null) {
            return;
        }
        PsiExpression right = assign.getRExpression();
        if (right == null) {
            return;
        }
        if (!(right.getType() instanceof PsiClassType)) {
            return;
        }
        PsiClass rightType = ((PsiClassType) right.getType()).resolve();
        if (rightType == null) {
            return;
        }

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        List<PsiStatement> statements = new ArrayList<>();
        Set<PsiField> assignedFields = new LinkedHashSet<>();

        String leftText = left.getText();
        String rightText = right.getText();

        for (PsiField field : currentType.getAllFields()) {
            if (!assignedFields.add(field)) {
                continue;
            }
            String fieldName = field.getName();
            String text;

            PsiField leftField = leftType.findFieldByName(fieldName, true);
            if (memberInConsider(leftField, element, leftType)) {
                PsiField rightField = rightType.findFieldByName(fieldName, true);
                if (memberInConsider(rightField, element, rightType)) {
                    text = String.format("%s.%s = %s.%s;", leftText, leftField.getName(), rightText, rightField.getName());
                } else {
                    PsiMethod rightGetter = PropertyUtil.findPropertyGetter(rightType, fieldName, false, true);
                    if (!memberInConsider(rightGetter, element, rightType)) {
                        continue;
                    }
                    text = String.format("%s.%s = %s.%s();", leftText, leftField.getName(), rightText, rightGetter.getName());
                }
            } else {
                PsiMethod leftSetter = PropertyUtil.findPropertySetter(leftType, fieldName, false, true);
                if (!memberInConsider(leftSetter, element, leftType)) {
                    continue;
                }
                PsiField rightField = rightType.findFieldByName(fieldName, true);
                if (memberInConsider(rightField, element, rightType)) {
                    text = String.format("%s.%s(%s.%s);", leftText, leftSetter.getName(), rightText, rightField.getName());
                } else {
                    PsiMethod rightGetter = PropertyUtil.findPropertyGetter(rightType, fieldName, false, true);
                    if (!memberInConsider(rightGetter, element, rightType)) {
                        continue;
                    }
                    text = String.format("%s.%s(%s.%s());", leftText, leftSetter.getName(), rightText, rightGetter.getName());
                }
            }
            PsiStatement statement = factory.createStatementFromText(text, element);
            statements.add(statement);
        }

        if (statements.isEmpty()) {
            return;
        }

        PsiElement assignStatement = assign.getParent();
        PsiElement block = assignStatement.getParent();
        for (PsiStatement statement : statements) {
            block.addBefore(statement, assignStatement);
        }
        assignStatement.delete();
    }

    private static boolean memberInConsider(PsiMember member, PsiElement location, PsiClass accessObjectClass) {
        if (member == null) {
            return false;
        }
        if (!PsiUtil.isAccessible(member, location, accessObjectClass)) {
            return false;
        }
        PsiModifierList modifiers = member.getModifierList();
        return modifiers == null || !modifiers.hasModifierProperty(PsiModifier.STATIC);
    }
}
