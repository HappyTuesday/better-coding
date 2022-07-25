package io.nick.plugin.better.coding.coding;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAssignmentExpression;
import com.intellij.psi.PsiClass;
import io.nick.plugin.better.coding.utils.CodeTemplate;
import org.jetbrains.annotations.NotNull;

public class NonNullPropertyAssignIntentionAction extends PropertyAssignIntentionAction {
    @Override
    public @NotNull String getText() {
        return "Assign non null properties";
    }

    @Override
    protected void processAssignExpression(Project project, PsiClass currentType, PsiAssignmentExpression assign, Editor editor) {
        generateAssignments(project, currentType, assign, CodeTemplate.getTemplate("assign/non-null.ftl"));
    }
}
