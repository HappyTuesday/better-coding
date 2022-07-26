<#-- @ftlvariable name="entityProxy" type="io.volantis.plugin.better.coding.proxy.EntityProxy" -->
<#-- @ftlvariable name="repoProxy" type="io.volantis.plugin.better.coding.proxy.RepoProxy" -->
<#-- @ftlvariable name="dtoProxy" type="io.volantis.plugin.better.coding.proxy.DtoProxy" -->
private void ${repoProxy.initFromDTOMethodName(entityProxy)}(${entityProxy.qualifiedName} ${entityProxy.varName}, ${dtoProxy.qualifiedName} ${dtoProxy.varName}) {
    <#list dtoProxy.dtoFields as dtoField>
        <#assign entityField = entityProxy.findFieldForDTO(dtoField) ! ''>
        <#if entityField == ''>
            <#continue>
        </#if>
        <#if entityProxy.isEnumType(entityField.type)>
            if (${dtoProxy.varName}.${dtoField.getter.name}() != null) {
                ${entityProxy.varName}.${entityField.name} = ${entityField.type.canonicalText}.parse(${dtoProxy.varName}.${dtoField.getter.name}());
            }
        <#else>
            ${entityProxy.varName}.${entityField.name} = ${dtoProxy.varName}.${dtoField.getter.name}();
        </#if>
    </#list>
}