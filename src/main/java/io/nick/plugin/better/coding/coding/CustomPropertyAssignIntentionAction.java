package io.nick.plugin.better.coding.coding;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiUtil;
import io.nick.plugin.better.coding.settings.BetterCodingSettings;
import io.nick.plugin.better.coding.utils.CodeTemplate;
import io.nick.plugin.better.coding.utils.CodingUtils;
import io.nick.plugin.better.coding.utils.PsiTypeHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomPropertyAssignIntentionAction extends PropertyAssignIntentionAction {
    @Override
    public @NotNull String getText() {
        return "Assign properties with templates";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return super.isAvailable(project, editor, element) && !BetterCodingSettings.getInstance(project).getFieldAssignTemplates().isEmpty();
    }

    @Override
    protected void processAssignExpression(Project project, PsiClass currentType, PsiAssignmentExpression assign, Editor editor) {
        Map<String, String> fieldAssignTemplates = BetterCodingSettings.getInstance(project).getFieldAssignTemplates();
        if (fieldAssignTemplates.isEmpty()) {
            return;
        }

        JBPopupFactory.getInstance()
            .createListPopup(new TemplateListPopupStep(project, currentType, assign, fieldAssignTemplates))
            .showInBestPositionFor(editor);
    }

    private class TemplateListPopupStep extends BaseListPopupStep<String> {
        private final Project project;
        private final PsiClass currentType;
        private final PsiAssignmentExpression assign;
        private final Map<String, String> fieldAssignTemplates;
        public TemplateListPopupStep(Project project, PsiClass currentType, PsiAssignmentExpression assign, Map<String, String> fieldAssignTemplates) {
            super("Choose Template", new ArrayList<>(fieldAssignTemplates.keySet()));
            this.project = project;
            this.currentType = currentType;
            this.assign = assign;
            this.fieldAssignTemplates = fieldAssignTemplates;
        }

        @Override
        public @Nullable PopupStep<?> onChosen(String selectedValue, boolean finalChoice) {
            String template = fieldAssignTemplates.get(selectedValue);
            CodingUtils.modifyPsi(project, "Property Assign", () ->
                generateAssignments(project, currentType, assign, template)
            );
            return null;
        }
    }
}
