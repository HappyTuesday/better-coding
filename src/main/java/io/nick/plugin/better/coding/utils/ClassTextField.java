package io.nick.plugin.better.coding.utils;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.ui.EditorTextField;
import com.intellij.util.ui.JBDimension;
import org.jetbrains.annotations.NotNull;

public class ClassTextField extends EditorTextField {
    private final PsiElement context;
    private PsiTypeCodeFragment codeFragment;

    public final EventListeners<PsiClass> classChanged = new EventListeners<>();

    public ClassTextField(PsiElement context) {
        super(context.getProject(), JavaFileType.INSTANCE);
        setPreferredSize(new JBDimension(getPreferredSize().width, 30));
        this.context = context;
        setEnteredClass(null);
    }

    public void setEnteredClass(PsiClass psiClass) {
        Project project = context.getProject();
        JavaCodeFragmentFactory factory = JavaCodeFragmentFactory.getInstance(project);
        if (psiClass != null && psiClass.getName() != null) {
            codeFragment = factory.createTypeCodeFragment(psiClass.getName(), context, true);
            codeFragment.importClass(psiClass);
        } else {
            codeFragment = factory.createTypeCodeFragment("", context, true);
        }

        Document document = PsiDocumentManager.getInstance(project).getDocument(codeFragment);
        if (document == null) {
            throw new IllegalStateException();
        }
        setDocument(document);
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                PsiType type;
                try {
                    type = codeFragment.getType();
                } catch (PsiTypeCodeFragment.TypeSyntaxException | PsiTypeCodeFragment.NoTypeException e) {
                    markAsInvalid(e.getMessage());
                    return;
                }
                if (type instanceof PsiClassType) {
                    PsiClass psiClass = ((PsiClassType) type).resolve();
                    if (psiClass != null) {
                        clearInvalidMark();
                        classChanged.onChange(psiClass);
                    } else {
                        markAsInvalid("could not resolve " + type);
                    }
                } else {
                    markAsInvalid("unsupported type " + type);
                }
            }
        });
        if (psiClass != null) {
            classChanged.onChange(psiClass);
        }
    }

    public PsiClass getEnteredClass() {
        try {
            PsiType type = codeFragment.getType();
            if (type instanceof PsiClassType) {
                return ((PsiClassType) type).resolve();
            }
        } catch (PsiTypeCodeFragment.TypeSyntaxException | PsiTypeCodeFragment.NoTypeException ignored) {
        }
        return null;
    }

    private void markAsInvalid(String tooltip) {
        setToolTipText(tooltip);
    }

    private void clearInvalidMark() {
        setToolTipText("");
    }
}
