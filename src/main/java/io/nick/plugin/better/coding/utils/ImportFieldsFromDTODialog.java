package io.nick.plugin.better.coding.utils;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.refactoring.ui.MemberSelectionPanel;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.JBUI;
import io.nick.plugin.better.coding.proxy.DtoField;
import io.nick.plugin.better.coding.proxy.DtoProxy;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ImportFieldsFromDTODialog extends DialogWrapper {
    private final ContentPanel contentPanel;

    public ImportFieldsFromDTODialog(PsiElement context) {
        super(context.getProject());
        setTitle("Import Fields From DTO");
        contentPanel = new ContentPanel(context);
        init();
        getOKAction().putValue(Action.NAME, "Create");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPanel;
    }

    public DtoProxy getSelectedDtoProxy() {
        PsiClass psiClass = contentPanel.dtoProxyTextField.getEnteredClass();
        return psiClass != null ? new DtoProxy(psiClass) : null;
    }

    public List<DtoField> getSelectedDtoFields() {
        DtoProxy dtoProxy = getSelectedDtoProxy();
        if (dtoProxy == null) {
            return Collections.emptyList();
        }
        return contentPanel.memberSelectionPanel.getTable()
            .getSelectedMemberInfos()
            .stream()
            .map(m -> (PsiField) m.getMember())
            .map(f -> new DtoField(dtoProxy, f))
            .collect(Collectors.toList());
    }

    private static class ContentPanel extends JBPanel<ContentPanel> {
        final ClassTextField dtoProxyTextField;
        final MemberSelectionPanel memberSelectionPanel;

        public ContentPanel(PsiElement context) {
            super(new GridBagLayout());
            setPreferredSize(new JBDimension(600, 500));

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.WEST;
            constraints.weightx = 1;

            JBLabel fromLabel = new JBLabel("Import from: ");
            FontStyleUtil.bold(fromLabel);
            constraints.gridy = 0;
            add(fromLabel, constraints);

            dtoProxyTextField = new ClassTextField(context);
            dtoProxyTextField.setPlaceholder("DTO class");
            constraints.gridy = 1;
            constraints.insets = JBUI.insetsTop(5);
            constraints.fill = GridBagConstraints.HORIZONTAL;
            add(dtoProxyTextField, constraints);

            memberSelectionPanel = new MemberSelectionPanel("Fields to Import", Collections.emptyList(), null);
            constraints.gridy = 2;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.insets = JBUI.insetsTop(10);
            constraints.weighty = 1;
            add(memberSelectionPanel, constraints);

            dtoProxyTextField.classChanged.listen(selected -> {
                if (selected == null) {
                    memberSelectionPanel.getTable().setMemberInfos(Collections.emptyList());
                } else {
                    DtoProxy dtoProxy = new DtoProxy(selected);
                    List<MemberInfo> memberInfos = dtoProxy.getDtoFields()
                        .stream()
                        .filter(f -> !f.isLogicalDeleteField())
                        .map(f -> {
                            MemberInfo memberInfo = new MemberInfo(f.psiField);
                            memberInfo.setChecked(true);
                            return memberInfo;
                        })
                        .collect(Collectors.toList());
                    memberSelectionPanel.getTable().setMemberInfos(memberInfos);
                }
            });
        }
    }
}
