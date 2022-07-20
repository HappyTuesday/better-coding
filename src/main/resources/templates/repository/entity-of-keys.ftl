<#-- @ftlvariable name="dtoProxy" type="io.nick.plugin.better.coding.proxy.DtoProxy" -->
<#-- @ftlvariable name="repoProxy" type="io.nick.plugin.better.coding.proxy.RepoProxy" -->
<#-- @ftlvariable name="keyField" type="com.intellij.psi.PsiField" -->
<#-- @ftlvariable name="entityProxy" type="io.nick.plugin.better.coding.proxy.EntityProxy" -->
<#-- @ftlvariable name="persisterProxy" type="io.nick.plugin.better.coding.proxy.PersisterProxy" -->
<#assign keysVar = repoProxy.pluralize(keyField.name)>
public java.util.List<${entityProxy.qualifiedName}> ${repoProxy.getEntityOfKeysMethodName(entityProxy, keyField)}(java.util.List<${keyField.type.canonicalText}> ${keysVar}) {
    if (${keysVar}.isEmpty()) {
        return java.util.Collections.emptyList();
    }
    java.util.List<${dtoProxy.qualifiedName}> dtos = ${repoProxy.getPersisterFieldName(persisterProxy)}.${persisterProxy.getFindDTOByKeysMethodName(dtoProxy, keyField)}(${keysVar});
    return ${repoProxy.getRestoreEntitiesMethodName(entityProxy)}(dtos);
}
