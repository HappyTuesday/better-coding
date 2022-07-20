package io.nick.plugin.better.coding.app.converter;

import com.intellij.psi.PsiMethod;
import io.nick.plugin.better.coding.app.AppSchema;
import io.nick.plugin.better.coding.app.QueryModel;
import io.nick.plugin.better.coding.proxy.ConverterProxy;
import io.nick.plugin.better.coding.proxy.CopierProxy;
import io.nick.plugin.better.coding.proxy.FetcherProxy;

public abstract class BaseMethod {
    public final ConverterProxy converterProxy;
    public final QueryModel queryModel;

    public BaseMethod(ConverterProxy converterProxy, QueryModel queryModel) {
        this.converterProxy = converterProxy;
        this.queryModel = queryModel;
    }

    public abstract PsiMethod generateMethod();

    public abstract void addDependencies(AppSchema appSchema);

    public void generateAndAdd(AppSchema appSchema) {
        PsiMethod psiMethod = generateMethod();
        if (psiMethod != null && converterProxy.addMethod(psiMethod)) {
            addDependencies(appSchema);
        }
    }

    protected void addCommonDependencies(AppSchema appSchema, QueryModel.Join join, FetcherProxy fetcherProxy) {
        converterProxy.addFetcherField(join.source.dtoProxy, fetcherProxy);

        QueryModel.DtoSource dtoSource = join.source.getDtoSource();
        if (dtoSource != null) {
            CopierProxy copierProxy = appSchema.getCopierProxy(dtoSource.dtoProxy);
            copierProxy.addCopyToInfoMethod(queryModel.target, dtoSource.dtoProxy);
            converterProxy.addCopierField(dtoSource.dtoProxy, copierProxy);
        }

        QueryModel.InfoSource infoSource = join.source.getInfoSource();
        if (infoSource != null && !queryModel.from.dtoProxy.equals(join.source.dtoProxy)) {
            ConverterProxy nestedConverter = appSchema.getConverterProxy(join.source.dtoProxy);
            converterProxy.addConverterField(infoSource.dtoProxy, nestedConverter);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseMethod that = (BaseMethod) o;
        return queryModel.equals(that.queryModel);
    }

    @Override
    public int hashCode() {
        return queryModel.hashCode();
    }
}