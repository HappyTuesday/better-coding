package io.nick.plugin.better.coding.settings;

import com.intellij.ide.fileTemplates.impl.FileTemplateHighlighter;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.util.LayerDescriptor;
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileTypes.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.JBUI;
import io.nick.plugin.better.coding.utils.CodingUtils;
import io.nick.plugin.better.coding.utils.ComponentSizeUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BetterCodingSettingsPanel extends JBPanel<BetterCodingSettingsPanel> implements AutoCloseable {
    private static final FileType freemarkerFileType = FileTypeManager.getInstance().getFileTypeByExtension("ftl");
    private final EditorTextField logicalDeleteFieldsEditor;
    private final Editor infoFieldTemplateEditor;
    private final Editor infoClassTemplateEditor;
    private final TextFieldWithAutoCompletion<String> entityTrackerClassSelector;
    private final TextFieldWithAutoCompletion<String> entityTrackersClassSelector;
    private final Editor entityNotFoundTemplateEditor;

    private boolean dirty;

    public BetterCodingSettingsPanel(Project project) {
        super(new BorderLayout());

        ContentPanel panel = new ContentPanel();
        panel.addSeparator("Common");
        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                dirty = true;
            }

            @Override
            public void bulkUpdateFinished(@NotNull Document document) {
                dirty = true;
            }
        };

        logicalDeleteFieldsEditor = new EditorTextField();
        logicalDeleteFieldsEditor.getDocument().addDocumentListener(documentListener);
        panel.addLabeledComponent("Logical delete fields", logicalDeleteFieldsEditor);

        panel.addSeparator("Info class");
        infoFieldTemplateEditor = createEditor(project);
        ComponentSizeUtil.setPreferredHeight(infoFieldTemplateEditor.getComponent(), 200);
        infoFieldTemplateEditor.getDocument().addDocumentListener(documentListener);
        panel.addLabeledComponent("Info field template", infoFieldTemplateEditor.getComponent());

        infoClassTemplateEditor = createEditor(project);
        ComponentSizeUtil.setPreferredHeight(infoClassTemplateEditor.getComponent(), 200);
        infoClassTemplateEditor.getDocument().addDocumentListener(documentListener);
        panel.addLabeledComponent("Info class template", infoClassTemplateEditor.getComponent());

        panel.addSeparator("Entity class");

        entityTrackerClassSelector = classSelectorByName("EntityTracker", project);
        ComponentSizeUtil.setPreferredHeight(entityTrackerClassSelector, 28);
        entityTrackerClassSelector.getDocument().addDocumentListener(documentListener);
        panel.addLabeledComponent("Entity tracker class", entityTrackerClassSelector);

        entityTrackersClassSelector = classSelectorByName("EntityTrackers", project);
        ComponentSizeUtil.setPreferredHeight(entityTrackersClassSelector, 28);
        entityTrackersClassSelector.getDocument().addDocumentListener(documentListener);
        panel.addLabeledComponent("Entity trackers class", entityTrackersClassSelector);

        entityNotFoundTemplateEditor = createEditor(project);
        ComponentSizeUtil.setPreferredHeight(entityNotFoundTemplateEditor.getComponent(), 120);
        entityNotFoundTemplateEditor.getDocument().addDocumentListener(documentListener);
        panel.addLabeledComponent("Entity not found template", entityNotFoundTemplateEditor.getComponent());

        add(panel, BorderLayout.NORTH);
    }

    public Set<String> getLogicalDeleteFields() {
        String[] a = logicalDeleteFieldsEditor.getText().split("[,;\\s]+");
        return Arrays.stream(a).filter(Predicate.not(String::isEmpty)).collect(Collectors.toSet());
    }

    public void setLogicalDeleteFields(Set<String> logicalDeleteFields) {
        if (logicalDeleteFields != null) {
            logicalDeleteFieldsEditor.setText(String.join(" ", logicalDeleteFields));
        } else {
            logicalDeleteFieldsEditor.setText("");
        }
    }

    public String getInfoFieldTemplate() {
        return infoFieldTemplateEditor.getDocument().getText();
    }

    public void setInfoFieldTemplate(String infoFieldTemplate) {
        infoFieldTemplateEditor.getDocument().setText(infoFieldTemplate);
    }

    public String getInfoClassTemplate() {
        return infoClassTemplateEditor.getDocument().getText();
    }

    public void setInfoClassTemplate(String infoClassTemplate) {
        infoClassTemplateEditor.getDocument().setText(infoClassTemplate);
    }

    public String getEntityTrackerClass() {
        return entityTrackerClassSelector.getText();
    }

    public void setEntityTrackerClass(String entityTrackerClass) {
        entityTrackerClassSelector.setText(entityTrackerClass);
    }

    public String getEntityTrackersClass() {
        return entityTrackersClassSelector.getText();
    }

    public void setEntityTrackersClass(String entityTrackersClass) {
        entityTrackersClassSelector.setText(entityTrackersClass);
    }

    public String getEntityNotFoundTemplate() {
        return entityNotFoundTemplateEditor.getDocument().getText();
    }

    public void setEntityNotFoundTemplate(String entityNotFoundTemplate) {
        entityNotFoundTemplateEditor.getDocument().setText(entityNotFoundTemplate);
    }

    public boolean isDirty() {
        return dirty;
    }

    public void reset(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public void close() {
        EditorFactory.getInstance().releaseEditor(infoFieldTemplateEditor);
        EditorFactory.getInstance().releaseEditor(infoClassTemplateEditor);
        EditorFactory.getInstance().releaseEditor(entityNotFoundTemplateEditor);
    }

    private static TextFieldWithAutoCompletion<String> classSelectorByName(String simpleName, Project project) {
        List<String> alternatives = CodingUtils.findAllClassInAllScopeByName(simpleName, project)
            .stream()
            .map(PsiClass::getQualifiedName)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        String text = alternatives.isEmpty() ? "" : alternatives.get(0);
        TextFieldWithAutoCompletion.StringsCompletionProvider provider = new TextFieldWithAutoCompletion.StringsCompletionProvider(alternatives, PlatformIcons.CLASS_ICON);
        return new TextFieldWithAutoCompletion<>(project, provider, true, text);
    }

    private static class ContentPanel extends JBPanel<ContentPanel> {
        private int rowIndex;

        public ContentPanel() {
            super(new GridBagLayout());
        }

        public void addLabeledComponent(String labelText, Component component) {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.NORTHWEST;

            JBLabel label = new JBLabel(labelText);
            constraints.gridy = rowIndex++;
            constraints.insets = JBUI.insets(5, 20, 5, 0);
            add(label, constraints);

            constraints.gridy = rowIndex++;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.insets = JBUI.insets(0, 20, 5, 0);
            add(component, constraints);
        }

        public void addSeparator(String title) {
            GridBagConstraints constraints = new GridBagConstraints(
                0, rowIndex++,
                GridBagConstraints.REMAINDER, 1,
                1, 1,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, JBUI.emptyInsets(),
                0, 0
            );
            TitledSeparator separator = new TitledSeparator(title);
            add(separator, constraints);
        }
    }

    private static Editor createEditor(Project project) {
        EditorFactory editorFactory = EditorFactory.getInstance();
        Document doc = EditorFactory.getInstance().createDocument("");
        FileType fileType = freemarkerFileType == FileTypes.UNKNOWN ? FileTypes.PLAIN_TEXT : freemarkerFileType;
        Editor editor = editorFactory.createEditor(doc, project, fileType, false);

        EditorSettings editorSettings = editor.getSettings();
        editorSettings.setVirtualSpace(false);
        editorSettings.setLineMarkerAreaShown(false);
        editorSettings.setIndentGuidesShown(false);
        editorSettings.setLineNumbersShown(false);
        editorSettings.setFoldingOutlineShown(false);
        editorSettings.setAdditionalColumnsCount(3);
        editorSettings.setAdditionalLinesCount(1);
        editorSettings.setCaretRowShown(false);

        if (freemarkerFileType == FileTypes.UNKNOWN) {
            ((EditorEx)editor).setHighlighter(createTemplateHighlighter(project));
        }

        return editor;
    }

    private static EditorHighlighter createTemplateHighlighter(Project project) {
        if (freemarkerFileType != FileTypes.UNKNOWN) {
            return EditorHighlighterFactory.getInstance().createEditorHighlighter(project, new LightVirtualFile("aaa." + ".ftl"));
        }

        FileType fileType = FileTypes.PLAIN_TEXT;

        SyntaxHighlighter originalHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(fileType, null, null);
        if (originalHighlighter == null) {
            originalHighlighter = new PlainSyntaxHighlighter();
        }

        final EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        LayeredLexerEditorHighlighter highlighter = new LayeredLexerEditorHighlighter(new FileTemplateHighlighter(), scheme);
        highlighter.registerLayer(new IElementType("TEXT", Language.ANY), new LayerDescriptor(originalHighlighter, ""));
        return highlighter;
    }
}
