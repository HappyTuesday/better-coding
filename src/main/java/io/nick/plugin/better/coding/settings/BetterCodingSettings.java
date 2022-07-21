package io.nick.plugin.better.coding.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import io.nick.plugin.better.coding.utils.CodeTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@State(
    name = "io.nick.plugin.better.coding.settings.BetterCodingSettings",
    storages = @Storage("better-coding-settings.xml")
)
public class BetterCodingSettings implements PersistentStateComponent<BetterCodingSettings> {

    private Set<String> logicalDeleteFields = new HashSet<>(Arrays.asList("markedAsDeleted", "is_deleted", "is_delete", "deleted"));
    private String infoFieldTemplate = CodeTemplate.getTemplate("info/add-field-from-dto.ftl");
    private String infoClassTemplate = CodeTemplate.getTemplate("info/info-class.ftl");
    private String entityTrackerClass = "EntityTracker";
    private String entityTrackersClass = "EntityTrackers";
    private String entityNotFoundTemplate = CodeTemplate.getTemplate("repository/entity-not-found.ftl");

    public static BetterCodingSettings getInstance(Project project) {
        return project.getService(BetterCodingSettings.class);
    }

    @Override
    public @Nullable BetterCodingSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull BetterCodingSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public Set<String> getLogicalDeleteFields() {
        return logicalDeleteFields;
    }

    public void setLogicalDeleteFields(Set<String> logicalDeleteFields) {
        this.logicalDeleteFields = logicalDeleteFields;
    }

    public String getInfoFieldTemplate() {
        return infoFieldTemplate;
    }

    public void setInfoFieldTemplate(String infoFieldTemplate) {
        this.infoFieldTemplate = infoFieldTemplate;
    }

    public String getInfoClassTemplate() {
        return infoClassTemplate;
    }

    public void setInfoClassTemplate(String infoClassTemplate) {
        this.infoClassTemplate = infoClassTemplate;
    }

    public String getEntityTrackerClass() {
        return entityTrackerClass;
    }

    public void setEntityTrackerClass(String entityTrackerClass) {
        this.entityTrackerClass = entityTrackerClass;
    }

    public String getEntityTrackersClass() {
        return entityTrackersClass;
    }

    public void setEntityTrackersClass(String entityTrackersClass) {
        this.entityTrackersClass = entityTrackersClass;
    }

    public String getEntityNotFoundTemplate() {
        return entityNotFoundTemplate;
    }

    public void setEntityNotFoundTemplate(String entityNotFoundTemplate) {
        this.entityNotFoundTemplate = entityNotFoundTemplate;
    }
}
