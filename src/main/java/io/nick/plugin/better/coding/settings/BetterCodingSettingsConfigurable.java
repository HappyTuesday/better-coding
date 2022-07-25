package io.nick.plugin.better.coding.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import io.nick.plugin.better.coding.utils.CodingUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class BetterCodingSettingsConfigurable implements Configurable {
    private final Project project;
    private BetterCodingSettingsPanel settingsPanel;
    private final BetterCodingSettings settings;

    public BetterCodingSettingsConfigurable(Project project) {
        this.project = project;
        this.settings = BetterCodingSettings.getInstance(project);
    }

    @Override
    public String getDisplayName() {
        return "Better Coding";
    }

    @Override
    public @Nullable JComponent createComponent() {
        return settingsPanel = new BetterCodingSettingsPanel(project);
    }

    @Override
    public boolean isModified() {
        return settingsPanel != null && settingsPanel.isDirty();
    }

    @Override
    public void apply() {
        settings.setLogicalDeleteFields(settingsPanel.getLogicalDeleteFields());
        settings.setInfoFieldTemplate(settingsPanel.getInfoFieldTemplate());
        settings.setInfoClassTemplate(settingsPanel.getInfoClassTemplate());
        settings.setEntityTrackerClass(settingsPanel.getEntityTrackerClass());
        settings.setEntityTrackersClass(settingsPanel.getEntityTrackersClass());
        settings.setEntityNotFoundTemplate(settingsPanel.getEntityNotFoundTemplate());
    }

    @Override
    public void reset() {
        CodingUtils.modifyPsi(project, "reset", () -> {
            settingsPanel.setLogicalDeleteFields(settings.getLogicalDeleteFields());
            settingsPanel.setInfoFieldTemplate(settings.getInfoFieldTemplate());
            settingsPanel.setInfoClassTemplate(settings.getInfoClassTemplate());
            settingsPanel.setEntityTrackerClass(settings.getEntityTrackerClass());
            settingsPanel.setEntityTrackersClass(settings.getEntityTrackersClass());
            settingsPanel.setEntityNotFoundTemplate(settings.getEntityNotFoundTemplate());
            settingsPanel.clearDirty();
        });
    }

    @Override
    public void disposeUIResources() {
        if (settingsPanel == null) {
            return;
        }
        settingsPanel.close();
        settingsPanel = null;
    }
}
