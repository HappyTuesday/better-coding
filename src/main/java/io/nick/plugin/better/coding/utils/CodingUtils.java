package io.nick.plugin.better.coding.utils;

import com.intellij.ide.IdeView;
import com.intellij.ide.actions.ElementCreator;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.containers.FList;
import com.intellij.util.ui.EDT;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CodingUtils {

    private static @NotNull FList<@NotNull String> slowOperationStack = FList.emptyList();

    public static PsiClass findClassInProjectByName(String simpleName, Project project) {
        PsiShortNamesCache shortNamesCache = PsiShortNamesCache.getInstance(project);
        return allowSlowOperations(() -> {
            PsiClass[] classes = shortNamesCache.getClassesByName(simpleName, GlobalSearchScope.projectScope(project));
            return classes.length > 0 ? classes[0] : null;
        });
    }

    public static PsiClass findClassInProjectByFullName(String fullName, Project project) {
        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        return allowSlowOperations(() -> facade.findClass(fullName, GlobalSearchScope.projectScope(project)));
    }

    public static List<PsiClass> findAllClassInAllScopeByName(String simpleName, Project project) {
        PsiShortNamesCache shortNamesCache = PsiShortNamesCache.getInstance(project);
        return allowSlowOperations(() -> {
            PsiClass[] classes = shortNamesCache.getClassesByName(simpleName, GlobalSearchScope.allScope(project));
            return Arrays.asList(classes);
        });
    }

    public static PsiClass findClassInDirectory(String className, PsiDirectory directory) {
        PsiFile file = directory.findFile(className + ".java");
        if (file instanceof PsiJavaFile) {
            for (PsiClass c : ((PsiJavaFile) file).getClasses()) {
                if (className.equals(c.getName())) {
                    return c;
                }
            }
        }
        return null;
    }

    public static void modifyPsi(Project project, String actionName, Runnable action) {
        modifyPsi(project, actionName, () -> {
            try {
                action.run();
            } catch (Exception e) {
                String traces = Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n"));
                Messages.showMessageDialog(project, e.getMessage() + "\n" + traces, actionName, Messages.getErrorIcon());
            }
            return new PsiElement[0];
        });
    }

    public static void modifyPsi(Project project, String actionName, Supplier<PsiElement[]> action) {
        ElementCreator elementCreator = new ElementCreator(project, actionName) {
            @Override
            protected PsiElement[] create(@NotNull String newName) {
                PsiElement[] elements = action.get();
                return elements == null ? new PsiElement[0] : elements;
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }

            @Override
            protected String getActionName(String newName) {
                return actionName;
            }
        };
        allowSlowOperations(()-> elementCreator.tryCreate(actionName));
    }

    public static void shortenClassReferences(PsiElement element) {
        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(element.getProject());
        styleManager.shortenClassReferences(element);
    }

    public static void addFieldToClass(PsiField field, PsiClass targetClass) {
        if (targetClass.findFieldByName(field.getName(), true) != null) {
            return;
        }
        PsiField lastField = null;
        PsiMethod firstMethod = null;
        for (PsiElement e = targetClass.getFirstChild(); e != null; e = e.getNextSibling()) {
            if (e instanceof PsiField) {
                lastField = (PsiField) e;
            } else if (e instanceof PsiMethod) {
                firstMethod = (PsiMethod) e;
                break;
            }
        }
        PsiField created;
        if (lastField != null) {
            created = (PsiField) targetClass.addAfter(field, lastField);
        } else if (firstMethod != null) {
            created = (PsiField) targetClass.addBefore(field, firstMethod);
        } else {
            created = (PsiField) targetClass.addBefore(field, targetClass.getLastChild());
        }
        shortenClassReferences(created);
    }

    public static boolean canAddClassHere(DataContext dataContext, Set<? extends JpsModuleSourceRootType<?>> sourceRootTypes) {
        final Project project = CommonDataKeys.PROJECT.getData(dataContext);
        final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
        if (project == null || view == null || view.getDirectories().length == 0) {
            return false;
        }
        if (sourceRootTypes == null) {
            return true;
        }
        ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        for (PsiDirectory dir : view.getDirectories()) {
            if (projectFileIndex.isUnderSourceRootOfType(dir.getVirtualFile(), sourceRootTypes) && checkPackageExists(dir)) {
                return true;
            }
            Module module = ModuleUtilCore.findModuleForPsiElement(dir);
            if (module != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkPackageExists(PsiDirectory directory) {
        PsiPackage pkg = JavaDirectoryService.getInstance().getPackage(directory);
        if (pkg == null) {
            return false;
        }

        String name = pkg.getQualifiedName();
        return StringUtil.isEmpty(name) || PsiNameHelper.getInstance(directory.getProject()).isQualifiedName(name);
    }

    public static PsiClass retrieveContainingClass(AnActionEvent e) {
        PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
        if (element != null) {
            for (PsiElement p = element; p != null; p = p.getParent()) {
                if (p instanceof PsiClass) {
                    return (PsiClass) p;
                }
            }
        }
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (file instanceof PsiJavaFile) {
            PsiClass[] classes = ((PsiJavaFile) file).getClasses();
            if (classes.length != 0) {
                return classes[0];
            }
        }
        return null;
    }

    public static PsiDirectory getOrCreateSubdirectory(PsiDirectory directory, String subdirectoryName) {
        PsiDirectory subdirectory = directory.findSubdirectory(subdirectoryName);
        return Objects.requireNonNullElseGet(subdirectory, () -> directory.createSubdirectory(subdirectoryName));
    }

    public static <T, E extends Throwable> T allowSlowOperations(@NotNull ThrowableComputable<T, E> computable) throws E {
        try (AccessToken ignore = allowSlowOperations("generic")) {
            return computable.compute();
        }
    }

    public static @NotNull AccessToken allowSlowOperations(@NotNull @NonNls String activityName) {
        if (!EDT.isCurrentThreadEdt()) {
            return AccessToken.EMPTY_ACCESS_TOKEN;
        }

        FList<String> prev = slowOperationStack;
        slowOperationStack = prev.prepend(activityName);
        return new StackedAccessToken(prev);
    }

    private static class StackedAccessToken extends AccessToken {
        private final FList<String> prev;

        public StackedAccessToken(FList<String> prev) {
            this.prev = prev;
        }

        @Override
        public void finish() {
            slowOperationStack = prev;
        }
    }

    public static PsiClass createJavaClass(PsiDirectory directory, String name, String content) {
        PsiJavaFile file = createJavaFile(directory, name, content);
        PsiClass[] classes = file.getClasses();
        if (classes.length == 0) {
            throw new IllegalArgumentException("no class found");
        }
        return classes[0];
    }

    private static PsiJavaFile createJavaFile(PsiDirectory directory, String className, String content) {
        String fileName = className + JavaFileType.INSTANCE.getDefaultExtension();
        PsiFileFactory factory = PsiFileFactory.getInstance(directory.getProject());
        PsiFile psiFile = factory.createFileFromText(fileName, JavaLanguage.INSTANCE, content, false, false);
        return (PsiJavaFile) psiFile;
    }
}
