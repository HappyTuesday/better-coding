<#-- @ftlvariable name="dtoField" type="io.volantis.plugin.better.coding.proxy.DtoField" -->
<#if dtoField.comment??>
    /**
    <#list dtoField.comment?split('\n') as line>
        * ${line}
    </#list>
    */
</#if>
${dtoField.type.canonicalText} ${dtoField.name};