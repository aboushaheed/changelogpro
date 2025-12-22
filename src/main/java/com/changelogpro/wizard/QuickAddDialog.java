package com.changelogpro.wizard;

import com.changelogpro.config.*;
import com.changelogpro.services.ChangeLogProjectService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Quick dialog for adding a changelog entry with minimal input.
 */
public class QuickAddDialog extends DialogWrapper {
    
    private final Project project;
    private final ChangeLogProjectService service;
    private final ChangeLogConfig config;
    private final ChangeType type;
    
    private JBTextArea descriptionField;
    private JBTextField issueField;
    private JBTextField prField;
    
    public QuickAddDialog(@NotNull Project project, @NotNull ChangeType type) {
        super(project, true);
        this.project = project;
        this.service = ChangeLogProjectService.getInstance(project);
        this.config = service.getConfig();
        this.type = type;
        
        setTitle("Quick Add: " + type.getDisplayName());
        setSize(450, 300);
        init();
    }
    
    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setPreferredSize(new Dimension(430, 250));
        panel.setBorder(JBUI.Borders.empty(10));
        
        // Header with type info
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JLabel iconLabel = new JLabel(type.getIcon());
        JLabel titleLabel = new JLabel(type.getDisplayName() + " - " + type.getDescription());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Description
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(new JBLabel("Description *"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        descriptionField = new JBTextArea(3, 35);
        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);
        JBScrollPane descScroll = new JBScrollPane(descriptionField);
        formPanel.add(descScroll, gbc);
        
        // Issue and PR in same row
        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JPanel refPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        
        String issueLabel = config.getIssueTracker().getTermName() + " ID";
        refPanel.add(new JBLabel(issueLabel));
        
        String prLabel = config.getGitProvider().getPrTermShort() + " #";
        refPanel.add(new JBLabel(prLabel));
        
        issueField = new JBTextField();
        refPanel.add(issueField);
        
        prField = new JBTextField();
        refPanel.add(prField);
        
        formPanel.add(refPanel, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        // Info
        JLabel infoLabel = new JLabel("<html><small>Entry will be saved to: .changes/unreleased/" + 
            type.getDirectoryName() + "/</small></html>");
        infoLabel.setForeground(JBColor.GRAY);
        panel.add(infoLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    @Override
    protected @Nullable ValidationInfo doValidate() {
        if (descriptionField.getText().trim().isEmpty()) {
            return new ValidationInfo("Description is required", descriptionField);
        }
        return null;
    }
    
    @Override
    protected void doOKAction() {
        ChangeEntry entry = new ChangeEntry(type);
        entry.setDescription(descriptionField.getText().trim());
        entry.setIssueId(issueField.getText().trim());
        entry.setPrNumber(prField.getText().trim());
        
        boolean success = service.getFileService().createChangeEntry(entry);
        
        if (success) {
            super.doOKAction();
        } else {
            setErrorText("Failed to create entry. Check the log for details.");
        }
    }
}
