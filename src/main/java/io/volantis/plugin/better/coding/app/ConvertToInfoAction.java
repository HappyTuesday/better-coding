package io.volantis.plugin.better.coding.app;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.WriteActionAware;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiDirectory;
import com.intellij.util.PlatformIcons;
import io.volantis.plugin.better.coding.proxy.ConverterProxy;
import io.volantis.plugin.better.coding.utils.CodingUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import javax.swing.*;
import java.util.List;
import java.util.Set;

public class ConvertToInfoAction extends AnAction implements UpdateInBackground, WriteActionAware, DumbAware {
    @Nullable
    private final Set<? extends JpsModuleSourceRootType<?>> mySourceRootTypes;

    public ConvertToInfoAction() {
        this("Convert To Info", "Convert To Info", PlatformIcons.CLASS_ICON, JavaModuleSourceRootTypes.SOURCES);
    }

    public ConvertToInfoAction(String text, String description, Icon icon, @Nullable Set<? extends JpsModuleSourceRootType<?>> rootTypes) {
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
        PsiDirectory dir = view.getOrChooseDirectory();
        if (dir == null) {
            return;
        }
        QueryModelBuilderDialog dialog = new QueryModelBuilderDialog(dir);
        dialog.show();
        if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            List<QueryModel> queryModels = dialog.buildModels();
            createConvertMethods(dir, queryModels);
        }
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Presentation presentation = e.getPresentation();
        boolean enabled = CodingUtils.canAddClassHere(dataContext, mySourceRootTypes) && isAvailableIn(dataContext);
        presentation.setEnabledAndVisible(enabled);
    }

    private static boolean isAvailableIn(DataContext dataContext) {
        IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
        if (view == null) {
            return false;
        }
        PsiDirectory dir = view.getOrChooseDirectory();
        if (dir == null) {
            return false;
        }
        return dir.findSubdirectory("converters") != null
            || dir.findSubdirectory("converter") != null;
    }

    private static class QueryModelBuilderDialog extends DialogWrapper {
        private final QueryModelPanel queryModelPanel;

        public QueryModelBuilderDialog(PsiDirectory appDirectory) {
            super(appDirectory.getProject());
            setTitle("Build Query Model");
            this.queryModelPanel = new QueryModelPanel(appDirectory);
            init();
            getOKAction().putValue(Action.NAME, "Create");
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
            return queryModelPanel;
        }

        public List<QueryModel> buildModels() {
            return queryModelPanel.buildModels();
        }
    }

    private static void createConvertMethods(PsiDirectory appDirectory, List<QueryModel> queryModels) {
        CodingUtils.modifyPsi(appDirectory.getProject(), "Create Converter Methods", () -> {
            for (QueryModel queryModel : queryModels) {
                AppSchema appSchema = new AppSchema(appDirectory);
                ConverterProxy converterProxy = appSchema.getConverterProxy(queryModel.from.dtoProxy);
                converterProxy.addConvertToInfoMethod(queryModel, appSchema);
                converterProxy.addBatchConvertToInfoMethod(queryModel, appSchema);
            }
        });
    }
}
