<#-- @ftlvariable name="dtoProxy" type="io.nick.plugin.better.coding.proxy.DtoProxy" -->
<#-- @ftlvariable name="repoProxy" type="io.nick.plugin.better.coding.proxy.RepoProxy" -->
<#-- @ftlvariable name="keyField" type="com.intellij.psi.PsiField" -->
<#-- @ftlvariable name="entityProxy" type="io.nick.plugin.better.coding.proxy.EntityProxy" -->
<#-- @ftlvariable name="persisterProxy" type="io.nick.plugin.better.coding.proxy.PersisterProxy" -->
public ${entityProxy.qualifiedName} ${repoProxy.getEntityOfKeyMethodName(entityProxy, keyField)}(${keyField.type.canonicalText} ${keyField.name}) {
    ${dtoProxy.qualifiedName} dto = ${repoProxy.getPersisterFieldName(persisterProxy)}.${persisterProxy.getFindDTOByKeyMethodName(dtoProxy, keyField)}(${keyField.name});
    if (dto == null) {
        throw new ${repoProxy.serviceRuntimeExceptionClass.qualifiedName}(${repoProxy.apiReturnCodeClass.qualifiedName}.PARAMETER_ERROR, "${entityProxy.className} #${keyField.name} not found");
    }
    return ${repoProxy.getRestoreEntityMethodName(entityProxy)}(dto);
}
