<#-- @ftlvariable name="dtoProxy" type="io.volantis.plugin.better.coding.proxy.DtoProxy" -->
<#-- @ftlvariable name="repoProxy" type="io.volantis.plugin.better.coding.proxy.RepoProxy" -->
<#-- @ftlvariable name="entityProxy" type="io.volantis.plugin.better.coding.proxy.EntityProxy" -->
public ${entityProxy.qualifiedName} ${repoProxy.getRestoreEntityMethodName(entityProxy)}(${dtoProxy.qualifiedName} ${dtoProxy.varName}) {
    ${entityProxy.qualifiedName} ${entityProxy.varName} = new ${entityProxy.qualifiedName}();
    ${repoProxy.initFromDTOMethodName(entityProxy)}(${entityProxy.varName}, ${dtoProxy.varName});
    ${repoProxy.getTrackerFieldName(entityProxy)}.track(${entityProxy.varName});
    return ${entityProxy.varName};
}
