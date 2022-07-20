<#-- @ftlvariable name="persisterProxy" type="io.nick.plugin.better.coding.proxy.PersisterProxy" -->
<#-- @ftlvariable name="dtoProxy" type="io.nick.plugin.better.coding.proxy.DtoProxy" -->
void ${persisterProxy.getUpdateDTOMethodName(dtoProxy)}(${dtoProxy.qualifiedName} ${dtoProxy.varName});
