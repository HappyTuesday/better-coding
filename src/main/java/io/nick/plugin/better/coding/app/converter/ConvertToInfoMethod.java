package io.nick.plugin.better.coding.app.converter;

import com.intellij.psi.PsiMethod;
import io.nick.plugin.better.coding.app.AppSchema;
import io.nick.plugin.better.coding.app.FieldFilter;
import io.nick.plugin.better.coding.app.QueryModel;
import io.nick.plugin.better.coding.proxy.ConverterProxy;
import io.nick.plugin.better.coding.proxy.CopierProxy;
import io.nick.plugin.better.coding.proxy.FetcherProxy;
import io.nick.plugin.better.coding.utils.CodeTemplate;
import io.nick.plugin.better.coding.utils.MethodTemplate;

import java.util.Collections;
import java.util.List;

public class ConvertToInfoMethod extends BaseMethod {
    public ConvertToInfoMethod(ConverterProxy converterProxy, QueryModel queryModel) {
        super(converterProxy, queryModel);
    }

    public String getMethodName() {
        return ConverterProxy.getConvertToInfoMethodName(queryModel.target, queryModel.from.dtoProxy);
    }

    @Override
    public PsiMethod generateMethod() {
        return new MethodTemplate(converterProxy.createClassIfNotExist())
            .pass("method", this)
            .build("converter/convert.ftl");
    }

    public String generateText() {
        return CodeTemplate.INSTANCE
            .render("converter/convert.ftl", Collections.singletonMap("method", this));
    }

    @Override
    public void addDependencies(AppSchema appSchema) {
        CopierProxy copierProxy = appSchema.getCopierProxy(queryModel.from.dtoProxy);
        copierProxy.addCopyToInfoMethod(queryModel.target, queryModel.from.dtoProxy);
        converterProxy.addCopierField(queryModel.from.dtoProxy, copierProxy);

        for (QueryModel.Join join : queryModel.joins) {
            FetcherProxy fetcherProxy = appSchema.getFetcherProxy(join.source.dtoProxy);
            List<FieldFilter> filters = join.getFieldFilters(!queryModel.joinIsSingular(join.referredSource));
            fetcherProxy.addFindByFiltersMethod(join.source.dtoProxy, join.source.orderByClause, false, filters);
            addCommonDependencies(appSchema, join, fetcherProxy);
        }
    }
}