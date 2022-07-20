<#-- @ftlvariable name="dtoField" type="io.nick.plugin.better.coding.proxy.DtoField" -->
<#-- @ftlvariable name="descriptionClass" type="com.intellij.psi.PsiClass" -->
<#-- @ftlvariable name="fieldPrefix" type="java.lang.String" -->
<#if dtoField.name != 'markedAsDeleted'>
    /**
    <#if dtoField.comment??>
        <#list dtoField.comment?split('\n') as line>
            * ${line}
        </#list>
        *
    </#if>
     * @source ${dtoField.dtoProxy.className}#${dtoField.name}
     */
    @${descriptionClass.qualifiedName}("${(dtoField.comment ! dtoField.name)?replace('\n', '\\n')}")
    public ${dtoField.type.canonicalText ! dtoField.type.presentableText} ${dtoField.getNameWithPrefix(fieldPrefix)};
</#if>
