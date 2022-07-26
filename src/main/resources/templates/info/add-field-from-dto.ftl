<#-- @ftlvariable name="dtoField" type="io.volantis.plugin.better.coding.proxy.DtoField" -->
<#-- @ftlvariable name="fieldPrefix" type="java.lang.String" -->
/**
<#if dtoField.comment??>
    <#list dtoField.comment?split('\n') as line>
        * ${line}
    </#list>
    *
</#if>
* @source ${dtoField.dtoProxy.className}#${dtoField.name}
*/
public ${dtoField.type.canonicalText ! dtoField.type.presentableText} ${dtoField.getNameWithPrefix(fieldPrefix)};
