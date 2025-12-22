package com.changelogpro.wizard;

import com.changelogpro.config.*;
import com.changelogpro.services.ChangeLogProjectService;
import com.changelogpro.services.GitService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
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
 * Wizard dialog for initializing ChangeLog Pro in a project.
 */
public class InitializeWizardDialog extends DialogWrapper {
    
    private final Project project;
    private final ChangeLogProjectService service;
    private final ChangeLogConfig config;
    private final GitService gitService;
    
    // UI Components
    private JBTextField repoUrlField;
    private ComboBox<GitProvider> gitProviderCombo;
    private ComboBox<IssueTracker> issueTrackerCombo;
    private JBTextField issueTrackerUrlField;
    private JBCheckBox autoStageCheckbox;
    private JBTextArea previewArea;
    
    public InitializeWizardDialog(@NotNull Project project) {
        super(project, true);
        this.project = project;
        this.service = ChangeLogProjectService.getInstance(project);
        this.config = service.getConfig();
        this.gitService = service.getGitService();
        
        setTitle("ChangeLog Pro - Initialize Project");
        setSize(600, 550);
        init();
    }
    
    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setPreferredSize(new Dimension(580, 500));
        mainPanel.setBorder(JBUI.Borders.empty(15));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Form
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Preview
        JPanel previewPanel = createPreviewPanel();
        mainPanel.add(previewPanel, BorderLayout.SOUTH);
        
        // Auto-detect settings
        autoDetectSettings();
        
        return mainPanel;
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(JBUI.Borders.empty(0, 0, 15, 0));
        
        JLabel titleLabel = new JLabel("Initialize ChangeLog Pro");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        
        JLabel descLabel = new JLabel("<html>Configure your project for professional changelog management.<br>" +
            "This will create the necessary directory structure and configuration files.</html>");
        descLabel.setForeground(JBColor.GRAY);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(descLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Section: Git Repository
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        JLabel gitSection = new JLabel("Git Repository");
        gitSection.setFont(gitSection.getFont().deriveFont(Font.BOLD, 14f));
        gitSection.setBorder(JBUI.Borders.empty(10, 0, 5, 0));
        panel.add(gitSection, gbc);
        
        // Repository URL
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JBLabel("Repository URL:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        gbc.weightx = 1;
        repoUrlField = new JBTextField();
        repoUrlField.getDocument().addDocumentListener(new SimpleDocumentListener(this::onRepoUrlChanged));
        panel.add(repoUrlField, gbc);
        
        // Git Provider
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JBLabel("Git Provider:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        gbc.weightx = 1;
        gitProviderCombo = new ComboBox<>(GitProvider.values());
        gitProviderCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof GitProvider) {
                    setText(((GitProvider) value).getDisplayName());
                }
                return this;
            }
        });
        gitProviderCombo.addActionListener(e -> updatePreview());
        panel.add(gitProviderCombo, gbc);
        
        // Section: Issue Tracker
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        JLabel issueSection = new JLabel("Issue Tracker");
        issueSection.setFont(issueSection.getFont().deriveFont(Font.BOLD, 14f));
        issueSection.setBorder(JBUI.Borders.empty(15, 0, 5, 0));
        panel.add(issueSection, gbc);
        
        // Issue Tracker Type
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JBLabel("Tracker Type:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        gbc.weightx = 1;
        issueTrackerCombo = new ComboBox<>(IssueTracker.values());
        issueTrackerCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof IssueTracker) {
                    setText(((IssueTracker) value).getDisplayName());
                }
                return this;
            }
        });
        issueTrackerCombo.addActionListener(e -> onIssueTrackerChanged());
        panel.add(issueTrackerCombo, gbc);
        
        // Issue Tracker URL
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JBLabel("Tracker URL:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        gbc.weightx = 1;
        issueTrackerUrlField = new JBTextField();
        issueTrackerUrlField.getDocument().addDocumentListener(new SimpleDocumentListener(this::updatePreview));
        panel.add(issueTrackerUrlField, gbc);
        
        // Section: Options
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        JLabel optionsSection = new JLabel("Options");
        optionsSection.setFont(optionsSection.getFont().deriveFont(Font.BOLD, 14f));
        optionsSection.setBorder(JBUI.Borders.empty(15, 0, 5, 0));
        panel.add(optionsSection, gbc);
        
        // Auto-stage
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        autoStageCheckbox = new JBCheckBox("Automatically stage changelog files after creation", true);
        panel.add(autoStageCheckbox, gbc);
        
        return panel;
    }
    
    private JPanel createPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBorder(JBUI.Borders.empty(10, 0, 0, 0));
        
        JLabel label = new JLabel("Directory Structure Preview:");
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        panel.add(label, BorderLayout.NORTH);
        
        previewArea = new JBTextArea(6, 50);
        previewArea.setEditable(false);
        previewArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        JBScrollPane scrollPane = new JBScrollPane(previewArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        updatePreview();
        
        return panel;
    }
    
    private void autoDetectSettings() {
        // Try to detect repository URL
        String remoteUrl = gitService.getRemoteUrl();
        if (remoteUrl != null && !remoteUrl.isEmpty()) {
            repoUrlField.setText(remoteUrl);
            
            // Auto-detect provider
            GitProvider provider = GitProvider.detectFromUrl(remoteUrl);
            gitProviderCombo.setSelectedItem(provider);
            
            // If using GitHub/GitLab, set issue tracker to their issues
            if (provider == GitProvider.GITHUB) {
                issueTrackerCombo.setSelectedItem(IssueTracker.GITHUB_ISSUES);
            } else if (provider == GitProvider.GITLAB) {
                issueTrackerCombo.setSelectedItem(IssueTracker.GITLAB_ISSUES);
            }
        }
        
        updatePreview();
    }
    
    private void onRepoUrlChanged() {
        String url = repoUrlField.getText().trim();
        if (!url.isEmpty()) {
            GitProvider provider = GitProvider.detectFromUrl(url);
            gitProviderCombo.setSelectedItem(provider);
        }
        updatePreview();
    }
    
    private void onIssueTrackerChanged() {
        IssueTracker tracker = (IssueTracker) issueTrackerCombo.getSelectedItem();
        if (tracker != null && tracker.usesRepoUrl()) {
            issueTrackerUrlField.setEnabled(false);
            issueTrackerUrlField.setText("(Uses repository URL)");
        } else {
            issueTrackerUrlField.setEnabled(true);
            if (issueTrackerUrlField.getText().equals("(Uses repository URL)")) {
                issueTrackerUrlField.setText("");
            }
        }
        updatePreview();
    }
    
    private void updatePreview() {
        StringBuilder preview = new StringBuilder();
        preview.append("Project Root/\n");
        preview.append("├── .changes/\n");
        preview.append("│   └── unreleased/\n");
        
        ChangeType[] types = ChangeType.values();
        for (int i = 0; i < types.length; i++) {
            String prefix = (i == types.length - 1) ? "│       └── " : "│       ├── ";
            preview.append(prefix).append(types[i].getDirectoryName()).append("/\n");
        }
        
        preview.append("├── changelog-pro.properties\n");
        preview.append("└── CHANGELOG.md\n");
        
        previewArea.setText(preview.toString());
    }
    
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String repoUrl = repoUrlField.getText().trim();
        if (repoUrl.isEmpty()) {
            return new ValidationInfo("Repository URL is required", repoUrlField);
        }
        
        IssueTracker tracker = (IssueTracker) issueTrackerCombo.getSelectedItem();
        if (tracker != null && !tracker.usesRepoUrl() && tracker != IssueTracker.NONE) {
            String trackerUrl = issueTrackerUrlField.getText().trim();
            if (trackerUrl.isEmpty() || trackerUrl.equals("(Uses repository URL)")) {
                return new ValidationInfo("Issue tracker URL is required for " + tracker.getDisplayName(), issueTrackerUrlField);
            }
        }
        
        return null;
    }
    
    @Override
    protected void doOKAction() {
        // Save configuration
        config.setRepoUrl(repoUrlField.getText().trim());
        config.setGitProvider((GitProvider) gitProviderCombo.getSelectedItem());
        config.setIssueTracker((IssueTracker) issueTrackerCombo.getSelectedItem());
        
        IssueTracker tracker = config.getIssueTracker();
        if (tracker != null && !tracker.usesRepoUrl()) {
            config.setIssueTrackerUrl(issueTrackerUrlField.getText().trim());
        } else {
            config.setIssueTrackerUrl("");
        }
        
        config.setAutoStage(autoStageCheckbox.isSelected());
        
        // Initialize project
        boolean success = service.getFileService().initializeProject();
        
        if (success) {
            super.doOKAction();
        } else {
            setErrorText("Failed to initialize project. Check the log for details.");
        }
    }
    
    /**
     * Simple document listener for text field changes.
     */
    private static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final Runnable action;
        
        SimpleDocumentListener(Runnable action) {
            this.action = action;
        }
        
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            action.run();
        }
        
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            action.run();
        }
        
        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            action.run();
        }
    }
}
