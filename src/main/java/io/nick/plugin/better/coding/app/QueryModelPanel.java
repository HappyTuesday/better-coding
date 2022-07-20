package io.nick.plugin.better.coding.app;

import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.*;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.JBUI;
import io.nick.plugin.better.coding.proxy.*;
import io.nick.plugin.better.coding.utils.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("DialogTitleCapitalization")
public class QueryModelPanel extends JBPanel<QueryModelPanel> {
    private final AppSchema appSchema;
    private final List<DtoProxy> availableDtoProxies;
    private final ContentPanel contentPanel;
    private final Map<QueryKey, Boolean> queryAlreadyDefinedCache = new HashMap<>();

    public QueryModelPanel(PsiDirectory appDirectory) {
        super(new BorderLayout());
        appSchema = new AppSchema(appDirectory);
        availableDtoProxies = DtoProxy.listAllDTOsInProject(appDirectory.getProject());
        setBackground(JBUI.CurrentTheme.NewClassDialog.panelBackground());
        contentPanel = new ContentPanel();
        JBScrollPane scrollPane = new JBScrollPane(contentPanel);
        scrollPane.setBorder(JBUI.Borders.empty());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new JBDimension(900, 500));
        scrollPane.setBorder(JBUI.Borders.customLine(Color.getColor("0x323232"), 1));
        add(scrollPane, BorderLayout.CENTER);
        refresh();
    }

    private boolean queryAlreadyDefined(QueryKey queryKey) {
        Boolean existsInCache = queryAlreadyDefinedCache.get(queryKey);
        if (existsInCache != null) {
            return existsInCache;
        }
        ConverterProxy converterProxy = appSchema.getConverterProxy(queryKey.fromDTO);
        boolean exists = converterProxy.convertToInfoMethodExists(queryKey.targetInfo, queryKey.fromDTO) &&
            converterProxy.batchConvertToInfoMethodExists(queryKey.targetInfo, queryKey.fromDTO);
        queryAlreadyDefinedCache.put(queryKey, exists);
        return exists;
    }

    private void refresh() {
        contentPanel.extraQueryListBox.refresh();
        validate();
        repaint();
    }

    public List<QueryModel> buildModels() {
        QueryModel mainModel = contentPanel.mainQueryBox.buildModel();
        List<QueryModel> extraModels = contentPanel.extraQueryListBox.buildModels();
        List<QueryModel> result = new ArrayList<>(1 + extraModels.size());
        result.add(mainModel);
        result.addAll(extraModels);
        return result;
    }

    private class ContentPanel extends JBPanel<ContentPanel> {
        private final MainQueryBox mainQueryBox;
        private final ExtraQueryListBox extraQueryListBox;
        public ContentPanel() {
            super(new VerticalLayout(10));
            setBorder(JBUI.Borders.empty(5));
            mainQueryBox = new MainQueryBox();
            add(mainQueryBox);
            extraQueryListBox = new ExtraQueryListBox(mainQueryBox);
            add(extraQueryListBox);
            mainQueryBox.joinListPanel.refreshed.listen(j -> refresh());
            mainQueryBox.joinListPanel.sourceChanged.listen(s -> refresh());
        }
    }

    private class MainQueryBox extends JBPanel<MainQueryBox> {
        private final TargetInfoBox targetBox;
        private final FromSourceBox fromSourceBox;
        private final JoinListPanel joinListPanel;

        public MainQueryBox() {
            super(new VerticalLayout(10));
            targetBox = new TargetInfoBox(appSchema.directory);
            fromSourceBox = new FromSourceBox(appSchema.directory, availableDtoProxies);
            QueryHeaderPanel header = new QueryHeaderPanel(targetBox.infoProxyField, fromSourceBox);
            add(header);
            joinListPanel = new JoinListPanel(fromSourceBox);
            add(joinListPanel);

            targetBox.targetChanged.listen(p -> {
                suggestSourceDTO(p, fromSourceBox);
                refreshJoinBoxes();
            });
            fromSourceBox.sourceChanged.listen(p -> refreshJoinBoxes());

            if (targetBox.getInfoProxy() != null) {
                suggestSourceDTO(targetBox.getInfoProxy(), fromSourceBox);
            }
            refreshJoinBoxes();
        }

        public QueryKey getQueryKey() {
            InfoProxy targetInfo = targetBox.getInfoProxy();
            DtoProxy fromDTO = fromSourceBox.getDtoProxy();
            if (targetInfo == null || fromDTO == null) {
                return null;
            }
            return new QueryKey(targetInfo, fromDTO);
        }

        public QueryModel buildModel() {
            InfoProxy targetInfo = targetBox.getInfoProxy();
            DtoProxy fromDTO = fromSourceBox.getDtoProxy();
            if (targetInfo == null || fromDTO == null) {
                return null;
            }
            QueryModel.DtoSource fromSource = fromSourceBox.buildSource();
            List<QueryModel.Join> joins = joinListPanel.computeJoinModels(fromSourceBox, fromSource);
            return new QueryModel(targetInfo, fromSource, joins);
        }

        public Set<QueryKey> getDependingQueryKeys() {
            return joinListPanel.getDependingQueryKeys();
        }

        private void refreshJoinBoxes() {
            joinListPanel.refresh(appSchema.directory, targetBox.getInfoProxy(), availableDtoProxies);
            validate();
            repaint();
        }
    }

    private static void suggestSourceDTO(InfoProxy targetInfo, MutableSourceBox sourceBox) {
        DtoProxy suggested = getSuggestedSourceDTO(targetInfo, sourceBox.choices);
        if (suggested != null) {
            sourceBox.select(suggested);
        }
    }

    private static DtoProxy getSuggestedSourceDTO(InfoProxy targetInfo, List<DtoProxy> choices) {
        for (InfoField infoField : targetInfo.getInfoFields()) {
            InfoField.Source source = infoField.getSource();
            if (source == null) continue;
            for (DtoProxy dtoProxy : choices) {
                if (dtoProxy.getClassName().equals(source.dtoClassName)) {
                    return dtoProxy;
                }
            }
        }
        String infoSimpleName = StringUtil.trimEnd(targetInfo.getClassName(), "Info");
        for (DtoProxy dtoProxy : choices) {
            String dtoSimpleName = StringUtil.trimEnd(StringUtil.trimEnd(dtoProxy.getClassName(), "DTO"), "Ext");
            if (infoSimpleName.contains(dtoSimpleName) || dtoSimpleName.contains(infoSimpleName)) {
                return dtoProxy;
            }
        }
        return null;
    }

    private static class QueryHeaderPanel extends JBPanel<QueryHeaderPanel> {
        public QueryHeaderPanel(Component targetInfoComp, SourceBox fromSourceBox) {
            super(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = JBUI.insetsRight(10);

            JBLabel selectLabel = new JBLabel("SELECT");
            FontStyleUtil.bold(selectLabel);
            selectLabel.setLabelFor(targetInfoComp);

            constraints.gridx = 0;
            add(selectLabel, constraints);

            ComponentSizeUtil.setPreferredWidth(targetInfoComp, 240);
            constraints.gridx = 1;
            add(targetInfoComp, constraints);

            JBLabel fromLabel = new JBLabel("FROM");
            constraints.gridx = 2;
            add(fromLabel, constraints);

            ComponentSizeUtil.setPreferredWidth(fromSourceBox.getDtoProxyComponent(), 240);
            constraints.gridx = 3;
            add(fromSourceBox.getDtoProxyComponent(), constraints);

            JBLabel asLabel = new JBLabel("AS");
            asLabel.setLabelFor(fromSourceBox.aliasBox.getComponent());
            constraints.gridx = 4;
            add(asLabel, constraints);

            constraints.gridx = 5;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = JBUI.emptyInsets();
            add(fromSourceBox.aliasBox.getComponent(), constraints);
        }
    }

    private class ExtraQueryListBox extends JBPanel<ExtraQueryListBox> {
        private final MainQueryBox mainQueryBox;
        private final Map<QueryKey, ExtraQueryBox> queryBoxMap;

        public ExtraQueryListBox(MainQueryBox mainQueryBox) {
            super(new VerticalLayout(10));
            this.mainQueryBox = mainQueryBox;
            this.queryBoxMap = new LinkedHashMap<>();
        }

        public List<QueryModel> buildModels() {
            return queryBoxMap.values().stream()
                .map(ExtraQueryBox::buildModel)
                .collect(Collectors.toList());
        }

        public void refresh() {
            QueryKey mainQueryKey = mainQueryBox.getQueryKey();
            Map<QueryKey, ExtraQueryBox> newQueryBoxMap = new LinkedHashMap<>();
            for (QueryKey queryKey : mainQueryBox.getDependingQueryKeys()) {
                recursiveCopy(appSchema.directory, queryKey, mainQueryKey, newQueryBoxMap);
            }
            queryBoxMap.entrySet().removeIf(entry -> {
                if (newQueryBoxMap.containsKey(entry.getKey())) {
                    return false;
                }
                remove(entry.getValue());
                return true;
            });
            for (Map.Entry<QueryKey, ExtraQueryBox> entry : newQueryBoxMap.entrySet()) {
                if (queryBoxMap.containsKey(entry.getKey())) {
                    continue;
                }
                queryBoxMap.put(entry.getKey(), entry.getValue());
                add(entry.getValue());
            }
            validate();
            repaint();
        }

        private void recursiveCopy(PsiDirectory directory, QueryKey queryKey, QueryKey mainQueryKey, Map<QueryKey, ExtraQueryBox> newQueryBoxMap) {
            if (queryKey.equals(mainQueryKey) || newQueryBoxMap.containsKey(queryKey) || queryAlreadyDefined(queryKey)) {
                return;
            }
            ExtraQueryBox queryBox = queryBoxMap.get(queryKey);
            ExtraQueryBox newQueryBox;
            if (queryBox == null) {
                newQueryBox = new ExtraQueryBox(directory, queryKey.targetInfo, queryKey.fromDTO, availableDtoProxies);
                newQueryBox.joinListPanel.refreshed.listen(j -> refresh());
            } else {
                newQueryBox = queryBox;
            }
            newQueryBoxMap.put(queryKey, newQueryBox);
            for (QueryKey nestedQueryKey : newQueryBox.joinListPanel.getDependingQueryKeys()) {
                recursiveCopy(directory, nestedQueryKey, mainQueryKey, newQueryBoxMap);
            }
        }
    }

    private static class ExtraQueryBox extends JBPanel<ExtraQueryBox> {
        private final InfoProxy targetInfo;
        private final DtoSourceBox fromSourceBox;
        private final JoinListPanel joinListPanel;

        public ExtraQueryBox(PsiDirectory directory, InfoProxy targetInfo, DtoProxy fromDTO, List<DtoProxy> dtoChoices) {
            super(new VerticalLayout(10));
            this.targetInfo = targetInfo;
            Border border = JBUI.Borders.customLineTop(Color.decode("0x515151"));
            setBorder(border);

            JBLabel targetInfoLabel = new JBLabel(targetInfo.getClassName());
            targetInfoLabel.setToolTipText(targetInfo.getQualifiedName());
            fromSourceBox = new DtoSourceBox(fromDTO);
            QueryHeaderPanel header = new QueryHeaderPanel(targetInfoLabel, fromSourceBox);
            add(header);
            joinListPanel = new JoinListPanel(fromSourceBox);
            add(joinListPanel);

            joinListPanel.refresh(directory, targetInfo, dtoChoices);
        }

        public QueryModel buildModel() {
            QueryModel.DtoSource fromSource = fromSourceBox.buildSource();
            List<QueryModel.Join> joins = joinListPanel.computeJoinModels(fromSourceBox, fromSource);
            return new QueryModel(targetInfo, fromSource, joins);
        }
    }

    private static class QueryKey {
        public final InfoProxy targetInfo;
        public final DtoProxy fromDTO;

        public QueryKey(InfoProxy targetInfo, DtoProxy fromDTO) {
            this.targetInfo = targetInfo;
            this.fromDTO = fromDTO;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            QueryKey queryKey = (QueryKey) o;
            return targetInfo.equals(queryKey.targetInfo) && fromDTO.equals(queryKey.fromDTO);
        }

        @Override
        public int hashCode() {
            return Objects.hash(targetInfo, fromDTO);
        }
    }

    private static class JoinListPanel extends JBPanel<JoinListPanel> {
        private final SourceBox fromSourceBox;
        private final AliasChecker aliasChecker;
        private final List<JoinBox> joinBoxes;
        public final EventListeners<SourceBox> sourceChanged = new EventListeners<>();
        public final EventListeners<JoinBox> joinChanged = new EventListeners<>();
        public final EventListeners<JoinListPanel> refreshed = new EventListeners<>();

        public JoinListPanel(SourceBox fromSourceBox) {
            super(new GridBagLayout());
            this.fromSourceBox = fromSourceBox;
            aliasChecker = new AliasChecker();
            aliasChecker.add(fromSourceBox.aliasBox);
            fromSourceBox.aliasBox.changed.listen(e -> handleSourceAliasChanged(fromSourceBox));
            joinBoxes = new ArrayList<>();
        }

        public void refresh(PsiDirectory directory, InfoProxy targetInfo, List<DtoProxy> dtoChoices) {
            DtoProxy fromDTO = fromSourceBox.getDtoProxy();
            if (targetInfo == null || fromDTO == null) {
                return;
            }

            for (JoinBox joinBox : joinBoxes) {
                aliasChecker.remove(joinBox.sourceBox.aliasBox);
            }
            joinBoxes.clear();
            List<SourceBox> sourceBoxes = computeSourceBoxes(directory, targetInfo, fromDTO, dtoChoices);
            List<SourceBox> availableSources = new ArrayList<>();
            availableSources.add(fromSourceBox);

            for (SourceBox sourceBox : sourceBoxes) {
                JoinBox joinBox = new JoinBox(sourceBox, new ArrayList<>(availableSources));
                joinBoxes.add(joinBox);
                aliasChecker.add(sourceBox.aliasBox);
                sourceBox.aliasBox.changed.listen(e -> handleSourceAliasChanged(joinBox.sourceBox));

                availableSources.add(sourceBox);
                if (joinBox.sourceBox instanceof InfoSourceBox) {
                    ((InfoSourceBox) joinBox.sourceBox).sourceChanged.listen(v -> {
                        sourceChanged.onChange(joinBox.sourceBox);
                        joinChanged.onChange(joinBox);
                    });
                }

                joinBox.localFieldChanged.listen(v -> joinChanged.onChange(joinBox));
                joinBox.referredSourceChanged.listen(v -> joinChanged.onChange(joinBox));
                joinBox.referredFieldChanged.listen(v -> joinChanged.onChange(joinBox));
            }

            render(targetInfo);
            refreshed.onChange(this);
            validate();
            repaint();
        }

        private void render(InfoProxy targetInfo) {
            removeAll();

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.WEST;
            constraints.gridy = 0;
            constraints.insets = JBUI.insetsRight(10);
            constraints.fill = GridBagConstraints.HORIZONTAL;
            for (JoinBox joinBox : joinBoxes) {
                renderJoinBox(joinBox, targetInfo, constraints);
                constraints.gridx = 0;
                constraints.gridy++;
            }
        }

        private void renderJoinBox(JoinBox joinBox, InfoProxy targetInfo, GridBagConstraints constraints) {
            SourceBox sourceBox = joinBox.sourceBox;
            if (sourceBox instanceof InfoSourceBox) {
                renderInfoSourceBox((InfoSourceBox) sourceBox, constraints);
            } else {
                renderDtoSourceBox((DtoSourceBox) sourceBox, targetInfo, constraints);
            }
            constraints.gridy++;
            renderJoinCondition(joinBox, constraints);
        }

        private void renderInfoSourceBox(InfoSourceBox sourceBox, GridBagConstraints constraints) {
            JBLabel assignLabel = new JBLabel("ASSIGN");
            FontStyleUtil.bold(assignLabel);
            ComponentSizeUtil.setPreferredWidth(assignLabel, 60);
            constraints.gridx = 0;
            add(assignLabel, constraints);

            JBLabel fieldLabel = new JBLabel("." + sourceBox.targetField.getName());
            fieldLabel.setToolTipText(sourceBox.targetField.getType().getPresentableText() + " " + sourceBox.targetField.getName());
            ComponentSizeUtil.setPreferredWidth(fieldLabel, 80);
            constraints.gridx = 1;
            add(fieldLabel, constraints);

            JBLabel fromLabel = new JBLabel("FROM");
            constraints.gridx = 2;
            add(fromLabel, constraints);

            constraints.gridx = 3;
            ComponentSizeUtil.setPreferredWidth(sourceBox.getDtoProxyComponent(), 240);
            add(sourceBox.getDtoProxyComponent(), constraints);

            if (!(sourceBox instanceof MapInfoSourceBox)) {
                constraints.gridwidth = 3;
            }
            renderAlias(sourceBox, constraints, !(sourceBox instanceof MapInfoSourceBox));
            constraints.gridwidth = 1;

            if (sourceBox instanceof MapInfoSourceBox) {
                MapInfoSourceBox mapInfoSourceBox = (MapInfoSourceBox) sourceBox;
                JBLabel keyedBy = new JBLabel("keyed by");
                constraints.gridx = 6;
                add(keyedBy, constraints);

                constraints.gridx = 7;
                constraints.insets = JBUI.emptyInsets();
                add(mapInfoSourceBox.keyFieldComboBox, constraints);
                keyedBy.setLabelFor(mapInfoSourceBox.keyFieldComboBox);
                constraints.insets = JBUI.insetsRight(10);
            }
        }

        private void renderDtoSourceBox(DtoSourceBox sourceBox, InfoProxy targetInfo, GridBagConstraints constraints) {
            JBLabel copyLabel = new JBLabel("COPY");
            FontStyleUtil.bold(copyLabel);
            ComponentSizeUtil.setPreferredWidth(copyLabel, 60);
            constraints.gridx = 0;
            add(copyLabel, constraints);

            List<InfoField> infoFields = targetInfo.getInfoFieldsFromDTO(sourceBox.dtoProxy);
            JBLabel fieldsLabel = new JBLabel(infoFields.size() + (infoFields.size() == 1 ? " field" : " fields"));
            ComponentSizeUtil.setPreferredWidth(fieldsLabel, 80);
            fieldsLabel.setToolTipText(infoFields.stream().map(InfoField::getName).collect(Collectors.joining(",")));
            constraints.gridx = 1;
            add(fieldsLabel, constraints);

            JBLabel fromLabel = new JBLabel("FROM");
            constraints.gridx = 2;
            add(fromLabel, constraints);

            JBLabel dtoLabel = new JBLabel(sourceBox.dtoProxy.getClassName());
            dtoLabel.setToolTipText(sourceBox.dtoProxy.getQualifiedName());
            ComponentSizeUtil.setPreferredWidth(dtoLabel, 240);
            constraints.gridx = 3;
            add(dtoLabel, constraints);

            constraints.gridwidth = 3;
            renderAlias(sourceBox, constraints, true);
            constraints.gridwidth = 1;
        }

        private void renderAlias(SourceBox sourceBox, GridBagConstraints constraints, boolean last) {
            JBLabel asLabel = new JBLabel("AS");
            constraints.gridx = 4;
            add(asLabel, constraints);

            constraints.gridx = 5;
            constraints.weightx = 1;
            if (last) {
                constraints.insets = JBUI.emptyInsets();
            }
            add(sourceBox.aliasBox.getComponent(), constraints);
            asLabel.setLabelFor(sourceBox.aliasBox.getComponent());
            constraints.weightx = 0;
            if (last) {
                constraints.insets = JBUI.insetsRight(10);
            }
        }

        private void renderJoinCondition(JoinBox joinBox, GridBagConstraints constraints) {
            JBLabel onLabel = new JBLabel("ON");
            constraints.gridx = 2;
            add(onLabel, constraints);
            constraints.gridx = 3;
            ComponentSizeUtil.setPreferredWidth(joinBox.localFieldComboBox, 240);
            add(joinBox.localFieldComboBox, constraints);
            onLabel.setLabelFor(joinBox.localFieldComboBox);

            JBLabel opLabel = new JBLabel("=");
            constraints.gridx = 4;
            add(opLabel, constraints);

            ComponentSizeUtil.setPreferredWidth(joinBox.referredSourceComboBox, 180);
            constraints.gridx = 5;
            constraints.insets = JBUI.emptyInsets();
            add(joinBox.referredSourceComboBox, constraints);

            JBLabel dotLabel = new JBLabel(".");
            constraints.gridx = 6;
            add(dotLabel, constraints);

            ComponentSizeUtil.setPreferredWidth(joinBox.referredFieldComboBox, 100);
            constraints.gridx = 7;
            add(joinBox.referredFieldComboBox, constraints);
            constraints.insets = JBUI.insetsRight(10);
        }

        public List<QueryModel.Join> computeJoinModels(SourceBox fromSourceBox, QueryModel.DtoSource fromSource) {
            Map<SourceBox, QueryModel.Source> sourceMap = new LinkedHashMap<>();
            sourceMap.put(fromSourceBox, fromSource);
            List<QueryModel.Join> joins = new ArrayList<>();
            for (JoinBox joinBox : joinBoxes ) {
                sourceMap.put(joinBox.sourceBox, joinBox.sourceBox.buildSource());
                QueryModel.Join join = joinBox.buildJoin(sourceMap);
                joins.add(join);
            }
            return joins;
        }

        public Set<QueryKey> getDependingQueryKeys() {
            Set<QueryKey> queryKeys = new LinkedHashSet<>();
            for (JoinBox joinBox : joinBoxes) {
                SourceBox sourceBox = joinBox.sourceBox;
                DtoProxy dtoProxy = sourceBox.getDtoProxy();
                if (dtoProxy == null || !(sourceBox instanceof InfoSourceBox)) continue;
                InfoProxy targetInfo = ((InfoSourceBox) sourceBox).infoProxy;
                QueryKey queryKey = new QueryKey(targetInfo, dtoProxy);
                queryKeys.add(queryKey);
            }
            return queryKeys;
        }

        private void handleSourceAliasChanged(SourceBox targetBox) {
            for (JoinBox joinBox : joinBoxes) {
                if (joinBox.getReferredSourceBox() == targetBox) {
                    joinBox.updateSourceAlias();
                }
            }
            aliasChecker.update(targetBox.aliasBox);
        }

        private List<SourceBox> computeSourceBoxes(PsiDirectory directory, InfoProxy targetInfo, DtoProxy fromDTO, List<DtoProxy> dtoChoices) {
            List<SourceBox> sourceBoxes = new ArrayList<>();
            for (InfoField infoField : targetInfo.getInfoFields()) {
                PsiType fieldType = infoField.getType();
                DtoField dtoField = infoField.getSourceField();
                if (dtoField != null) {
                    DtoProxy dtoProxy = dtoField.dtoProxy;
                    if (dtoProxy.equals(fromDTO)) {
                        continue;
                    }
                    boolean alreadyMet = sourceBoxes.stream().anyMatch(j -> Objects.equals(j.getDtoProxy(), dtoProxy));
                    if (alreadyMet) {
                        continue;
                    }
                    sourceBoxes.add(new DtoSourceBox(dtoProxy));
                } else if (InfoProxy.isInfoClassType(fieldType)) {
                    SourceBox sourceBox = new SingularInfoSourceBox(directory, dtoChoices, InfoProxy.forType(fieldType), infoField);
                    sourceBoxes.add(sourceBox);
                } else if (TypeUtils.isListType(fieldType)) {
                    PsiType eleType = ((PsiClassType) fieldType).getParameters()[0];
                    if (InfoProxy.isInfoClassType(eleType)) {
                        SourceBox sourceBox = new ListInfoSourceBox(directory, dtoChoices, InfoProxy.forType(eleType), infoField);
                        sourceBoxes.add(sourceBox);
                    }
                } else if (TypeUtils.isMapType(fieldType)) {
                    PsiType[] typeArgs = ((PsiClassType) fieldType).getParameters();
                    if (InfoProxy.isInfoClassType(typeArgs[1])) {
                        SourceBox sourceBox = new MapInfoSourceBox(directory, dtoChoices, InfoProxy.forType(typeArgs[1]), infoField, typeArgs[0]);
                        sourceBoxes.add(sourceBox);
                    }
                }
            }
            return sourceBoxes;
        }
    }

    private static class TargetInfoBox {
        public final ClassTextField infoProxyField;
        public final EventListeners<InfoProxy> targetChanged = new EventListeners<>();

        public TargetInfoBox(PsiDirectory directory) {
            infoProxyField = new ClassTextField(directory);
            infoProxyField.setPlaceholder("Info class");
            infoProxyField.classChanged.listen(c -> infoProxyChanged(new InfoProxy(c)));
        }

        private void infoProxyChanged(InfoProxy newInfoProxy) {
            targetChanged.onChange(newInfoProxy);
        }

        public InfoProxy getInfoProxy() {
            PsiClass psiClass = infoProxyField.getEnteredClass();
            return psiClass != null ? new InfoProxy(psiClass) : null;
        }
    }

    private static abstract class SourceBox {
        private static final Pattern ALIAS_POSTFIX_PATTERN = Pattern.compile("\\d*$");
        protected final AliasBox aliasBox;

        public SourceBox() {
           aliasBox = new AliasBox();
        }

        public abstract Component getDtoProxyComponent();

        public abstract DtoProxy getDtoProxy();

        public abstract QueryModel.Source buildSource();

        public String getAlias() {
            return aliasBox.getText();
        }

        protected void setAlias(String alias) {
            aliasBox.setText(alias);
        }

        protected void setDefaultAliasFor(DtoProxy dtoProxy) {
            setAlias(StringUtil.trimEnd(dtoProxy.getVarName(), "DTO"));
        }

        public void adjustAlias(List<SourceBox> previousSourceBoxes) {
            String myAlias = getAlias();
            if (myAlias == null || myAlias.isEmpty() || isAliasAvailable(myAlias, previousSourceBoxes)) {
                return;
            }
            String prefix = ALIAS_POSTFIX_PATTERN.matcher(myAlias).replaceFirst("");
            String postfix = myAlias.substring(prefix.length());
            int no = postfix.isEmpty() ? 0 : Integer.parseInt(postfix);
            do {
                no++;
                myAlias = prefix + no;
            } while (!isAliasAvailable(myAlias, previousSourceBoxes));
            setAlias(myAlias);
        }

        private static boolean isAliasAvailable(String newAlias, List<SourceBox> previousSourceBoxes) {
            for (SourceBox sourceBox : previousSourceBoxes) {
                if (newAlias.equals(sourceBox.getAlias())) {
                    return false;
                }
            }
            return true;
        }
    }

    private static class AliasBox {
        private final Color originColor;
        private final EditorTextField textField;
        public final EventListeners<DocumentEvent> changed = new EventListeners<>();

        public AliasBox() {
            textField = new EditorTextField();
            textField.setPlaceholder("Alias");
            originColor = textField.getBackground();
            textField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void documentChanged(@NotNull DocumentEvent event) {
                    changed.onChange(event);
                }
            });
        }

        public Component getComponent() {
            return textField;
        }

        public String getText() {
            return textField.getText();
        }

        public void setText(String newText) {
            textField.setText(newText);
        }

        public void markAsInvalid() {
            textField.setBackground(JBColor.RED);
        }

        public void clearInvalidMark() {
            textField.setBackground(originColor);
        }
    }

    private static class AliasChecker {
        private final Map<CharSequence, Set<AliasBox>> boxMap = new HashMap<>();

        public void add(AliasBox box) {
            String text = box.getText();
            if (text.isEmpty()) {
                return;
            }
            Set<AliasBox> now = boxMap.computeIfAbsent(text, k -> new HashSet<>());
            now.add(box);
            if (now.size() > 1) {
                now.forEach(AliasBox::markAsInvalid);
            }
        }

        public void remove(AliasBox box) {
            for (Set<AliasBox> list : boxMap.values()) {
                if (list.remove(box)) {
                    if (list.size() == 1) {
                        list.forEach(AliasBox::clearInvalidMark);
                    }
                    break;
                }
            }
            box.clearInvalidMark();
        }

        public void update(AliasBox box) {
            remove(box);
            add(box);
        }
    }

    private abstract static class MutableSourceBox extends SourceBox {
        public final List<DtoProxy> choices;
        protected final ClassTextField dtoProxyTextField;
        public final EventListeners<DtoProxy> sourceChanged = new EventListeners<>();

        public MutableSourceBox(PsiDirectory directory, List<DtoProxy> choices) {
            this.choices = choices;
            dtoProxyTextField = new ClassTextField(directory);
            dtoProxyTextField.classChanged.listen(c -> dtoProxyChanged(new DtoProxy(c)));
        }

        protected void dtoProxyChanged(DtoProxy newDtoProxy) {
            setDefaultAliasFor(newDtoProxy);
            sourceChanged.onChange(newDtoProxy);
        }

        @Override
        public Component getDtoProxyComponent() {
            return dtoProxyTextField;
        }

        @Override
        public DtoProxy getDtoProxy() {
            PsiClass psiClass = dtoProxyTextField.getEnteredClass();
            return psiClass != null ? new DtoProxy(psiClass) : null;
        }

        public void select(DtoProxy dtoProxy) {
            dtoProxyTextField.setEnteredClass(dtoProxy.getPsiClass());
        }
    }

    private static class FromSourceBox extends MutableSourceBox {
        public FromSourceBox(PsiDirectory directory, List<DtoProxy> choices) {
            super(directory, choices);
            dtoProxyTextField.setPlaceholder("DTO class");
        }

        @Override
        public QueryModel.DtoSource buildSource() {
            return new QueryModel.DtoSource(getDtoProxy(), getAlias(), null);
        }
    }

    private static class DtoSourceBox extends SourceBox {
        private final DtoProxy dtoProxy;
        private final JBLabel dtoProxyLabel;
        public DtoSourceBox(DtoProxy dtoProxy) {
            this.dtoProxy = dtoProxy;
            dtoProxyLabel = new JBLabel(dtoProxy.getClassName());
            dtoProxyLabel.setToolTipText(dtoProxy.getQualifiedName());
            setDefaultAliasFor(dtoProxy);
        }

        @Override
        public Component getDtoProxyComponent() {
            return dtoProxyLabel;
        }

        @Override
        public DtoProxy getDtoProxy() {
            return dtoProxy;
        }

        @Override
        public QueryModel.DtoSource buildSource() {
            return new QueryModel.DtoSource(dtoProxy, getAlias(), null);
        }
    }

    private abstract static class InfoSourceBox extends MutableSourceBox {
        protected final InfoProxy infoProxy;
        protected final InfoField targetField;
        public InfoSourceBox(PsiDirectory directory, List<DtoProxy> fromChoices, InfoProxy infoProxy, InfoField targetField) {
            super(directory, fromChoices);
            dtoProxyTextField.setPlaceholder("DTO class");
            this.targetField = targetField;
            this.infoProxy = infoProxy;
            suggestSourceDTO(infoProxy, this);
        }

        @Override
        public QueryModel.Source buildSource() {
            return new QueryModel.InfoSource(getDtoProxy(), getAlias(), null, targetField, infoProxy, getIntegrationMode());
        }

        protected abstract QueryModel.IntegrationMode getIntegrationMode();
    }

    public static class SingularInfoSourceBox extends InfoSourceBox {
        public SingularInfoSourceBox(PsiDirectory directory, List<DtoProxy> fromChoices, InfoProxy infoProxy, InfoField targetField) {
            super(directory, fromChoices, infoProxy, targetField);
        }

        @Override
        protected QueryModel.IntegrationMode getIntegrationMode() {
            return new QueryModel.IntegratedAsSingular();
        }
    }

    public static class ListInfoSourceBox extends InfoSourceBox {
        public ListInfoSourceBox(PsiDirectory directory, List<DtoProxy> fromChoices, InfoProxy infoProxy, InfoField targetField) {
            super(directory, fromChoices, infoProxy, targetField);
        }

        @Override
        protected QueryModel.IntegrationMode getIntegrationMode() {
            return new QueryModel.IntegratedAsList();
        }
    }

    public static class MapInfoSourceBox extends InfoSourceBox {
        private final PsiType keyType;
        private final MyComboBox<DtoField> keyFieldComboBox;

        public MapInfoSourceBox(PsiDirectory directory, List<DtoProxy> fromChoices, InfoProxy infoProxy, InfoField targetField, PsiType keyType) {
            super(directory, fromChoices, infoProxy, targetField);
            this.keyType = keyType;
            keyFieldComboBox = new MyComboBox<>(new DtoFieldListCellRenderer());
        }

        @Override
        protected void dtoProxyChanged(DtoProxy newDtoProxy) {
            super.dtoProxyChanged(newDtoProxy);
            List<DtoField> dtoFields = newDtoProxy.getDtoFields()
                .stream()
                .filter(f -> Objects.equals(f.getType(), keyType))
                .collect(Collectors.toList());
            keyFieldComboBox.setElements(dtoFields);
            keyFieldComboBox.validate();
        }

        @Override
        protected QueryModel.IntegrationMode getIntegrationMode() {
            DtoField keyField = keyFieldComboBox.getElement();
            if (keyField == null) {
                throw new ValidationException("key field is not chosen");
            }
            return new QueryModel.IntegratedAsMap(keyField.getName());
        }
    }

    private static class JoinBox {
        public final SourceBox sourceBox;
        private final MyComboBox<DtoField> localFieldComboBox;
        private final MyComboBox<SourceBox> referredSourceComboBox;
        private final MyComboBox<DtoField> referredFieldComboBox;

        public final EventListeners<DtoField> localFieldChanged = new EventListeners<>();
        public final EventListeners<SourceBox> referredSourceChanged = new EventListeners<>();
        public final EventListeners<DtoField> referredFieldChanged = new EventListeners<>();

        public JoinBox(SourceBox sourceBox, List<SourceBox> availableSourceBoxes) {
            this.sourceBox = sourceBox;
            sourceBox.adjustAlias(availableSourceBoxes);

            localFieldComboBox = new MyComboBox<>(new DtoFieldListCellRenderer());
            referredSourceComboBox = new MyComboBox<>(new SourceBoxListCellRenderer(), availableSourceBoxes);
            referredFieldComboBox = new MyComboBox<>(new DtoFieldListCellRenderer());
            if (sourceBox instanceof MutableSourceBox) {
                ((MutableSourceBox) sourceBox).sourceChanged.listen(this::sourceDtoProxyChanged);
            }
            if (sourceBox.getDtoProxy() != null) {
                sourceDtoProxyChanged(sourceBox.getDtoProxy());
            }
            localFieldComboBox.addSelectedListener(localFieldChanged::onChange);
            referredSourceComboBox.addSelectedListener(this::referredSourceChanged);
            referredFieldComboBox.addSelectedListener(referredFieldChanged::onChange);
            refreshSuitableLocalFields();
            refreshAvailableSourceFields();
        }

        private void sourceDtoProxyChanged(DtoProxy newDtoProxy) {
            refreshSuitableLocalFields();
        }

        private void refreshSuitableLocalFields() {
            DtoProxy dtoProxy = sourceBox.getDtoProxy();
            if (dtoProxy == null) {
                localFieldComboBox.setModel(new DefaultComboBoxModel<>());
            } else {
                List<DtoField> dtoFields = dtoProxy.getDtoFields()
                    .stream()
                    .filter(this::localFieldCanUse)
                    .collect(Collectors.toList());
                localFieldComboBox.setElements(dtoFields);
                localFieldChanged.onChange(localFieldComboBox.getElement());
                suggestReferredField(localFieldComboBox.getElement());
            }
            localFieldComboBox.validate();
        }

        private boolean localFieldCanUse(DtoField localField) {
            return !localField.isLogicalDeleteField();
        }

        private boolean fieldCanRefer(DtoField remoteField) {
            return !remoteField.isLogicalDeleteField();
        }

        private void referredSourceChanged(SourceBox referredSource) {
            referredSourceChanged.onChange(referredSource);
            refreshAvailableSourceFields();
        }

        private void refreshAvailableSourceFields() {
            SourceBox referredSource = referredSourceComboBox.getElement();
            if (referredSource == null) {
                referredFieldComboBox.setElements(Collections.emptyList());
            } else {
                List<DtoField> dtoFields = getAvailableSourceFields(referredSource.getDtoProxy());
                referredFieldComboBox.setElements(dtoFields);
                referredFieldChanged.onChange(referredFieldComboBox.getElement());
                suggestReferredField(localFieldComboBox.getElement());
            }
            referredFieldComboBox.validate();
        }

        public void updateSourceAlias() {
            referredSourceComboBox.revalidate();
            referredSourceComboBox.repaint();
        }

        public SourceBox getReferredSourceBox() {
            return referredSourceComboBox.getElement();
        }

        private List<DtoField> getAvailableSourceFields(DtoProxy source) {
            return source
                .getDtoFields()
                .stream()
                .filter(this::fieldCanRefer)
                .collect(Collectors.toList());
        }

        private void suggestReferredField(DtoField localField) {
            if (localField == null) {
                return;
            }
            SourceBox referredSource = referredSourceComboBox.getElement();
            if (referredSource == null) {
                return;
            }
            List<DtoField> dtoFields = getAvailableSourceFields(referredSource.getDtoProxy());
            DtoField suggested = getSuggestedReferredField(localField, dtoFields);
            if (suggested != null) {
                referredFieldComboBox.select(suggested);
            }
        }

        private static DtoField getSuggestedReferredField(DtoField localField, List<DtoField> choices) {
            String localFieldName = localField.getName();
            if (localFieldName == null) {
                return null;
            }
            for (DtoField choice : choices) {
                if (!Objects.equals(choice.getType(), localField.getType())) continue;
                String referredName = choice.getName();
                if (referredName == null) continue;
                if (localFieldName.toUpperCase().contains(referredName)
                    || referredName.toUpperCase().contains(localFieldName)) {
                    return choice;
                }
            }
            for (DtoField choice : choices) {
                String referredName = choice.getName();
                if (referredName == null) continue;
                if (localFieldName.toUpperCase().contains(referredName)
                    || referredName.toUpperCase().contains(localFieldName)) {
                    return choice;
                }
            }
            return null;
        }

        public QueryModel.Join buildJoin(Map<SourceBox, QueryModel.Source> sourceMap) {
            DtoField localField = localFieldComboBox.getElement();
            if (localField == null) {
                throw new ValidationException("local field is not set");
            }
            SourceBox referredSourceBox = referredSourceComboBox.getElement();
            if (referredSourceBox == null) {
                throw new ValidationException("referred source is not set");
            }
            DtoField referredField = referredFieldComboBox.getElement();
            if (referredField == null) {
                throw new ValidationException("referred field is not set");
            }
            return new QueryModel.Join(sourceMap.get(referredSourceBox), referredField, sourceMap.get(sourceBox), localField);
        }
    }

    private static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }

    private static class DtoFieldListCellRenderer implements ComboBoxCustomizer<DtoField> {
        @Override
        public String toString(DtoField value) {
            return value != null ? value.getName() : "";
        }

        @Override
        public void customize(@NotNull JBLabel label, DtoField value, int index) {
            if (value != null) {
                label.setText(value.getName());
            }
        }
    }

    private static class SourceBoxListCellRenderer implements ComboBoxCustomizer<SourceBox> {
        @Override
        public String toString(SourceBox value) {
            return value != null ? value.getAlias() : "";
        }

        @Override
        public void customize(@NotNull JBLabel label, SourceBox value, int index) {
            if (value != null) {
                label.setText(value.getAlias());
            }
        }
    }
}
