package io.nick.plugin.better.coding.proxy;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import io.nick.plugin.better.coding.utils.*;

import java.util.*;

public class RepoProxy extends PsiClassProxy {

    public RepoProxy(PsiDirectory directory, String className) {
        super(directory, className);
    }

    public RepoProxy(PsiClass psiClass) {
        super(psiClass);
    }

    @Override
    protected PsiClass doCreateClass() {
        return classTemplate().pass("repoProxy", this).create("repository/repository-class.ftl");
    }

    public String getPersisterFieldName(PersisterProxy persisterProxy) {
        return StringUtil.decapitalize(persisterProxy.className);
    }

    public String getTrackerFieldName(EntityProxy entityProxy) {
        return StringUtil.decapitalize(entityProxy.className) + "Tracker";
    }

    public String getRestoreEntityMethodName(EntityProxy entityProxy) {
        return "restore" + entityProxy.className;
    }

    public String getRestoreEntitiesMethodName(EntityProxy entityProxy) {
        return "restore" + StringUtil.pluralize(entityProxy.className);
    }

    public String convertToDTOMethodName(EntityProxy entityProxy) {
        return StringUtil.decapitalize(entityProxy.className) + "ToDTO";
    }

    public String initFromDTOMethodName(EntityProxy entityProxy) {
        return "init" + entityProxy.className + "FromDTO";
    }

    public String getEntityOfKeyMethodName(EntityProxy entityProxy, DtoField keyField) {
        return String.format("%sOf%s", StringUtil.decapitalize(entityProxy.className), StringUtil.capitalize(keyField.getName()));
    }

    public String getEntityOfKeysMethodName(EntityProxy entityProxy, DtoField keyField) {
        return String.format("%sOf%s", StringUtil.decapitalize(entityProxy.className), StringUtil.pluralize(StringUtil.capitalize(keyField.getName())));
    }

    public void addPersisterField(PersisterProxy persisterProxy) {
        createClassIfNotExist();
        PsiField field = createPersisterField(persisterProxy);
        if (field != null) {
            addField(field);
        }
    }

    public PsiField createPersisterField(PersisterProxy persisterProxy) {
        createClassIfNotExist();
        return fieldTemplate()
            .pass("persisterProxy", persisterProxy)
            .generate("repository/persister-field.ftl");
    }

    public void addTrackerField(EntityProxy entityProxy, DtoProxy dtoProxy, PersisterProxy persisterProxy) {
        createClassIfNotExist();
        PsiField field = createTrackerField(entityProxy, dtoProxy, persisterProxy);
        if (field != null) {
            addField(field);
        }
    }

    public PsiField createTrackerField(EntityProxy entityProxy, DtoProxy dtoProxy, PersisterProxy persisterProxy) {
        createClassIfNotExist();
        return fieldTemplate()
            .pass("entityProxy", entityProxy)
            .pass("dtoProxy", dtoProxy)
            .pass("persisterProxy", persisterProxy)
            .generate("repository/tracker-field.ftl");
    }

    public void addRestoreEntitiesMethod(EntityProxy entityProxy, DtoProxy dtoProxy) {
        createClassIfNotExist();
        PsiMethod restoreManyMethod = createRestoreEntitiesMethod(entityProxy, dtoProxy);
        if (restoreManyMethod != null) {
            addMethod(restoreManyMethod);
        }
    }

    public PsiMethod createRestoreEntitiesMethod(EntityProxy entityProxy, DtoProxy dtoProxy) {
        createClassIfNotExist();
        return methodTemplate()
            .pass("entityProxy", entityProxy)
            .pass("dtoProxy", dtoProxy)
            .build("repository/restore-entities.ftl");
    }

    public void addRestoreEntityMethod(EntityProxy entityProxy, DtoProxy dtoProxy) {
        createClassIfNotExist();
        PsiMethod method = createRestoreEntityMethod(entityProxy, dtoProxy);
        if (method != null) {
            addMethod(method);
        }
    }

    public PsiMethod createRestoreEntityMethod(EntityProxy entityProxy, DtoProxy dtoProxy) {
        createClassIfNotExist();
        return methodTemplate()
            .pass("entityProxy", entityProxy)
            .pass("dtoProxy", dtoProxy)
            .build("repository/restore-entity.ftl");
    }

    public void addInitFromDTOMethod(EntityProxy entityProxy, DtoProxy dtoProxy) {
        createClassIfNotExist();
        PsiMethod method = createInitFromDTOMethod(entityProxy, dtoProxy);
        if (method != null) {
            addMethod(method);
        }
    }

    public PsiMethod createInitFromDTOMethod(EntityProxy entityProxy, DtoProxy dtoProxy) {
        createClassIfNotExist();
        return methodTemplate()
            .pass("entityProxy", entityProxy)
            .pass("dtoProxy", dtoProxy)
            .build("repository/init-from-dto.ftl");
    }

    public void addConvertToDTOMethod(EntityProxy entityProxy, DtoProxy dtoProxy) {
        createClassIfNotExist();
        PsiMethod method = createConvertToDTOMethod(entityProxy, dtoProxy);
        if (method != null) {
            addMethod(method);
        }
    }

    public PsiMethod createConvertToDTOMethod(EntityProxy entityProxy, DtoProxy dtoProxy) {
        createClassIfNotExist();
        return methodTemplate()
            .pass("dtoProxy", dtoProxy)
            .pass("entityProxy", entityProxy)
            .build("repository/convert-to-dto.ftl");
    }

    public PsiMethod createEntityOfKeyMethod(EntityProxy entityProxy, PersisterProxy persisterProxy, DtoField keyField) {
        createClassIfNotExist();
        return methodTemplate()
            .pass("keyField", keyField)
            .pass("entityProxy", entityProxy)
            .pass("persisterProxy", persisterProxy)
            .build("repository/entity-of-key.ftl");
    }

    public PsiMethod createEntityOfKeysMethod(EntityProxy entityProxy, PersisterProxy persisterProxy, DtoField keyField) {
        createClassIfNotExist();
        return methodTemplate()
            .pass("keyField", keyField)
            .pass("entityProxy", entityProxy)
            .pass("persisterProxy", persisterProxy)
            .build("repository/entity-of-keys.ftl");
    }

    public void shortenClassReferences() {
        CodingUtils.shortenClassReferences(psiClass);
    }

    public List<PsiClass> relatedEntityClasses() {
        return EntityHelper.entityClassesInDir(directory);
    }

    public String renderEntityNotFound(EntityProxy entityProxy, DtoField keyField) {
        Map<String, Object> params = new HashMap<>();
        params.put("entityProxy", entityProxy);
        params.put("keyField", keyField);
        String template = getSettings().getEntityNotFoundTemplate();
        return CodeTemplate.INSTANCE.render("entity-not-found", template, params);
    }

    private MethodTemplate methodTemplate() {
        createClassIfNotExist();
        return new MethodTemplate(psiClass).pass("repoProxy", this);
    }

    private FieldTemplate fieldTemplate() {
        createClassIfNotExist();
        return new FieldTemplate(psiClass).pass("repoProxy", this);
    }

    public static RepoProxy forEntity(EntityProxy entityProxy) {
        String className = entityProxy.className + "Repository";
        PsiClass psiClass = CodingUtils.findClassInDirectory(className, entityProxy.directory);
        if (psiClass != null) {
            return new RepoProxy(psiClass);
        } else {
            return new RepoProxy(entityProxy.directory, className);
        }
    }

    public static List<RepoProxy> suggestedForEntity(EntityProxy entityProxy) {
        RepoProxy repoProxy = forEntity(entityProxy);
        if (repoProxy.isClassCreated()) {
            return Collections.singletonList(repoProxy);
        }
        List<RepoProxy> result = new ArrayList<>();
        result.add(repoProxy);
        repoProxy.listRelatedClasses("Repository").forEach(c -> result.add(new RepoProxy(c)));
        return result;
    }
}
