<#-- @ftlvariable name="entityProxy" type="io.volantis.plugin.better.coding.proxy.EntityProxy" -->
<#-- @ftlvariable name="repoProxy" type="io.volantis.plugin.better.coding.proxy.RepoProxy" -->
<#-- @ftlvariable name="dtoProxy" type="io.volantis.plugin.better.coding.proxy.DtoProxy" -->
private ${dtoProxy.qualifiedName} ${repoProxy.convertToDTOMethodName(entityProxy)}(${entityProxy.qualifiedName} ${entityProxy.varName}) {
    ${dtoProxy.qualifiedName} ${dtoProxy.varName} = new ${dtoProxy.qualifiedName}();
    <#list dtoProxy.dtoFields as dtoField>
        <#if dtoField.logicalDeleteField>
            ${dtoProxy.varName}.${dtoField.setter.name}(false);
            <#continue>
        </#if>
        <#assign entityField = entityProxy.findFieldForDTO(dtoField) ! ''>
        <#if entityField == ''>
            <#continue>
        </#if>
        <#if entityProxy.isEnumType(entityField.type)>
            if (${entityProxy.varName}.${entityField.name} != null) {
                ${dtoProxy.varName}.${dtoField.setter.name}(${entityProxy.varName}.${entityField.name}.code);
            }
        <#else>
            ${dtoProxy.varName}.${dtoField.setter.name}(${entityProxy.varName}.${entityField.name});
        </#if>
    </#list>
    return ${dtoProxy.varName};
}