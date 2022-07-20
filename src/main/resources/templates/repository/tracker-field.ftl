<#-- @ftlvariable name="dtoProxy" type="io.nick.plugin.better.coding.proxy.DtoProxy" -->
<#-- @ftlvariable name="repoProxy" type="io.nick.plugin.better.coding.proxy.RepoProxy" -->
<#-- @ftlvariable name="trackerClass" type="com.intellij.psi.PsiClass" -->
<#-- @ftlvariable name="trackersClass" type="com.intellij.psi.PsiClass" -->
<#-- @ftlvariable name="entityProxy" type="io.nick.plugin.better.coding.proxy.EntityProxy" -->
<#-- @ftlvariable name="persisterProxy" type="io.nick.plugin.better.coding.proxy.PersisterProxy" -->
private final ${trackerClass.qualifiedName}<${entityProxy.qualifiedName}> ${repoProxy.getTrackerFieldName(entityProxy)} = ${trackersClass.qualifiedName}.snapshotTracker(
    ${entityProxy.qualifiedName}.class,
    (${entityProxy.varName}, snapshot, changes) -> {
        if (changes.fieldChanged()) {
            ${repoProxy.getPersisterFieldName(persisterProxy)}.${persisterProxy.getUpdateDTOMethodName(dtoProxy)}(${repoProxy.convertToDTOMethodName(entityProxy)}(${entityProxy.varName}));
        }
    });
