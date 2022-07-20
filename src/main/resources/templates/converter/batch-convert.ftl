<#-- @ftlvariable name="method" type="io.nick.plugin.better.coding.app.converter.BatchConvertToInfoMethod" -->
<#assign queryModel = method.queryModel>
<#macro join_from_dto join>
<#-- @ftlvariable name="join" type="io.nick.plugin.better.coding.app.QueryModel.Join" -->
    <#local source = join.source.dtoSource/>
    <#local referredSource = join.referredSource/>
    // join ${source.dtoProxy.className} to ${queryModel.target.className}
    java.util.List<${source.dtoProxy.qualifiedName}> ${source.dtosVar} = ${join.fetcherFieldName}.${join.fetchByListMethodName}(
        ${referredSource.dtosVar}.stream().map(${referredSource.dtoProxy.qualifiedName}::${join.referredField.getter.name}).filter(java.util.Objects::nonNull).distinct().collect(java.util.stream.Collectors.toList())
    );
    java.util.Map<${join.localField.type.canonicalText}, ${source.dtoProxy.qualifiedName}> ${source.dtoMapVar} = ${source.dtosVar}
        .stream()
        .collect(Collectors.toMap(${source.dtoProxy.qualifiedName}::${join.localField.getter.name}, java.util.function.Function.identity()));
    for (int i = 0; i < ${queryModel.from.dtosVar}.size(); i++) {
    <#list queryModel.computeJoinPath(join) as span>
        <#if span?is_first>
            ${span.prevSource.dtoProxy.qualifiedName} ${span.prevSource.dtoVar} = ${span.prevSource.dtosVar}.get(i);
        </#if>
        ${span.nextSource.dtoProxy.qualifiedName} ${span.nextSource.dtoVar} = ${span.nextSource.dtoMapVar}.get(${span.prevSource.dtoVar}.${span.prevField.getter.name}());
        if (${span.nextSource.dtoVar} == null) {
            continue;
        }
        <#if span?is_last>
            ${source.copierFieldName}.${source.copyToInfoMethodName}(${queryModel.target.varsName}.get(i), ${span.nextSource.dtoVar});
        </#if>
    </#list>
    }
</#macro>
<#macro join_from_info join>
<#-- @ftlvariable name="join" type="io.nick.plugin.better.coding.app.QueryModel.Join" -->
    <#local source = join.source.infoSource/>
    <#local referredSource = join.referredSource/>
    // prepare the data of ${source.dtoProxy.className}
    java.util.List<${source.dtoProxy.qualifiedName}> ${source.dtosVar} = ${join.fetcherFieldName}.${join.fetchByListMethodName}(
        ${referredSource.dtosVar}.stream().map(${referredSource.dtoProxy.qualifiedName}::${join.referredField.getter.name}).filter(java.util.Objects::nonNull).distinct().collect(java.util.stream.Collectors.toList())
    );
    // convert ${source.dtoProxy.className} to ${source.infoProxy.className}
    java.util.List<${source.infoProxy.qualifiedName}> ${source.infosVar} = ${source.getConverterObject(queryModel.from)}.${source.batchConvertToInfoMethodName}(${source.dtosVar});
    // include ${source.infoProxy.className} to ${queryModel.target.className}
    <#if source.integrationMode.singular??>
        <#if queryModel.isSourceJoined(source)>
            java.util.Map<${join.localField.type.canonicalText}, ${source.dtoProxy.qualifiedName}> ${source.dtoMapVar} = new java.util.LinkedHashMap<>(${source.dtosVar}.size());
        </#if>
            java.util.Map<${join.localField.type.canonicalText}, ${source.infoProxy.qualifiedName}> ${source.infoMapVar} = new java.util.LinkedHashMap<>(${source.dtosVar}.size());
            for (int i = 0; i < ${source.dtosVar}.size(); i++) {
                ${source.dtoProxy.qualifiedName} ${source.dtoVar} = ${source.dtosVar}.get(i);
        <#if queryModel.isSourceJoined(source)>
                ${source.dtoMapVar}.put(${source.dtoVar}.${join.localField.getter.name}(), ${source.dtoVar});
        </#if>
                ${source.infoProxy.qualifiedName} ${source.infoVar} = ${source.infosVar}.get(i);
                ${source.infoMapVar}.put(${source.dtoVar}.${join.localField.getter.name}(), ${source.infoVar});
            }
            for (int i = 0; i < ${queryModel.target.varsName}.size(); i++) {
        <#list queryModel.computeJoinPath(join) as span>
            <#if span?is_first>
                ${span.prevSource.dtoProxy.qualifiedName} ${span.prevSource.dtoVar} = ${span.prevSource.dtosVar}.get(i);
            </#if>
            <#if !span?is_last>
                ${span.nextSource.dtoProxy.qualifiedName} ${span.nextSource.dtoVar} = ${span.nextSource.dtoMapVar}.get(${span.prevSource.dtoVar}.${span.prevField.getter.name}());
                if (${span.nextSource.dtoVar} == null) {
                    continue;
                }
            <#else>
                ${queryModel.target.varsName}.get(i).${source.targetField.name} = ${source.infoMapVar}.get(${span.prevSource.dtoVar}.${span.prevField.getter.name}());
            </#if>
        </#list>
            }
    <#else>
        <#if queryModel.isSourceJoined(source)>
            java.util.Map<${join.localField.type.canonicalText}, java.util.List<${source.dtoProxy.qualifiedName}>> ${source.dtoMapVar} = new java.util.LinkedHashMap<>(${source.dtosVar}.size());
        </#if>
            java.util.Map<${join.localField.type.canonicalText}, java.util.List<${source.infoProxy.qualifiedName}>> ${source.infoMapVar} = new java.util.LinkedHashMap<>(${queryModel.from.dtosVar}.size());
            for (int i = 0; i < ${source.dtosVar}.size(); i++) {
                ${source.dtoProxy.qualifiedName} ${source.dtoVar} = ${source.dtosVar}.get(i);
        <#if queryModel.isSourceJoined(source)>
                ${source.dtoMapVar}.computeIfAbsent(${source.dtoVar}.${join.localField.getter.name}(), k -> new java.util.ArrayList<>()).add(${source.dtoVar});
        </#if>
                ${source.infoProxy.qualifiedName} ${source.infoVar} = ${source.infosVar}.get(i);
                ${source.infoMapVar}.computeIfAbsent(${source.dtoVar}.${join.localField.getter.name}(), k -> new java.util.ArrayList<>()).add(${source.infoVar});
            }
            for (int i = 0; i < ${queryModel.from.dtosVar}.size(); i++) {
        <#list queryModel.computeJoinPath(join) as span>
            <#if span?is_first>
                ${span.prevField.type.canonicalText} ${span.prevFieldVar} = ${span.prevSource.dtosVar}.get(i).${span.prevField.getter.name}();
            </#if>
            <#if queryModel.joinIsSingular(span.prevSource)>
                <#if span?is_last>
                    <#if source.integrationMode.list??>
                        ${queryModel.target.varsName}.get(i).${source.targetField.name} = ${source.infoMapVar}.getOrDefault(${span.prevFieldVar}, java.util.Collections.emptyList());
                    <#elseif source.integrationMode.map??>
                        ${queryModel.target.varsName}.get(i).${source.targetField.name} = ${source.infoMapVar}.getOrDefault(${span.prevFieldVar}, java.util.Collections.emptyList())
                            .stream()
                            .collect(java.util.stream.Collectors.toMap(x->x.${source.integrationMode.map.keyField}, java.util.function.Function::identity()));
                    </#if>
                <#elseif queryModel.joinIsSingular(span.nextSource)>
                    ${span.nextSource.dtoVar} ${span.nextSource.dtoVar} = ${span.nextSource.dtoMapVar}.get(${span.prevFieldVar});
                    if (${span.nextSource.dtoVar} == null) {
                        continue;
                    }
                    ${span.nextField.type.canonicalText} ${span.nextFieldVar} = ${span.nextSource.dtoVar}.${span.nextField.getter.name}();
                <#else>
                    java.util.Set<${span.nextField.type.canonicalText}> ${span.nextFieldsVar} = ${span.nextSource.dtoMapVar}.getOrDefault(${span.prevFieldVar}, java.util.Collections.emptyList())
                        .stream()
                        .map(${span.nextSource.dtoProxy.qualifiedName}::${span.nextField.getter.name})
                        .collect(java.util.stream.Collectors.toSet());
                    if (${span.nextFieldsVar}.isEmpty()) {
                        continue;
                    }
                </#if>
            <#elseif !span?is_last>
                java.util.Set<${span.nextField.type.canonicalText}> ${span.nextFieldsVar} = ${span.prevFieldsVar}.stream()
                    .map(${source.infoMapVar}::get)
                    .filter(java.util.Objects::nonNull)
                    .flatMap(java.util.Collection::stream)
                    .map(x->x.${span.nextField})
                    .collect(java.util.stream.Collectors.toSet());
                if (${span.nextFieldsVar}.isEmpty()) {
                    continue;
                }
            <#else>
                ${queryModel.target.varsName}.get(i).${source.targetField.name} = ${span.prevFieldsVar}.stream()
                    .map(${source.infoMapVar}::get)
                    .filter(java.util.Objects::nonNull)
                    .flatMap(java.util.Collection::stream)
                <#if source.integrationMode.list??>
                    .collect(java.util.stream.Collectors.toList());
                <#elseif source.integrationMode.map??>
                    .collect(java.util.stream.Collectors.toMap(x->x.${source.integrationMode.map.keyField}, java.util.function.Function.identity(), (a, b) -> b));
                </#if>
            </#if>
        </#list>
            }
    </#if>
</#macro>
public java.util.List<${queryModel.target.qualifiedName}> ${method.methodName}(java.util.List<${queryModel.from.dtoProxy.qualifiedName}> ${queryModel.from.dtosVar}) {
    // convert ${queryModel.from.dtoProxy.className} to ${queryModel.target.className}
    if (${queryModel.from.dtosVar}.isEmpty()) {
        return java.util.Collections.emptyList();
    }
    java.util.List<${queryModel.target.qualifiedName}> ${queryModel.target.varsName} = new java.util.ArrayList<>(${queryModel.from.dtosVar}.size());
    for (${queryModel.from.dtoProxy.qualifiedName} ${queryModel.from.dtoVar} : ${queryModel.from.dtosVar}) {
        ${queryModel.target.qualifiedName} ${queryModel.target.varName} = new ${queryModel.target.qualifiedName}();
        ${queryModel.from.copierFieldName}.${queryModel.from.copyToInfoMethodName}(${queryModel.target.varName}, ${queryModel.from.dtoVar});
        ${queryModel.target.varsName}.add(${queryModel.target.varName});
    }
<#list queryModel.joins as join>
    <#if join.source.dtoSource??>
        <@join_from_dto join/>
    <#elseif join.source.infoSource??>
        <@join_from_info join/>
    </#if>
</#list>
    return ${queryModel.target.varsName};
}
