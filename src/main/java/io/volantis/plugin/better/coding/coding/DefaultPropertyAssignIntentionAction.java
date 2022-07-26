package io.volantis.plugin.better.coding.coding;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import io.volantis.plugin.better.coding.utils.CodeTemplate;
import org.jetbrains.annotations.NotNull;

public class DefaultPropertyAssignIntentionAction extends PropertyAssignIntentionAction {
    @Override
    public @NotNull String getText() {
        return "Assign all properties";
    }

    @Override
    protected void processAssignExpression(Project project, PsiClass currentType, PsiAssignmentExpression assign, Editor editor) {
        generateAssignments(project, currentType, assign, CodeTemplate.getTemplate("assign/basic.ftl"));
    }
}
