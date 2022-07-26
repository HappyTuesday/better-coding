<#-- @ftlvariable name="repoProxy" type="io.volantis.plugin.better.coding.proxy.RepoProxy" -->
<#-- @ftlvariable name="keyField" type="io.volantis.plugin.better.coding.proxy.DtoField" -->
<#-- @ftlvariable name="entityProxy" type="io.volantis.plugin.better.coding.proxy.EntityProxy" -->
<#-- @ftlvariable name="persisterProxy" type="io.volantis.plugin.better.coding.proxy.PersisterProxy" -->
<#assign keysVar = repoProxy.pluralize(keyField.name)>
public java.util.List<${entityProxy.qualifiedName}> ${repoProxy.getEntityOfKeysMethodName(entityProxy, keyField)}(java.util.List<${keyField.type.canonicalText}> ${keysVar}) {
    if (${keysVar}.isEmpty()) {
        return java.util.Collections.emptyList();
    }
    java.util.List<${keyField.dtoProxy.qualifiedName}> dtos = ${repoProxy.getPersisterFieldName(persisterProxy)}.${persisterProxy.getFindDTOByKeysMethodName(keyField)}(${keysVar});
    return ${repoProxy.getRestoreEntitiesMethodName(entityProxy)}(dtos);
}
