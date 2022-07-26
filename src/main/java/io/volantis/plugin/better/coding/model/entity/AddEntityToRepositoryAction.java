package io.volantis.plugin.better.coding.model.entity;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.WriteActionAware;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.psi.*;
import com.intellij.util.PlatformIcons;
import io.volantis.plugin.better.coding.proxy.DtoProxy;
import io.volantis.plugin.better.coding.proxy.EntityProxy;
import io.volantis.plugin.better.coding.proxy.PersisterProxy;
import io.volantis.plugin.better.coding.proxy.RepoProxy;
import io.volantis.plugin.better.coding.utils.CodingUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AddEntityToRepositoryAction extends AnAction implements WriteActionAware, DumbAware {
    public AddEntityToRepositoryAction() {
        super("Add To Repository", "Add to repository", null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiClass entityClass = CodingUtils.retrieveContainingClass(e);
        if (entityClass == null) {
            return;
        }
        EntityProxy entityProxy = new EntityProxy(entityClass);
        List<RepoProxy> repoProxies = RepoProxy.suggestedForEntity(entityProxy);
        if (repoProxies.size() == 1) {
            ListPopupStep<PersisterProxy> step = prepareForPersisterDialog(entityProxy, repoProxies.get(0));
            if (step != null) {
                JBPopupFactory.getInstance()
                    .createListPopup(step)
                    .showInBestPositionFor(e.getDataContext());
            }
            return;
        }
        JBPopupFactory.getInstance()
            .createListPopup(new ChooseRepoPopupStep(entityProxy, repoProxies))
            .showInBestPositionFor(e.getDataContext());
    }

    private ListPopupStep<PersisterProxy> prepareForPersisterDialog(EntityProxy entityProxy, RepoProxy repoProxy) {
        String dtoClassName = entityProxy.getClassName() + "DTO";
        PsiClass dtoClass = CodingUtils.findClassInProjectByName(dtoClassName, entityProxy.getProject());
        if (dtoClass == null) {
            return null;
        }
        DtoProxy dtoProxy = new DtoProxy(dtoClass);
        List<PersisterProxy> persisterProxies = PersisterProxy.suggestedForRepo(repoProxy, false);
        if (persisterProxies.size() == 1) {
            addEntityToRepo(entityProxy, repoProxy, dtoProxy, persisterProxies.get(0));
            return null;
        }
        return new ChoosePersisterPopupStep(entityProxy, repoProxy, dtoProxy, persisterProxies);
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

    private void addEntityToRepo(EntityProxy entityProxy, RepoProxy repoProxy, DtoProxy dtoProxy, PersisterProxy persisterProxy) {
        CodingUtils.modifyPsi(entityProxy.getProject(), "Add Entity To Repository",
            () -> doAddEntityToRepo(entityProxy, repoProxy, dtoProxy, persisterProxy)
        );
    }

    private void doAddEntityToRepo(EntityProxy entityProxy, RepoProxy repoProxy, DtoProxy dtoProxy, PersisterProxy persisterProxy) {
        persisterProxy.addUpdateEntityMethod(dtoProxy);
        repoProxy.addPersisterField(persisterProxy);
        repoProxy.addTrackerField(entityProxy, dtoProxy, persisterProxy);
        repoProxy.addRestoreEntityMethod(entityProxy, dtoProxy);
        repoProxy.addRestoreEntitiesMethod(entityProxy, dtoProxy);
        repoProxy.addInitFromDTOMethod(entityProxy, dtoProxy);
        repoProxy.addConvertToDTOMethod(entityProxy, dtoProxy);
        repoProxy.shortenClassReferences();
    }

    private class ChooseRepoPopupStep extends BaseListPopupStep<RepoProxy> {
        private final EntityProxy entityProxy;

        public ChooseRepoPopupStep(EntityProxy entityProxy, List<RepoProxy> items) {
            super("Choose Repository", items, PlatformIcons.CLASS_ICON);
            this.entityProxy = entityProxy;
        }

        @Override
        public @NotNull String getTextFor(RepoProxy value) {
            return value.getClassName();
        }

        @Override
        public @Nullable PopupStep<?> onChosen(RepoProxy selectedItem, boolean finalChoice) {
            return prepareForPersisterDialog(entityProxy, selectedItem);
        }
    }

    private class ChoosePersisterPopupStep extends BaseListPopupStep<PersisterProxy> {
        private final EntityProxy entityProxy;
        private final RepoProxy repoProxy;
        private final DtoProxy dtoProxy;

        public ChoosePersisterPopupStep(EntityProxy entityProxy, RepoProxy repoProxy, DtoProxy dtoProxy, List<PersisterProxy> items) {
            super("Choose Persister", items, PlatformIcons.INTERFACE_ICON);
            this.entityProxy = entityProxy;
            this.repoProxy = repoProxy;
            this.dtoProxy = dtoProxy;
        }

        @Override
        public @NotNull String getTextFor(PersisterProxy value) {
            return value.getClassName();
        }

        @Override
        public @Nullable PopupStep<?> onChosen(PersisterProxy selectedItem, boolean finalChoice) {
            addEntityToRepo(entityProxy, repoProxy, dtoProxy, selectedItem);
            return null;
        }
    }
}
