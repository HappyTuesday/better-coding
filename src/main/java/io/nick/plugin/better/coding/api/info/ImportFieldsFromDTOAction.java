package io.nick.plugin.better.coding.api.info;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.WriteActionAware;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import io.nick.plugin.better.coding.proxy.DtoField;
import io.nick.plugin.better.coding.proxy.InfoProxy;
import io.nick.plugin.better.coding.utils.CodingUtils;
import io.nick.plugin.better.coding.utils.ImportFieldsFromDTODialog;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ImportFieldsFromDTOAction extends AnAction implements UpdateInBackground, WriteActionAware, DumbAware {
    public ImportFieldsFromDTOAction() {
        super("Import Fields from DTO", "Import Fields from DTO", null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiClass infoClass = CodingUtils.retrieveContainingClass(e);
        if (infoClass == null) {
            return;
        }
        ImportFieldsFromDTODialog dialog = new ImportFieldsFromDTODialog(infoClass);
        dialog.show();
        List<DtoField> dtoFields = dialog.getSelectedDtoFields();
        if (!dtoFields.isEmpty()) {
            InfoProxy infoProxy = new InfoProxy(infoClass);
            CodingUtils.modifyPsi(infoProxy.getProject(), "Import Fields From DTO",
                () -> infoProxy.importFieldsFromDTO(dtoFields)
            );
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Presentation presentation = event.getPresentation();
        if (ActionPlaces.isPopupPlace(event.getPlace())) {
            Editor editor = event.getData(CommonDataKeys.EDITOR);
            presentation.setEnabledAndVisible(isEnabled(event) && editor != null);
        }
        else {
            presentation.setEnabled(isEnabled(event));
        }
    }

    private static boolean isEnabled(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return false;
        }
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        return editor != null;
    }
}
