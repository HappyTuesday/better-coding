package io.volantis.plugin.better.coding.app.fetcher;

import com.intellij.psi.PsiType;
import io.volantis.plugin.better.coding.app.FieldFilter;
import io.volantis.plugin.better.coding.app.FieldFilterMode;
import io.volantis.plugin.better.coding.proxy.DtoProxy;
import io.volantis.plugin.better.coding.utils.MethodParameter;
import io.volantis.plugin.better.coding.utils.TypeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class FindByFiltersMethod {
    public final DtoProxy dtoProxy;
    public final List<FieldFilter> filters;
    public final OrderByClause orderByClause;
    public final boolean withLimit;

    public FindByFiltersMethod(DtoProxy dtoProxy, OrderByClause orderByClause, boolean withLimit, List<FieldFilter> filters) {
        this.dtoProxy = dtoProxy;
        this.filters = filters;
        this.withLimit = withLimit;
        this.orderByClause = orderByClause;
    }

    public String getMethodName() {
        return computeMethodName(orderByClause, filters, withLimit);
    }

    public List<MethodParameter> getParameters() {
        List<MethodParameter> params = new ArrayList<>(filters.size());
        for (FieldFilter filter : filters) {
            MethodParameter param = new MethodParameter(filter.getVar());
            PsiType fieldType = dtoProxy.getDtoField(filter.fieldName).getType();
            if (filter.filterMode == FieldFilterMode.IN_LIST) {
                param.paramType = TypeUtils.listOf(dtoProxy.getProject(), fieldType);
            } else {
                param.paramType = fieldType;
            }
            params.add(param);
        }
        if (withLimit) {
            MethodParameter limit = new MethodParameter("limit");
            limit.paramType = PsiType.INT;
            limit.desc = "the max records to fetch";
            params.add(limit);
            MethodParameter offset = new MethodParameter("offset");
            offset.paramType = PsiType.INT;
            offset.desc = "the offset of which to fetch ";
            params.add(offset);
        }
        return params;
    }

    public static String computeMethodName(OrderByClause orderByClause, List<FieldFilter> filters) {
        return computeMethodName(orderByClause, filters, false);
    }

    public static String computeMethodName(OrderByClause orderByClause, List<FieldFilter> filters, boolean includingLimit) {
        StringBuilder sb = new StringBuilder();
        sb.append("findList");
        if (!filters.isEmpty()) {
            sb.append("By");
            for (FieldFilter filter : filters) {
                sb.append(StringUtils.capitalize(filter.getVar()));
            }
        }
        if (orderByClause != null) {
            sb.append("OrderBy");
            sb.append(StringUtils.capitalize(orderByClause.fieldName));
            if (orderByClause.descending) {
                sb.append("Desc");
            }
        }
        if (includingLimit) {
            sb.append("WithLimit");
        }
        return sb.toString();
    }
}
