<#-- @ftlvariable name="method" type="io.volantis.plugin.better.coding.app.converter.ConvertToInfoMethod" -->
<#assign queryModel = method.queryModel>
<#macro join_from_dto join>
<#-- @ftlvariable name="join" type="io.volantis.plugin.better.coding.app.QueryModel.Join" -->
    <#local source = join.source.dtoSource/>
    <#local referredSource = join.referredSource/>
    // join ${source.dtoProxy.className} to ${queryModel.target.className}
    <#if referredSource == queryModel.from>
        ${source.dtoProxy.qualifiedName} ${source.dtoVar} = ${join.fetcherFieldName}.${join.fetchMethodName}(${referredSource.dtoVar}.${join.referredField.getter.name}()).stream().findAny().orElse(null);
    <#else>
        ${source.dtoProxy.qualifiedName} ${source.dtoVar};
        if (${join.referredSource.dtoVar} != null) {
            ${source.dtoVar} = ${join.fetcherFieldName}.${join.fetchMethodName}(${referredSource.dtoVar}.${join.referredField.getter.name}()).stream().findAny().orElse(null);
        } else {
            ${source.dtoVar} = null;
        }
    </#if>
    if (${source.dtoVar} != null) {
        ${source.copierFieldName}.${source.copyToInfoMethodName}(${queryModel.target.varName}, ${source.dtoVar});
    }
</#macro>
<#macro join_from_info join>
<#-- @ftlvariable name="join" type="io.volantis.plugin.better.coding.app.QueryModel.Join" -->
    <#local source = join.source.infoSource/>
    <#local referredSource = join.referredSource/>
    // fetch and convert ${source.dtoProxy.className} to ${source.infoProxy.className}, then includes ${source.infoProxy.className} to ${queryModel.target.className}
    <#if queryModel.joinIsSingular(referredSource) && source.integrationMode.singular??>
        <#if referredSource == queryModel.from>
            ${source.dtoProxy.qualifiedName} ${source.dtoVar} = ${join.fetcherFieldName}.${join.fetchMethodName}(${referredSource.dtoVar}.${join.referredField.getter.name}()).stream().findAny().orElse(null);
        <#else>
            ${source.dtoProxy.qualifiedName} ${source.dtoVar};
            if (${referredSource.dtoVar} != null) {
                ${source.dtoVar} = ${join.fetcherFieldName}.${join.fetchMethodName}(${referredSource.dtoVar}.${join.referredField.getter.name}()).stream().findAny().orElse(null);
            } else {
                ${source.dtoVar} = null;
            }
        </#if>
        if (${source.dtoVar} != null) {
            ${queryModel.target.varName}.${source.targetField.name} = ${source.getConverterObject(queryModel.from)}.${source.convertToInfoMethodName}(${source.dtoVar});
        }
    <#else>
        <#if queryModel.joinIsSingular(referredSource)>
            <#if referredSource == queryModel.from>
                java.util.List<${source.dtoProxy.qualifiedName}> ${source.dtosVar} = ${join.fetcherFieldName}.${join.fetchMethodName}(${referredSource.dtoVar}.${join.referredField.getter.name}());
            <#else>
                java.util.List<${source.dtoProxy.qualifiedName}> ${source.dtosVar};
                if (${referredSource.dtoVar} != null) {
                    ${source.dtosVar} = ${join.fetcherFieldName}.${join.fetchMethodName}(${referredSource.dtoVar}.${join.referredField.getter.name}());
                } else {
                    ${source.dtosVar} = java.util.Collections.emptyList();
                }
            </#if>
        <#else>
            java.util.List<${source.dtoProxy.qualifiedName}> ${source.dtosVar} = ${join.fetcherFieldName}.${join.fetchByListMethodName}(
                ${referredSource.dtosVar}.stream().map(${referredSource.dtoProxy.qualifiedName}::${join.referredField.getter.name}).collect(java.util.Collectors.toList())
            );
        </#if>
        <#if source.integrationMode.list??>
            if (!${source.dtosVar}.isEmpty()) {
                ${queryModel.target.varName}.${source.targetField.name} = ${source.getConverterObject(queryModel.from)}.${source.batchConvertToInfoMethodName}(${source.dtosVar});
            } else {
                ${queryModel.target.varName}.${source.targetField.name} = java.util.Collections.emptyList();
            }
        <#elseif source.integrationMode.map??>
            <#local asMap = source.integrationMode.map/>
            if (!${source.dtosVar}.isEmpty()) {
                ${queryModel.target.varName}.${source.targetField.name} = ${source.getConverterObject(queryModel.from)}.${source.batchConvertToInfoMethodName}(${source.dtosVar})
                .stream()
                .collect(java.util.stream.Collectors.toMap(x -> x.${asMap.keyField}, java.util.function.Function.identity());
            } else {
                ${queryModel.target.varName}.${source.targetField.name} = java.util.Collections.emptyMap();
            }
        </#if>
    </#if>
</#macro>
public ${queryModel.target.qualifiedName} ${method.methodName}(${queryModel.from.dtoProxy.qualifiedName} ${queryModel.from.dtoVar}) {
    // convert ${queryModel.from.dtoProxy.className} to ${queryModel.target.className}
    ${queryModel.target.qualifiedName} ${queryModel.target.varName} = new ${queryModel.target.qualifiedName}();
    ${queryModel.from.copierFieldName}.${queryModel.from.copyToInfoMethodName}(${queryModel.target.varName}, ${queryModel.from.dtoVar});
<#list queryModel.joins as join>
    <#if join.source.dtoSource??>
        <@join_from_dto join/>
    <#elseif join.source.infoSource??>
        <@join_from_info join/>
    </#if>
</#list>
    return ${queryModel.target.varName};
}