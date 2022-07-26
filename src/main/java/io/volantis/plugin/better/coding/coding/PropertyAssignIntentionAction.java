package io.volantis.plugin.better.coding.coding;

import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.IncorrectOperationException;
import io.volantis.plugin.better.coding.utils.CodeTemplate;
import io.volantis.plugin.better.coding.utils.PsiTypeHelper;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class PropertyAssignIntentionAction extends BaseElementAtCaretIntentionAction {
    @Override
    public @NotNull String getFamilyName() {
        return "Coding assist";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
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
        processAssignExpression(project, currentType, assign, editor);
    }

    protected abstract void processAssignExpression(Project project, PsiClass currentType, PsiAssignmentExpression assign, Editor editor);

    protected void generateAssignments(Project project, PsiClass currentType, PsiAssignmentExpression assign, String assignTemplate) {
        PsiExpression leftExpr = assign.getLExpression();
        if (!(leftExpr.getType() instanceof PsiClassType)) {
            return;
        }
        PsiClass leftType = ((PsiClassType) leftExpr.getType()).resolve();
        if (leftType == null) {
            return;
        }
        PsiExpression rightExpr = assign.getRExpression();
        if (rightExpr == null) {
            return;
        }
        if (!(rightExpr.getType() instanceof PsiClassType)) {
            return;
        }
        PsiClass rightType = ((PsiClassType) rightExpr.getType()).resolve();
        if (rightType == null) {
            return;
        }

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        List<PsiStatement> statements = new ArrayList<>();

        List<String> propertyNamesToConsider = Stream.concat(
                Arrays.stream(currentType.getAllFields())
                    .map(PsiField::getName),
                Arrays.stream(currentType.getAllMethods())
                    .map(PropertyUtil::getPropertyName)
                    .filter(Objects::nonNull)
            )
            .distinct()
            .collect(Collectors.toList());

        PsiTypeHelper typeHelper = new PsiTypeHelper();

        for (String propertyName : propertyNamesToConsider) {
            PsiField leftField = filterSuitableField(leftType.findFieldByName(propertyName, true), assign, leftType);
            PsiMethod leftSetter = filterSuitableMethod(PropertyUtil.findPropertySetter(leftType, propertyName, false, true), assign, leftType);
            PsiField rightField = filterSuitableField(rightType.findFieldByName(propertyName, true), assign, rightType);
            PsiMethod rightGetter = filterSuitableMethod(PropertyUtil.findPropertyGetter(rightType, propertyName, false, true), assign, rightType);
            if (leftField == null && leftSetter == null || rightField == null && rightGetter == null) {
                continue;
            }

            PsiType leftPropertyType = leftField != null ? leftField.getType() : PropertyUtil.getPropertyType(leftSetter);
            PsiType rightPropertyType = rightField != null ? rightField.getType() : PropertyUtil.getPropertyType(rightGetter);

            Map<String, Object> params = new LinkedHashMap<>();
            params.put("propertyName", propertyName);
            params.put("leftPropertyType", leftPropertyType);
            params.put("leftExpr", leftExpr);
            params.put("leftField", leftField);
            params.put("leftSetter", leftSetter);
            params.put("rightExpr", rightExpr);
            params.put("rightPropertyType", rightPropertyType);
            params.put("rightField", rightField);
            params.put("rightGetter", rightGetter);
            params.put("typeHelper", typeHelper);

            String text = CodeTemplate.INSTANCE.render("assign-property", assignTemplate, params).stripLeading();
            if (StringUtil.isEmpty(text)) {
                continue;
            }

            PsiStatement statement = factory.createStatementFromText(text, assign);
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

    protected static PsiMethod filterSuitableMethod(PsiMethod method, PsiElement location, PsiClass accessObjectClass) {
        return memberInConsider(method, location, accessObjectClass) ? method : null;
    }

    protected static PsiField filterSuitableField(PsiField field, PsiElement location, PsiClass accessObjectClass) {
        return memberInConsider(field, location, accessObjectClass) ? field : null;
    }

    protected static boolean memberInConsider(PsiMember member, PsiElement location, PsiClass accessObjectClass) {
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
