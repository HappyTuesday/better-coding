<#-- @ftlvariable name="repoProxy" type="io.volantis.plugin.better.coding.proxy.RepoProxy" -->
<#-- @ftlvariable name="keyField" type="io.volantis.plugin.better.coding.proxy.DtoField" -->
<#-- @ftlvariable name="entityProxy" type="io.volantis.plugin.better.coding.proxy.EntityProxy" -->
<#-- @ftlvariable name="persisterProxy" type="io.volantis.plugin.better.coding.proxy.PersisterProxy" -->
public ${entityProxy.qualifiedName} ${repoProxy.getEntityOfKeyMethodName(entityProxy, keyField)}(${keyField.type.canonicalText} ${keyField.name}) {
    ${keyField.dtoProxy.qualifiedName} dto = ${repoProxy.getPersisterFieldName(persisterProxy)}.${persisterProxy.getFindDTOByKeyMethodName(keyField)}(${keyField.name});
    if (dto == null) {
        ${repoProxy.renderEntityNotFound(entityProxy, keyField)}
    }
    return ${repoProxy.getRestoreEntityMethodName(entityProxy)}(dto);
}
