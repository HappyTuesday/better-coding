<#-- @ftlvariable name="dtoProxy" type="io.nick.plugin.better.coding.proxy.DtoProxy" -->
<#-- @ftlvariable name="repoProxy" type="io.nick.plugin.better.coding.proxy.RepoProxy" -->
<#-- @ftlvariable name="entityProxy" type="io.nick.plugin.better.coding.proxy.EntityProxy" -->
public java.util.List<${entityProxy.qualifiedName}> ${repoProxy.getRestoreEntitiesMethodName(entityProxy)}(java.util.List<${dtoProxy.qualifiedName}> ${dtoProxy.varsName}) {
    java.util.List<${entityProxy.qualifiedName}> ${entityProxy.varsName} = new java.util.ArrayList<>(${dtoProxy.varsName}.size());
    for (${dtoProxy.qualifiedName} ${dtoProxy.varName} : ${dtoProxy.varsName}) {
        ${entityProxy.qualifiedName} ${entityProxy.varName} = new ${entityProxy.qualifiedName}();
        ${repoProxy.initFromDTOMethodName(entityProxy)}(${entityProxy.varName}, ${dtoProxy.varName});
        ${repoProxy.getTrackerFieldName(entityProxy)}.track(${entityProxy.varName});
        ${entityProxy.varsName}.add(${entityProxy.varName});
    }
    return ${entityProxy.varsName};
}
