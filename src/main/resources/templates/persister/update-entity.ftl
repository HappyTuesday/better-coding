<#-- @ftlvariable name="persisterProxy" type="io.volantis.plugin.better.coding.proxy.PersisterProxy" -->
<#-- @ftlvariable name="dtoProxy" type="io.volantis.plugin.better.coding.proxy.DtoProxy" -->
void ${persisterProxy.getUpdateDTOMethodName(dtoProxy)}(${dtoProxy.qualifiedName} ${dtoProxy.varName});
