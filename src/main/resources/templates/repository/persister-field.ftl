<#-- @ftlvariable name="repoProxy" type="io.volantis.plugin.better.coding.proxy.RepoProxy" -->
<#-- @ftlvariable name="persisterProxy" type="io.volantis.plugin.better.coding.proxy.PersisterProxy" -->
@javax.annotation.Resource
private ${persisterProxy.qualifiedName} ${repoProxy.getPersisterFieldName(persisterProxy)};
