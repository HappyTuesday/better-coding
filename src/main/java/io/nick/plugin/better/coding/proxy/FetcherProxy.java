package io.nick.plugin.better.coding.proxy;

import com.intellij.ide.fileTemplates.JavaTemplateUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import io.nick.plugin.better.coding.app.FieldFilter;
import io.nick.plugin.better.coding.app.fetcher.FindByFiltersMethod;
import io.nick.plugin.better.coding.app.fetcher.OrderByClause;
import io.nick.plugin.better.coding.utils.CodingUtils;
import io.nick.plugin.better.coding.utils.MethodTemplate;

import java.util.List;

public class FetcherProxy extends PsiClassProxy {
    public FetcherProxy(PsiDirectory directory, String className) {
        super(directory, className);
    }

    public FetcherProxy(PsiClass psiClass) {
        super(psiClass);
    }

    @Override
    protected PsiClass doCreateClass() {
        return classTemplate().pass("fetcherProxy", this).create("fetcher/fetcher-class.ftl");
    }

    public void addFindByFiltersMethod(DtoProxy dtoProxy, OrderByClause orderByClause, boolean withLimit, List<FieldFilter> filters) {
        createClassIfNotExist();
        addMapperField(dtoProxy);
        PsiMethod method = createFindByFiltersMethod(dtoProxy, orderByClause, withLimit, filters);
        if (method != null) {
            addMethod(method);
        }
    }

    public PsiMethod createFindByFiltersMethod(DtoProxy dtoProxy, OrderByClause orderByClause, boolean withLimit, List<FieldFilter> filters) {
        createClassIfNotExist();
        return new MethodTemplate(psiClass)
            .pass("method", new FindByFiltersMethod(dtoProxy, orderByClause, withLimit, filters))
            .build("fetcher/find-by-filters.ftl");
    }

    private void addMapperField(DtoProxy dtoProxy) {
        MapperProxy mapperProxy = dtoProxy.getMapperProxy();
        if (mapperProxy != null) {
            addInjectedField(mapperProxy.getMapperFieldName(), mapperProxy.createType());
        }
    }

    public static FetcherProxy forDTO(DtoProxy dtoProxy, PsiDirectory defaultFolder) {
        String fetcherClassName = StringUtil.trimEnd(dtoProxy.className, "DTO") + "Fetcher";
        PsiClass fetcherClass = CodingUtils.findClassInProjectByName(fetcherClassName, dtoProxy.getProject());
        if (fetcherClass != null) {
            return new FetcherProxy(fetcherClass);
        } else {
            return new FetcherProxy(defaultFolder, fetcherClassName);
        }
    }
}
