<#-- @ftlvariable name="copierProxy" type="io.volantis.plugin.better.coding.proxy.CopierProxy" -->
<#-- @ftlvariable name="targetProxy" type="io.volantis.plugin.better.coding.proxy.InfoProxy" -->
<#-- @ftlvariable name="sourceProxy" type="io.volantis.plugin.better.coding.proxy.DtoProxy" -->
public void ${copierProxy.getCopyToInfoMethodName(targetProxy, sourceProxy)}(${targetProxy.qualifiedName} ${targetProxy.varName}, ${sourceProxy.qualifiedName} ${sourceProxy.varName}) {
<#list sourceProxy.dtoFields as sourceField>
    <#assign targetField = targetProxy.findInfoFieldWithSource(sourceField) ! ''>
    <#if targetField == ''><#continue></#if>
    ${targetProxy.varName}.${targetField.name} = ${sourceProxy.varName}.${sourceField.getter.name}();
</#list>
}