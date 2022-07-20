package io.nick.plugin.better.coding.proxy;

import com.intellij.ide.fileTemplates.JavaTemplateUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import io.nick.plugin.better.coding.app.AppSchema;
import io.nick.plugin.better.coding.app.QueryModel;
import io.nick.plugin.better.coding.app.converter.BatchConvertToInfoMethod;
import io.nick.plugin.better.coding.app.converter.ConvertToInfoMethod;
import io.nick.plugin.better.coding.utils.CodingUtils;

public class ConverterProxy extends PsiClassProxy {
    public ConverterProxy(PsiDirectory directory, String className) {
        super(directory, className);
    }

    public ConverterProxy(PsiClass psiClass) {
        super(psiClass);
    }

    @Override
    public PsiClass createClassIfNotExist() {
        if (psiClass != null) {
            return psiClass;
        }
        psiClass = CodingUtils.createJavaClass(className, JavaTemplateUtil.INTERNAL_CLASS_TEMPLATE_NAME, directory);
        afterClassCreated();
        return psiClass;
    }

    public void afterClassCreated() {
        PsiElementFactory factory = getFactory();
        String serviceFullName = "org.springframework.stereotype.Service";
        if (!psiClass.hasAnnotation(serviceFullName)) {
            PsiAnnotation serviceAnnotation = factory.createAnnotationFromText("@" + serviceFullName, psiClass);
            psiClass.addBefore(serviceAnnotation, psiClass.getModifierList());
        }
        CodingUtils.shortenClassReferences(psiClass);
    }

    public void addFetcherField(DtoProxy dtoProxy, FetcherProxy fetcherProxy) {
        createClassIfNotExist();
        addInjectedField(getFetcherFieldName(dtoProxy), fetcherProxy.createType());
    }

    public void addCopierField(DtoProxy dtoProxy, CopierProxy copierProxy) {
        createClassIfNotExist();
        addInjectedField(getCopierFieldName(dtoProxy), copierProxy.createType());
    }

    public void addConverterField(DtoProxy dtoProxy, ConverterProxy converterProxy) {
        createClassIfNotExist();
        addInjectedField(getConverterFieldName(dtoProxy), converterProxy.createType());
    }

    public void addBatchConvertToInfoMethod(QueryModel queryModel, AppSchema appSchema) {
        createClassIfNotExist();
        new BatchConvertToInfoMethod(this, queryModel).generateAndAdd(appSchema);
    }

    public boolean convertToInfoMethodExists(InfoProxy target, DtoProxy from) {
        if (psiClass == null) {
            return false;
        }
        for (PsiMethod method : psiClass.findMethodsByName(getConvertToInfoMethodName(target, from), true)) {
            PsiParameterList params = method.getParameterList();
            if (params.getParametersCount() == 0) continue;
            PsiParameter param = params.getParameter(0);
            if (param == null) continue;
            if (param.getType().getCanonicalText().equals(from.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }

    public boolean batchConvertToInfoMethodExists(InfoProxy target, DtoProxy from) {
        if (psiClass == null) {
            return false;
        }
        for (PsiMethod method : psiClass.findMethodsByName(getBatchConvertToInfoMethodName(target, from), true)) {
            PsiParameterList params = method.getParameterList();
            if (params.getParametersCount() == 0) continue;
            PsiParameter param = params.getParameter(0);
            if (param == null) continue;
            if (param.getType().getCanonicalText().equals("java.util.List<" + from.getQualifiedName() + ">")) {
                return true;
            }
        }
        return false;
    }

    public void addConvertToInfoMethod(QueryModel queryModel, AppSchema appSchema) {
        createClassIfNotExist();
        new ConvertToInfoMethod(this, queryModel).generateAndAdd(appSchema);
    }

    public static String getFetcherFieldName(DtoProxy dtoProxy) {
        return StringUtil.decapitalize(StringUtil.trimEnd(dtoProxy.getClassName(), "DTO")) + "Fetcher";
    }

    public static String getCopierFieldName(DtoProxy sourceProxy) {
        return StringUtil.decapitalize(StringUtil.trimEnd(sourceProxy.getClassName(), "DTO")) + "Copier";
    }

    public static String getConverterFieldName(DtoProxy sourceProxy) {
        String converterClassName = StringUtil.trimEnd(sourceProxy.getClassName(), "DTO") + "Converter";
        return StringUtil.decapitalize(converterClassName);
    }

    public static String getCopyToInfoMethodName(DtoProxy sourceProxy) {
        return "copyTo";
    }

    public static String getConvertToInfoMethodName(InfoProxy target, DtoProxy source) {
        return "convertTo" + target.getClassName();
    }

    public static String getBatchConvertToInfoMethodName(InfoProxy target, DtoProxy source) {
        return "batchConvertTo" + target.getClassName();
    }

    public static ConverterProxy forDTO(DtoProxy dtoProxy, PsiDirectory defaultFolder) {
        String converterClassName = StringUtil.trimEnd(dtoProxy.className, "DTO") + "Converter";
        PsiClass converterClass = CodingUtils.findClassInProjectByName(converterClassName, dtoProxy.getProject());
        if (converterClass != null) {
            return new ConverterProxy(converterClass);
        } else {
            return new ConverterProxy(defaultFolder, converterClassName);
        }
    }
}
