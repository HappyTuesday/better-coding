package io.nick.plugin.better.coding.utils;

import com.intellij.ide.IdeView;
import com.intellij.ide.actions.CreateFileAction;
import com.intellij.ide.actions.ElementCreator;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.GeneralModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ex.JavaSdkUtil;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.ui.configuration.SdkLookupUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SlowOperations;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CodingUtils {

    protected static final Logger LOG = Logger.getInstance(CodingUtils.class);

    public static PsiClass findClassInProjectByName(String simpleName, Project project) {
        return findClassByName(simpleName, null, project, GlobalSearchScope.projectScope(project));
    }

    public static PsiClass findClassInProjectByName(String simpleName, String packageName, Project project) {
        return findClassInProjectByFullName(StringUtil.getQualifiedName(packageName, simpleName), project);
    }

    public static PsiClass findClassInProjectByFullName(String fullName, Project project) {
        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        return SlowOperations.allowSlowOperations(() -> facade.findClass(fullName, GlobalSearchScope.projectScope(project)));
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

    public static PsiClass findClassInSamePackageByName(String className, PsiClass anotherClass) {
        PsiDirectory directory = anotherClass.getContainingFile().getContainingDirectory();
        PsiClass psiClass = findClassInDirectory(className, directory);
        if (psiClass != null) {
            return psiClass;
        }
        String packageName = anotherClass.getQualifiedName();
        if (packageName == null) {
            return null;
        }
        String fullName = StringUtil.getQualifiedName(packageName, className);
        return findClassInProjectByFullName(fullName, anotherClass.getProject());
    }

    public static PsiClass findClassInAllScopeByName(String simpleName, String subpackage, Project project) {
        return findClassByName(simpleName, subpackage, project, GlobalSearchScope.allScope(project));
    }

    public static PsiClass findClassByName(String simpleName, String subpackage, Project project, GlobalSearchScope searchScope) {
        PsiShortNamesCache shortNamesCache = PsiShortNamesCache.getInstance(project);
        return SlowOperations.allowSlowOperations(() -> {
            PsiClass[] classes = shortNamesCache.getClassesByName(simpleName, searchScope);
            for (PsiClass clazz : classes) {
                if (subpackage == null) {
                    return clazz;
                }
                String fullName = clazz.getQualifiedName();
                if (fullName != null && fullName.endsWith(subpackage + "." + simpleName)) {
                    return clazz;
                }
            }
            return null;
        });
    }

    public static PsiClass createJavaClass(String className, String templateName, PsiDirectory directory) throws IncorrectOperationException {
        if (className.contains(".")) {
            String[] names = className.split("\\.");
            for (int i = 0; i < names.length - 1; i++) {
                directory = CreateFileAction.findOrCreateSubdirectory(directory, names[i]);
            }
            className = names[names.length - 1];
        }

        DumbService service = DumbService.getInstance(directory.getProject());
        PsiDirectory finalDir = directory;
        String finalClassName = className;
        return service.computeWithAlternativeResolveEnabled(() -> JavaDirectoryService.getInstance()
            .createClass(finalDir, finalClassName, templateName, false)
        );
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
        SlowOperations.allowSlowOperations(()-> elementCreator.tryCreate(actionName));
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

    public static PsiElement createLineBreak(Project project) {
        return PsiParserFacade.SERVICE.getInstance(project).createWhiteSpaceFromText("\n\n");
    }

    public static void configureSDK(PsiDirectory dir) {
        Module module = ModuleUtilCore.findModuleForPsiElement(dir);
        if (module != null && ModuleRootManager.getInstance(module).getSdk() == null) {
            Project project = dir.getProject();
            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Looking for JDK", true) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    SdkLookupUtil.findAndSetupSdk(project, indicator, JavaSdk.getInstance(), sdk -> {
                        JavaSdkUtil.applyJdkToProject(project, sdk);
                        ModuleRootModificationUtil.setModuleSdk(module, sdk);
                        return null;
                    });
                }
            });
        }
    }

    public static PsiDirectory adjustDirectory(PsiDirectory directory, Set<? extends JpsModuleSourceRootType<?>> mySourceRootTypes) {
        ProjectFileIndex index = ProjectRootManager.getInstance(directory.getProject()).getFileIndex();
        if (mySourceRootTypes != null && !index.isUnderSourceRootOfType(directory.getVirtualFile(), mySourceRootTypes)) {
            Module module = ModuleUtilCore.findModuleForPsiElement(directory);
            if (module == null) return null;
            ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
            ContentEntry contentEntry =
                ContainerUtil.find(modifiableModel.getContentEntries(), entry -> entry.getFile() != null && VfsUtilCore.isAncestor(entry.getFile(), directory.getVirtualFile(), false));
            if (contentEntry == null) return null;
            try {
                VirtualFile src = WriteAction.compute(() -> VfsUtil.createDirectoryIfMissing(contentEntry.getFile(), "src"));
                contentEntry.addSourceFolder(src, false);
                WriteAction.run(modifiableModel::commit);
                return PsiManager.getInstance(module.getProject()).findDirectory(src);
            }
            catch (IOException e) {
                LOG.error(e);
                return null;
            }
        }
        return directory;
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
            if (module != null && ModuleType.is(module, GeneralModuleType.INSTANCE)) {
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
}
