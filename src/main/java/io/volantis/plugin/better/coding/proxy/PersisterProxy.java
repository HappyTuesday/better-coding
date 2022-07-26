package io.volantis.plugin.better.coding.proxy;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import io.volantis.plugin.better.coding.utils.CodingUtils;
import io.volantis.plugin.better.coding.utils.MethodTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PersisterProxy extends PsiClassProxy {

    public PersisterProxy(PsiDirectory directory, String className) {
        super(directory, className);
    }

    public PersisterProxy(PsiClass psiClass) {
        super(psiClass);
    }

    @Override
    protected PsiClass doCreateClass() {
        return classTemplate().pass("persisterProxy", this).create("persister/persister-class.ftl");
    }

    public String getUpdateDTOMethodName(DtoProxy dtoProxy) {
        return "update" + StringUtil.trimEnd(dtoProxy.className, "DTO");
    }

    public String getFindDTOByKeyMethodName(DtoField keyField) {
        return "find" + StringUtil.trimEnd(keyField.dtoProxy.className, "DTO") + "By" + StringUtil.capitalize(keyField.getName());
    }

    public String getFindDTOByKeysMethodName(DtoField keyField) {
        return "find" + StringUtil.trimEnd(keyField.dtoProxy.className, "DTO") + "By" + StringUtil.pluralize(StringUtil.capitalize(keyField.getName()));
    }

    public void addUpdateEntityMethod(DtoProxy dtoProxy) {
        createClassIfNotExist();
        PsiMethod method = methodTemplate()
            .pass("dtoProxy", dtoProxy)
            .build("persister/update-entity.ftl");
        if (method != null) {
            addMethod(method);
        }
    }

    private MethodTemplate methodTemplate() {
        createClassIfNotExist();
        return new MethodTemplate(psiClass).pass("persisterProxy", this);
    }

    public static PersisterProxy forRepo(RepoProxy repoProxy) {
        String className = repoProxy.className.replace("Repository", "Persister");
        PsiClass psiClass = CodingUtils.findClassInDirectory(className, repoProxy.directory);
        if (psiClass != null) {
            return new PersisterProxy(psiClass);
        } else {
            return new PersisterProxy(repoProxy.directory, className);
        }
    }

    public static List<PersisterProxy> suggestedForRepo(RepoProxy repoProxy, boolean keepAllChoices) {
        PersisterProxy persisterProxy = PersisterProxy.forRepo(repoProxy);
        if (persisterProxy.isClassCreated() && !keepAllChoices) {
            return Collections.singletonList(persisterProxy);
        }
        List<PersisterProxy> result = new ArrayList<>();
        result.add(persisterProxy);
        repoProxy.listRelatedClasses("Persister").forEach(c -> {
            if (!persisterProxy.getClassName().equals(c.getName())) {
                result.add(new PersisterProxy(c));
            }
        });
        return result;
    }
}
