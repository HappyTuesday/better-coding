<#-- @ftlvariable name="method" type="io.volantis.plugin.better.coding.app.fetcher.FindByFiltersMethod" -->
<#assign dtoProxy = method.dtoProxy/>
public java.util.List<${dtoProxy.qualifiedName}> ${method.methodName}(<#list method.parameters as p>${p.paramType.canonicalText} ${p.paramName}<#sep>, </#list>) {
<#if dtoProxy.mapperProxy?? && dtoProxy.exampleProxy??>
    <#assign mapperProxy = dtoProxy.mapperProxy/>
    <#assign exampleProxy = dtoProxy.exampleProxy/>
    ${exampleProxy.qualifiedName} example = new ${exampleProxy.qualifiedName}();
    <#if dtoProxy.logicalDeleteField?? || method.filters?size != 0>
        ${exampleProxy.qualifiedName}.Criteria criteria = example.createCriteria();
        <#list method.filters as filter>
            <#if filter.inListMode>
                if (${filter.var} == null || ${filter.var}.isEmpty()) {
                    return java.util.Collections.emptyList();
                }
                criteria.${filter.exampleCriteriaMethod}(${filter.var});
            <#else>
                if (${filter.var} == null) {
                    criteria.${filter.exampleCriteriaIsNullMethod}();
                } else {
                    criteria.${filter.exampleCriteriaMethod}(${filter.var});
                }
            </#if>
        </#list>
        <#if dtoProxy.logicalDeleteField??>
            criteria.and${dtoProxy.logicalDeleteField.name?cap_first}EqualTo(false);
        </#if>
    </#if>
    <#if method.orderByClause??>
        example.setOrderByClause("${method.orderByClause.columnName}<#if method.orderByClause.descending> desc</#if>");
    </#if>
    <#if method.withLimit>
        example.setLimit(limit);
        example.setOffset(offset);
    </#if>
    return ${mapperProxy.mapperFieldName}.selectByExample(example);
<#else>
    // todo implements this method
    return new java.util.ArrayList<>();
</#if>
}