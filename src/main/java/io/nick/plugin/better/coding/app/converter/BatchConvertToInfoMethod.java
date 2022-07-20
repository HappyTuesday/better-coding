package io.nick.plugin.better.coding.app.converter;

import com.intellij.psi.PsiMethod;
import io.nick.plugin.better.coding.app.AppSchema;
import io.nick.plugin.better.coding.app.QueryModel;
import io.nick.plugin.better.coding.proxy.ConverterProxy;
import io.nick.plugin.better.coding.proxy.CopierProxy;
import io.nick.plugin.better.coding.proxy.FetcherProxy;
import io.nick.plugin.better.coding.utils.MethodTemplate;

public class BatchConvertToInfoMethod extends BaseMethod {
    public BatchConvertToInfoMethod(ConverterProxy converterProxy, QueryModel queryModel) {
        super(converterProxy, queryModel);
    }

    public String getMethodName() {
        return ConverterProxy.getBatchConvertToInfoMethodName(queryModel.target, queryModel.from.dtoProxy);
    }

    @Override
    public PsiMethod generateMethod() {
        return new MethodTemplate(converterProxy.createClassIfNotExist())
            .pass("method", this)
            .build("converter/batch-convert.ftl");
    }

    @Override
    public void addDependencies(AppSchema appSchema) {
        CopierProxy copierProxy = appSchema.getCopierProxy(queryModel.from.dtoProxy);
        copierProxy.addCopyToInfoMethod(queryModel.target, queryModel.from.dtoProxy);
        converterProxy.addCopierField(queryModel.from.dtoProxy, copierProxy);

        for (QueryModel.Join join : queryModel.joins) {
            FetcherProxy fetcherProxy = appSchema.getFetcherProxy(join.source.dtoProxy);
            fetcherProxy.addFindByFiltersMethod(join.source.dtoProxy, join.source.orderByClause, false, join.getFieldFilters(true));
            addCommonDependencies(appSchema, join, fetcherProxy);
        }
    }
}