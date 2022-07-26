<#-- @ftlvariable name="propertyName" type="java.lang.String" -->
<#-- @ftlvariable name="leftExpr" type="com.intellij.psi.PsiExpression" -->
<#-- @ftlvariable name="leftPropertyType" type="com.intellij.psi.PsiType" -->
<#-- @ftlvariable name="leftField" type="com.intellij.psi.PsiField" -->
<#-- @ftlvariable name="leftSetter" type="com.intellij.psi.PsiMethod" -->
<#-- @ftlvariable name="rightExpr" type="com.intellij.psi.PsiExpression" -->
<#-- @ftlvariable name="rightPropertyType" type="com.intellij.psi.PsiType" -->
<#-- @ftlvariable name="rightField" type="com.intellij.psi.PsiField" -->
<#-- @ftlvariable name="rightGetter" type="com.intellij.psi.PsiMethod" -->
<#-- @ftlvariable name="typeHelper" type="io.volantis.plugin.better.coding.utils.PsiTypeHelper" -->
<#assign right>
    ${rightExpr.text}.<#if rightField??>${rightField.name}<#else>${rightGetter.name}()</#if><#t>
</#assign>
<#assign assignStatement>
    <#if leftField??>
        ${leftExpr.text}.${leftField.name} = ${right};<#t>
    <#else>
        ${leftExpr.text}.${leftSetter.name}(${right});<#t>
    </#if>
</#assign>
<#if typeHelper.isPrimitive(rightPropertyType)>
    ${assignStatement}
<#else>
    if (${right} != null) {
        ${assignStatement}
    }
</#if>