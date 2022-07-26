package io.volantis.plugin.better.coding.proxy;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiField;
import io.volantis.plugin.better.coding.utils.CodingUtils;
import io.volantis.plugin.better.coding.utils.EntityHelper;
import io.volantis.plugin.better.coding.utils.FieldTemplate;

public class EntityProxy extends PsiClassProxy {
    public EntityProxy(PsiDirectory directory, String className) {
        super(directory, className);
    }

    public EntityProxy(PsiClass psiClass) {
        super(psiClass);
    }

    @Override
    protected PsiClass doCreateClass() {
        return classTemplate().pass("entityProxy", this).create("entity/entity-class.ftl");
    }

    public void importFieldFromDTO(DtoField dtoField) {
        if (dtoField.isLogicalDeleteField()) {
            return;
        }
        new FieldTemplate(createClassIfNotExist())
            .pass("dtoField", dtoField)
            .generateAndAdd("entity/add-field-from-dto.ftl");
    }

    public PsiClass relatedDTOClass() {
        return CodingUtils.findClassInProjectByName(className + "DTO", getProject());
    }

    public PsiField findFieldForDTO(DtoField dtoField) {
        return EntityHelper.findEntityFieldForDTO(psiClass, dtoField);
    }
}
