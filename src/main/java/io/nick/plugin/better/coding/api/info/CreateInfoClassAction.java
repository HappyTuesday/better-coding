package io.nick.plugin.better.coding.api.info;

import com.intellij.ide.IdeView;
import com.intellij.ide.fileTemplates.JavaTemplateUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.WriteActionAware;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.util.PlatformIcons;
import io.nick.plugin.better.coding.utils.CodingUtils;
import io.nick.plugin.better.coding.utils.ImportFieldsFromDTODialog;
import io.nick.plugin.better.coding.proxy.DtoField;
import io.nick.plugin.better.coding.proxy.InfoProxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import javax.swing.*;
import java.util.List;
import java.util.Set;

public class CreateInfoClassAction extends AnAction implements UpdateInBackground, WriteActionAware, DumbAware {
    @Nullable
    private final Set<? extends JpsModuleSourceRootType<?>> mySourceRootTypes;

    public CreateInfoClassAction() {
        this("Info Class", "Info Class", PlatformIcons.CLASS_ICON, JavaModuleSourceRootTypes.SOURCES);
    }

    public CreateInfoClassAction(String text, String description, Icon icon, @Nullable Set<? extends JpsModuleSourceRootType<?>> rootTypes) {
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

        String infoClassName = dialog.getSelectedDtoProxy().getClassName().replace("DTO", "Info");
        PsiClass infoClass = CodingUtils.createJavaClass(infoClassName, JavaTemplateUtil.INTERNAL_CLASS_TEMPLATE_NAME, dir);
        if (infoClass == null) {
            return;
        }

        CodingUtils.configureSDK(dir);
        InfoProxy infoProxy = new InfoProxy(infoClass);
        CodingUtils.modifyPsi(project, "Import Fields From DTO", () -> {
            infoProxy.afterClassCreated();
            infoProxy.importFieldsFromDTO(dtoFields);
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
