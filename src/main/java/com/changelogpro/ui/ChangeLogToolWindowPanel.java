package com.changelogpro.ui;

import com.changelogpro.config.*;
import com.changelogpro.services.ChangeLogProjectService;
import com.changelogpro.services.LogService;
import com.changelogpro.wizard.GenerateChangelogDialog;
import com.changelogpro.wizard.InitializeWizardDialog;
import com.changelogpro.wizard.NewEntryWizardDialog;
import com.changelogpro.wizard.QuickAddDialog;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.*;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Main tool window panel for ChangeLog Pro.
 * Contains tabs for Actions, Configuration, Log, and Documentation.
 */
public class ChangeLogToolWindowPanel extends JPanel {
    
    private final Project project;
    private final ToolWindow toolWindow;
    private final ChangeLogProjectService service;
    private final ChangeLogConfig config;
    
    // UI Components
    private JBTabbedPane tabbedPane;
    private JPanel statusPanel;
    private JBLabel statusLabel;
    private JBTextArea logArea;
    private JPanel statsPanel;
    
    // Config tab components
    private JBTextField repoUrlField;
    private ComboBox<GitProvider> gitProviderCombo;
    private ComboBox<IssueTracker> issueTrackerCombo;
    private JBTextField issueTrackerUrlField;
    private JBCheckBox autoStageCheckbox;
    
    public ChangeLogToolWindowPanel(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        super(new BorderLayout());
        this.project = project;
        this.toolWindow = toolWindow;
        this.service = ChangeLogProjectService.getInstance(project);
        this.config = service.getConfig();
        
        initUI();
        setupLogListener();
        refreshStatus();
    }
    
    private void initUI() {
        // Status bar at top
        statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.NORTH);
        
        // Tabbed pane
        tabbedPane = new JBTabbedPane();
        tabbedPane.addTab("Actions", AllIcons.Actions.Execute, createActionsPanel());
        tabbedPane.addTab("Config", AllIcons.General.Settings, createConfigPanel());
        tabbedPane.addTab("Log", AllIcons.Debugger.Console, createLogPanel());
        tabbedPane.addTab("Help", AllIcons.Actions.Help, createHelpPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(JBUI.Borders.empty(8, 10));
        panel.setBackground(new JBColor(new Color(0xE3F2FD), new Color(0x1A237E)));
        
        statusLabel = new JBLabel();
        statusLabel.setIcon(AllIcons.General.Information);
        panel.add(statusLabel, BorderLayout.CENTER);
        
        JButton refreshButton = new JButton(AllIcons.Actions.Refresh);
        refreshButton.setToolTipText("Refresh status");
        refreshButton.setBorderPainted(false);
        refreshButton.setContentAreaFilled(false);
        refreshButton.addActionListener(e -> refreshStatus());
        panel.add(refreshButton, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(JBUI.Borders.empty(15));
        
        // Main actions section
        JPanel mainActionsPanel = new JPanel();
        mainActionsPanel.setLayout(new BoxLayout(mainActionsPanel, BoxLayout.Y_AXIS));
        mainActionsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Main Actions",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP
        ));
        
        JPanel mainButtonsPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        mainButtonsPanel.setBorder(JBUI.Borders.empty(10));
        
        JButton initButton = createActionButton("Initialize Project", 
            "Set up ChangeLog Pro for this project",
            AllIcons.Actions.Execute,
            e -> openInitializeDialog());
        mainButtonsPanel.add(initButton);
        
        JButton newEntryButton = createActionButton("New Entry (Wizard)", 
            "Create a new changelog entry with the wizard",
            AllIcons.General.Add,
            e -> openNewEntryWizard());
        mainButtonsPanel.add(newEntryButton);
        
        // Generate CHANGELOG button - prominent styling
        JButton generateButton = createActionButton("Generate CHANGELOG", 
            "Generate CHANGELOG.md from unreleased entries",
            AllIcons.Actions.Compile,
            e -> openGenerateChangelogDialog());
        generateButton.setFont(generateButton.getFont().deriveFont(Font.BOLD));
        generateButton.setBackground(new JBColor(new Color(0x4CAF50), new Color(0x2E7D32)));
        generateButton.setForeground(JBColor.WHITE);
        generateButton.setOpaque(true);
        mainButtonsPanel.add(generateButton);
        
        mainActionsPanel.add(mainButtonsPanel);
        panel.add(mainActionsPanel, BorderLayout.NORTH);
        
        // Quick add section
        JPanel quickAddPanel = new JPanel(new BorderLayout(0, 5));
        quickAddPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Quick Add Entry",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP
        ));
        
        JPanel quickButtonsPanel = new JPanel(new GridLayout(3, 2, 6, 6));
        quickButtonsPanel.setBorder(JBUI.Borders.empty(8));
        
        for (ChangeType type : ChangeType.values()) {
            JButton btn = new JButton(type.getDisplayName());
            btn.setIcon(type.getIcon());
            btn.setToolTipText(type.getDescription());
            btn.addActionListener(e -> openQuickAddDialog(type));
            quickButtonsPanel.add(btn);
        }
        
        quickAddPanel.add(quickButtonsPanel, BorderLayout.CENTER);
        panel.add(quickAddPanel, BorderLayout.CENTER);
        
        // Stats panel
        statsPanel = createStatsPanel();
        panel.add(statsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JButton createActionButton(String text, String tooltip, Icon icon, ActionListener action) {
        JButton button = new JButton(text);
        button.setIcon(icon);
        button.setToolTipText(tooltip);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.addActionListener(action);
        return button;
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Unreleased Changes",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP
        ));
        
        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 5, 5));
        statsGrid.setBorder(JBUI.Borders.empty(8));
        
        for (ChangeType type : ChangeType.values()) {
            int count = service.getFileService().countUnreleasedEntries(type);
            JLabel label = new JLabel(type.getDisplayName() + ": " + count);
            label.setIcon(type.getIcon());
            statsGrid.add(label);
        }
        
        panel.add(statsGrid, BorderLayout.CENTER);
        
        // Total count
        int total = service.getFileService().countTotalUnreleasedEntries();
        JLabel totalLabel = new JLabel("Total: " + total + " entries ready to release");
        totalLabel.setBorder(JBUI.Borders.empty(5, 10));
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
        panel.add(totalLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(15));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Repository URL
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JBLabel("Repository URL:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        gbc.weightx = 1;
        repoUrlField = new JBTextField(config.getRepoUrl());
        formPanel.add(repoUrlField, gbc);
        
        // Git Provider
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JBLabel("Git Provider:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        gbc.weightx = 1;
        gitProviderCombo = new ComboBox<>(GitProvider.values());
        gitProviderCombo.setSelectedItem(config.getGitProvider());
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
        formPanel.add(gitProviderCombo, gbc);
        
        // Issue Tracker
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JBLabel("Issue Tracker:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        gbc.weightx = 1;
        issueTrackerCombo = new ComboBox<>(IssueTracker.values());
        issueTrackerCombo.setSelectedItem(config.getIssueTracker());
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
        formPanel.add(issueTrackerCombo, gbc);
        
        // Issue Tracker URL
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JBLabel("Tracker URL:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        gbc.weightx = 1;
        issueTrackerUrlField = new JBTextField(config.getIssueTrackerUrl());
        formPanel.add(issueTrackerUrlField, gbc);
        
        // Auto-stage
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        autoStageCheckbox = new JBCheckBox("Auto-stage files after creation", config.isAutoStage());
        formPanel.add(autoStageCheckbox, gbc);
        
        // Save button
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        JButton saveButton = new JButton("Save Configuration");
        saveButton.setIcon(AllIcons.Actions.MenuSaveall);
        saveButton.addActionListener(e -> saveConfig());
        formPanel.add(saveButton, gbc);
        
        panel.add(formPanel, BorderLayout.NORTH);
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBorder(JBUI.Borders.empty(10));
        
        logArea = new JBTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        JBScrollPane scrollPane = new JBScrollPane(logArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton clearButton = new JButton("Clear Log");
        clearButton.addActionListener(e -> {
            logArea.setText("");
            service.getLogService().clear();
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(clearButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Load existing logs
        for (LogService.LogEntry entry : service.getLogService().getLogEntries()) {
            appendLog(entry);
        }
        
        return panel;
    }
    
    private JPanel createHelpPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(15));
        
        JBTextArea helpText = new JBTextArea();
        helpText.setEditable(false);
        helpText.setLineWrap(true);
        helpText.setWrapStyleWord(true);
        helpText.setText(getHelpContent());
        
        JBScrollPane scrollPane = new JBScrollPane(helpText);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton docsButton = new JButton("Open Full Documentation");
        docsButton.setIcon(AllIcons.Actions.Help);
        docsButton.addActionListener(e -> openDocumentation());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(docsButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private String getHelpContent() {
        return "CHANGELOG PRO - QUICK GUIDE\n" +
            "============================\n\n" +
            "What is a Changelog?\n" +
            "--------------------\n" +
            "A changelog is a file that contains a curated, chronologically ordered list of notable changes for each version of a project.\n\n" +
            "Why Keep a Changelog?\n" +
            "---------------------\n" +
            "- Makes it easier for users to see what changes have been made\n" +
            "- Helps contributors understand the project's history\n" +
            "- Provides transparency about the project's development\n" +
            "- Required for professional software releases\n\n" +
            "Change Types (Keep a Changelog Standard)\n" +
            "-----------------------------------------\n" +
            "- Added - New features\n" +
            "- Changed - Changes in existing functionality\n" +
            "- Deprecated - Soon-to-be removed features\n" +
            "- Removed - Now removed features\n" +
            "- Fixed - Bug fixes\n" +
            "- Security - Vulnerability fixes\n\n" +
            "Workflow\n" +
            "--------\n" +
            "1. Initialize Project - Set up the changelog structure\n" +
            "2. Add Entries - Create entries as you make changes\n" +
            "3. Generate CHANGELOG - Compile entries into CHANGELOG.md\n\n" +
            "Supported Build Systems\n" +
            "-----------------------\n" +
            "- Maven (pom.xml)\n" +
            "- Gradle Groovy (build.gradle)\n" +
            "- Gradle Kotlin (build.gradle.kts)\n\n" +
            "Learn More: https://keepachangelog.com";
    }
    
    private void setupLogListener() {
        service.getLogService().addListener(entry -> {
            ApplicationManager.getApplication().invokeLater(() -> appendLog(entry));
        });
    }
    
    private void appendLog(LogService.LogEntry entry) {
        if (logArea != null) {
            logArea.append(entry.toString() + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }
    
    private void refreshStatus() {
        boolean initialized = service.isInitialized();
        int totalEntries = service.getFileService().countTotalUnreleasedEntries();
        
        if (initialized) {
            statusLabel.setText("Project initialized | " + totalEntries + " unreleased entries");
            statusLabel.setIcon(AllIcons.General.InspectionsOK);
            statusPanel.setBackground(new JBColor(new Color(0xE8F5E9), new Color(0x1B5E20)));
        } else {
            statusLabel.setText("Project not initialized - Click 'Initialize Project' to start");
            statusLabel.setIcon(AllIcons.General.Warning);
            statusPanel.setBackground(new JBColor(new Color(0xFFF3E0), new Color(0xE65100)));
        }
        
        // Refresh the entire panel to update stats
        revalidate();
        repaint();
    }
    
    private void openInitializeDialog() {
        InitializeWizardDialog dialog = new InitializeWizardDialog(project);
        if (dialog.showAndGet()) {
            refreshStatus();
            service.getLogService().success("Project initialized successfully!");
        }
    }
    
    private void openNewEntryWizard() {
        if (!service.isInitialized()) {
            service.getLogService().warn("Please initialize the project first");
            JOptionPane.showMessageDialog(this, 
                "Please initialize the project first by clicking 'Initialize Project'.",
                "Project Not Initialized",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        NewEntryWizardDialog dialog = new NewEntryWizardDialog(project);
        if (dialog.showAndGet()) {
            refreshStatus();
        }
    }
    
    private void openQuickAddDialog(ChangeType type) {
        if (!service.isInitialized()) {
            service.getLogService().warn("Please initialize the project first");
            JOptionPane.showMessageDialog(this, 
                "Please initialize the project first by clicking 'Initialize Project'.",
                "Project Not Initialized",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        QuickAddDialog dialog = new QuickAddDialog(project, type);
        if (dialog.showAndGet()) {
            refreshStatus();
        }
    }
    
    private void openGenerateChangelogDialog() {
        if (!service.isInitialized()) {
            service.getLogService().warn("Please initialize the project first");
            JOptionPane.showMessageDialog(this, 
                "Please initialize the project first by clicking 'Initialize Project'.",
                "Project Not Initialized",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int totalEntries = service.getFileService().countTotalUnreleasedEntries();
        if (totalEntries == 0) {
            int result = JOptionPane.showConfirmDialog(this,
                "No unreleased entries found. Generate an empty changelog section?",
                "No Entries",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        GenerateChangelogDialog dialog = new GenerateChangelogDialog(project);
        if (dialog.showAndGet()) {
            refreshStatus();
        }
    }
    
    private void saveConfig() {
        config.setRepoUrl(repoUrlField.getText().trim());
        config.setGitProvider((GitProvider) gitProviderCombo.getSelectedItem());
        config.setIssueTracker((IssueTracker) issueTrackerCombo.getSelectedItem());
        config.setIssueTrackerUrl(issueTrackerUrlField.getText().trim());
        config.setAutoStage(autoStageCheckbox.isSelected());
        config.save();
        
        service.getLogService().success("Configuration saved");
        JOptionPane.showMessageDialog(this, "Configuration saved successfully!", 
            "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openDocumentation() {
        // Open documentation dialog
        DocumentationDialog dialog = new DocumentationDialog(project);
        dialog.show();
    }
}
