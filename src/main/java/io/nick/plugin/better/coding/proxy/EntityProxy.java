package io.nick.plugin.better.coding.proxy;

import com.intellij.ide.fileTemplates.JavaTemplateUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiField;
import io.nick.plugin.better.coding.utils.CodingUtils;
import io.nick.plugin.better.coding.utils.EntityHelper;
import io.nick.plugin.better.coding.utils.FieldTemplate;

public class EntityProxy extends PsiClassProxy {
    private EntityProxy(PsiDirectory directory, String className) {
        super(directory, className);
    }

    public EntityProxy(PsiClass psiClass) {
        super(psiClass);
    }

    @Override
    public PsiClass createClassIfNotExist() {
        if (psiClass != null) {
            return psiClass;
        }
        psiClass = CodingUtils.createJavaClass(className, JavaTemplateUtil.INTERNAL_INTERFACE_TEMPLATE_NAME, directory);
        return psiClass;
    }

    public void importFieldFromDTO(DtoField dtoField) {
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
