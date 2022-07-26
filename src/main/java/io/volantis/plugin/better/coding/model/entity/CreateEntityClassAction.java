package io.volantis.plugin.better.coding.model.entity;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.WriteActionAware;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.util.PlatformIcons;
import io.volantis.plugin.better.coding.proxy.EntityProxy;
import io.volantis.plugin.better.coding.utils.CodingUtils;
import io.volantis.plugin.better.coding.utils.ImportFieldsFromDTODialog;
import io.volantis.plugin.better.coding.proxy.DtoField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import javax.swing.*;
import java.util.List;
import java.util.Set;

public class CreateEntityClassAction extends AnAction implements UpdateInBackground, WriteActionAware, DumbAware {
    @Nullable
    private final Set<? extends JpsModuleSourceRootType<?>> mySourceRootTypes;

    public CreateEntityClassAction() {
        this("Entity Class", "Entity Class", PlatformIcons.CLASS_ICON, JavaModuleSourceRootTypes.SOURCES);
    }

    public CreateEntityClassAction(String text, String description, Icon icon, @Nullable Set<? extends JpsModuleSourceRootType<?>> rootTypes) {
        super(text, description, icon);
        this.mySourceRootTypes = rootTypes;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();

        IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
        if (view == null) {
            return;
        }

        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        PsiDirectory dir = view.getOrChooseDirectory();
        if (dir == null || project == null) {
            return;
        }

        ImportFieldsFromDTODialog dialog = new ImportFieldsFromDTODialog(dir);
        dialog.show();
        if (dialog.getExitCode() != DialogWrapper.OK_EXIT_CODE) {
            return;
        }
        List<DtoField> dtoFields = dialog.getSelectedDtoFields();

        String entityClassName = StringUtil.trimEnd(dialog.getSelectedDtoProxy().getClassName(), "DTO");
        EntityProxy entityProxy = new EntityProxy(dir, entityClassName);
        CodingUtils.modifyPsi(project, "Import Fields From DTO", () -> {
            for (DtoField dtoField : dtoFields) {
                entityProxy.importFieldFromDTO(dtoField);
            }
        });
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Presentation presentation = e.getPresentation();
        boolean enabled = CodingUtils.canAddClassHere(dataContext, mySourceRootTypes);
        presentation.setEnabledAndVisible(enabled);
    }
}
