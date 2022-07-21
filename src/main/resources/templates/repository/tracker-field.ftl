<#-- @ftlvariable name="dtoProxy" type="io.nick.plugin.better.coding.proxy.DtoProxy" -->
<#-- @ftlvariable name="repoProxy" type="io.nick.plugin.better.coding.proxy.RepoProxy" -->
<#-- @ftlvariable name="entityProxy" type="io.nick.plugin.better.coding.proxy.EntityProxy" -->
<#-- @ftlvariable name="persisterProxy" type="io.nick.plugin.better.coding.proxy.PersisterProxy" -->
private final ${repoProxy.settings.entityTrackerClass}<${entityProxy.qualifiedName}> ${repoProxy.getTrackerFieldName(entityProxy)} = ${repoProxy.settings.entityTrackersClass}.snapshotTracker(
    ${entityProxy.qualifiedName}.class,
    (${entityProxy.varName}, snapshot, changes) -> {
        if (changes.fieldChanged()) {
            ${repoProxy.getPersisterFieldName(persisterProxy)}.${persisterProxy.getUpdateDTOMethodName(dtoProxy)}(${repoProxy.convertToDTOMethodName(entityProxy)}(${entityProxy.varName}));
        }
    });
