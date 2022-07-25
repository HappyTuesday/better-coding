package io.nick.plugin.better.coding.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import io.nick.plugin.better.coding.utils.CodeTemplate;
import io.nick.plugin.better.coding.utils.CodingUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

@State(
    name = "io.nick.plugin.better.coding.settings.BetterCodingSettings",
    storages = @Storage("better-coding-settings.xml")
)
public class BetterCodingSettings implements PersistentStateComponent<BetterCodingSettings.State> {
    public static class State implements Serializable {
        public Set<String> logicalDeleteFields;
        public String infoFieldTemplate;
        public String infoClassTemplate;
        public String entityTrackerClass;
        public String entityTrackersClass;
        public String entityNotFoundTemplate;
        public Map<String, String> fieldAssignTemplates;
    }

    private final Project project;
    private State state = new State();

    public static BetterCodingSettings getInstance(Project project) {
        return project.getService(BetterCodingSettings.class);
    }

    public BetterCodingSettings(Project project) {
        this.project = project;
    }

    @Override
    public @Nullable BetterCodingSettings.State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull BetterCodingSettings.State state) {
        this.state = state;
    }

    public Set<String> getLogicalDeleteFields() {
        if (state.logicalDeleteFields != null && !state.logicalDeleteFields.isEmpty()) {
            return state.logicalDeleteFields;
        } else {
            return new HashSet<>(Arrays.asList("markedAsDeleted", "is_deleted", "is_delete", "deleted"));
        }
    }

    public void setLogicalDeleteFields(Set<String> logicalDeleteFields) {
        state.logicalDeleteFields = logicalDeleteFields;
    }

    public String getInfoFieldTemplate() {
        if (StringUtil.isNotEmpty(state.infoFieldTemplate)) {
            return state.infoFieldTemplate;
        } else {
            return CodeTemplate.getTemplate("info/add-field-from-dto.ftl");
        }
    }

    public void setInfoFieldTemplate(String infoFieldTemplate) {
        state.infoFieldTemplate = infoFieldTemplate;
    }

    public String getInfoClassTemplate() {
        if (StringUtil.isNotEmpty(state.infoClassTemplate)) {
            return state.infoClassTemplate;
        } else {
            return CodeTemplate.getTemplate("info/info-class.ftl");
        }
    }

    public void setInfoClassTemplate(String infoClassTemplate) {
        state.infoClassTemplate = infoClassTemplate;
    }

    public String getEntityTrackerClass() {
        if (StringUtil.isNotEmpty(state.entityTrackerClass)) {
            return state.entityTrackerClass;
        } else {
            return findSuitableClassFullName("EntityTracker", project);
        }
    }

    public void setEntityTrackerClass(String entityTrackerClass) {
        state.entityTrackerClass = entityTrackerClass;
    }

    public String getEntityTrackersClass() {
        if (StringUtil.isNotEmpty(state.entityTrackersClass)) {
            return state.entityTrackersClass;
        } else {
            return findSuitableClassFullName("EntityTrackers", project);
        }
    }

    public void setEntityTrackersClass(String entityTrackersClass) {
        state.entityTrackersClass = entityTrackersClass;
    }

    public String getEntityNotFoundTemplate() {
        if (StringUtil.isNotEmpty(state.entityNotFoundTemplate)) {
            return state.entityNotFoundTemplate;
        } else {
            return CodeTemplate.getTemplate("repository/entity-not-found.ftl");
        }
    }

    public void setEntityNotFoundTemplate(String entityNotFoundTemplate) {
        state.entityNotFoundTemplate = entityNotFoundTemplate;
    }

    public Map<String, String> getFieldAssignTemplates() {
        if (state.fieldAssignTemplates != null && !state.fieldAssignTemplates.isEmpty()) {
            return state.fieldAssignTemplates;
        } else {
            return Collections.emptyMap();
        }
    }

    public void setFieldAssignTemplates(Map<String, String> fieldAssignTemplates) {
        state.fieldAssignTemplates = fieldAssignTemplates;
    }

    private static String findSuitableClassFullName(String simpleName, Project project) {
        List<PsiClass> psiClasses = CodingUtils.findAllClassInAllScopeByName(simpleName, project);
        if (!psiClasses.isEmpty() && psiClasses.get(0).getQualifiedName() != null) {
            return psiClasses.get(0).getQualifiedName();
        } else {
            return simpleName;
        }
    }
}
