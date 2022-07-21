package io.nick.plugin.better.coding.proxy;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import io.nick.plugin.better.coding.utils.CodingUtils;

public class MapperProxy extends PsiClassProxy {
    public MapperProxy(PsiClass psiClass) {
        super(psiClass);
    }

    @Override
    protected PsiClass doCreateClass() {
        throw new UnsupportedOperationException();
    }

    public String getQualifiedExampleName() {
        return getQualifiedName().replace("Mapper", "Example");
    }

    public String getMapperFieldName() {
        return StringUtil.decapitalize(getClassName());
    }

    public static MapperProxy forDto(DtoProxy dtoProxy) {
        PsiClass mapperClass = CodingUtils.findClassInProjectByName(dtoProxy.className + "Mapper", dtoProxy.getProject());
        if (mapperClass == null) {
            return null;
        }
        return new MapperProxy(mapperClass);
    }
}
