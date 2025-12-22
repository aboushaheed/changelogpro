package com.changelogpro.wizard;

import com.changelogpro.config.ChangeEntry;
import com.changelogpro.config.ChangeType;
import com.changelogpro.services.*;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.Action;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Dialog for generating CHANGELOG.md with flexible options.
 * Supports simple generation to complete release workflow with Maven/Gradle integration.
 */
public class GenerateChangelogDialog extends DialogWrapper {
    
    private final Project project;
    private final ChangeLogProjectService projectService;
    private final ChangelogGenerator generator;
    private final GitService gitService;
    private final LogService logger;
    private final ProjectVersionService versionService;
    
    // UI Components
    private JTextField versionField;
    private JLabel detectedVersionLabel;
    private JLabel projectTypeLabel;
    private JCheckBox includeAuthorCheckbox;
    private JCheckBox updateProjectVersionCheckbox;
    private JCheckBox bumpToSnapshotCheckbox;
    private JTextField nextSnapshotField;
    private JCheckBox archiveFragmentsCheckbox;
    private JCheckBox deleteFragmentsCheckbox;
    private JCheckBox createTagCheckbox;
    private JCheckBox pushCheckbox;
    private JCheckBox pushTagsCheckbox;
    private JBTextArea previewArea;
    private JLabel entriesCountLabel;
    private JPanel optionsPanel;
    
    public GenerateChangelogDialog(@NotNull Project project) {
        super(project, true);
        this.project = project;
        this.projectService = ChangeLogProjectService.getInstance(project);
        this.logger = projectService.getLogService();
        this.generator = new ChangelogGenerator(project);
        this.gitService = new GitService(project, logger);
        this.versionService = new ProjectVersionService(project, logger);
        
        setTitle("Generate CHANGELOG");
        setSize(800, 650);
        setOKButtonText("Generate");
        init();
        
        // Initial preview
        updatePreview();
    }
    
    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(JBUI.Borders.empty(10));
        
        // Top: Summary
        mainPanel.add(createSummaryPanel(), BorderLayout.NORTH);
        
        // Center: Preview
        mainPanel.add(createPreviewPanel(), BorderLayout.CENTER);
        
        // Right: Options
        mainPanel.add(createOptionsPanel(), BorderLayout.EAST);
        
        return mainPanel;
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(JBUI.Borders.empty(0, 0, 10, 0));
        
        // Title
        JBLabel titleLabel = new JBLabel("Generate CHANGELOG.md", AllIcons.FileTypes.Text, SwingConstants.LEFT);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        
        // Entries count
        int count = generator.countUnreleasedEntries();
        entriesCountLabel = new JLabel();
        updateEntriesCount(count);
        infoPanel.add(entriesCountLabel);
        
        // Project type
        ProjectVersionService.ProjectType projectType = versionService.detectProjectType();
        projectTypeLabel = new JLabel("Project: " + projectType.getDisplayName());
        infoPanel.add(projectTypeLabel);
        
        // Detected version
        String currentVersion = versionService.getCurrentVersion();
        detectedVersionLabel = new JLabel();
        if (currentVersion != null) {
            detectedVersionLabel.setText("<html>Detected version: <b>" + currentVersion + "</b></html>");
        } else {
            detectedVersionLabel.setText("<html>Detected version: <font color='gray'>Not found</font></html>");
        }
        infoPanel.add(detectedVersionLabel);
        
        panel.add(infoPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void updateEntriesCount(int count) {
        if (count == 0) {
            entriesCountLabel.setText("<html><font color='orange'>⚠ No unreleased entries found</font></html>");
        } else {
            entriesCountLabel.setText("<html><font color='green'>✓ " + count + " entries ready</font></html>");
        }
    }
    
    private JPanel createPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Preview",
            TitledBorder.LEFT,
            TitledBorder.TOP
        ));
        
        previewArea = new JBTextArea();
        previewArea.setEditable(false);
        previewArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);
        
        JBScrollPane scrollPane = new JBScrollPane(previewArea);
        scrollPane.setPreferredSize(new Dimension(400, 350));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Refresh button
        JButton refreshButton = new JButton("Refresh", AllIcons.Actions.Refresh);
        refreshButton.addActionListener(e -> updatePreview());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createOptionsPanel() {
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Options",
            TitledBorder.LEFT,
            TitledBorder.TOP
        ));
        optionsPanel.setPreferredSize(new Dimension(280, 500));
        
        // ========== Version Section ==========
        addSectionHeader("Release Version");
        
        JPanel versionPanel = new JPanel(new BorderLayout(5, 2));
        versionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        versionPanel.setMaximumSize(new Dimension(270, 45));
        
        versionField = new JTextField();
        versionField.setToolTipText("Leave empty for [Unreleased] section");
        
        // Pre-fill with detected version (without SNAPSHOT)
        String currentVersion = versionService.getCurrentVersion();
        if (currentVersion != null) {
            versionField.setText(versionService.removeSnapshot(currentVersion));
        }
        
        versionField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { onVersionChanged(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { onVersionChanged(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { onVersionChanged(); }
        });
        
        JLabel versionHint = new JLabel("<html><small>e.g., 1.2.0 (empty = Unreleased)</small></html>");
        versionHint.setForeground(JBColor.GRAY);
        
        versionPanel.add(versionField, BorderLayout.CENTER);
        versionPanel.add(versionHint, BorderLayout.SOUTH);
        optionsPanel.add(versionPanel);
        
        optionsPanel.add(Box.createVerticalStrut(10));
        
        // ========== Content Options ==========
        addSectionHeader("Content Options");
        
        includeAuthorCheckbox = createCheckbox("Include author in entries", true);
        includeAuthorCheckbox.addActionListener(e -> updatePreview());
        optionsPanel.add(includeAuthorCheckbox);
        
        optionsPanel.add(Box.createVerticalStrut(10));
        
        // ========== Project Version Section ==========
        addSectionHeader("Project Version (Maven/Gradle)");
        
        updateProjectVersionCheckbox = createCheckbox("Update project version", false);
        updateProjectVersionCheckbox.setToolTipText("Update version in pom.xml or build.gradle");
        updateProjectVersionCheckbox.addActionListener(e -> updateVersionOptionsState());
        optionsPanel.add(updateProjectVersionCheckbox);
        
        bumpToSnapshotCheckbox = createCheckbox("Bump to next SNAPSHOT after release", false);
        bumpToSnapshotCheckbox.setEnabled(false);
        bumpToSnapshotCheckbox.addActionListener(e -> updateNextSnapshotState());
        optionsPanel.add(bumpToSnapshotCheckbox);
        
        JPanel snapshotPanel = new JPanel(new BorderLayout(5, 0));
        snapshotPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        snapshotPanel.setMaximumSize(new Dimension(270, 25));
        snapshotPanel.setBorder(JBUI.Borders.emptyLeft(20));
        
        JLabel snapshotLabel = new JLabel("Next: ");
        nextSnapshotField = new JTextField();
        nextSnapshotField.setEnabled(false);
        
        snapshotPanel.add(snapshotLabel, BorderLayout.WEST);
        snapshotPanel.add(nextSnapshotField, BorderLayout.CENTER);
        optionsPanel.add(snapshotPanel);
        
        optionsPanel.add(Box.createVerticalStrut(10));
        
        // ========== Fragment Handling ==========
        addSectionHeader("After Generation");
        
        archiveFragmentsCheckbox = createCheckbox("Archive fragments to version folder", false);
        archiveFragmentsCheckbox.setToolTipText("Move to .changes/vX.X.X/");
        archiveFragmentsCheckbox.addActionListener(e -> {
            if (archiveFragmentsCheckbox.isSelected()) {
                deleteFragmentsCheckbox.setSelected(false);
            }
        });
        optionsPanel.add(archiveFragmentsCheckbox);
        
        deleteFragmentsCheckbox = createCheckbox("Delete fragments", false);
        deleteFragmentsCheckbox.addActionListener(e -> {
            if (deleteFragmentsCheckbox.isSelected()) {
                archiveFragmentsCheckbox.setSelected(false);
            }
        });
        optionsPanel.add(deleteFragmentsCheckbox);
        
        optionsPanel.add(Box.createVerticalStrut(10));
        
        // ========== Git Options ==========
        addSectionHeader("Git Options");
        
        createTagCheckbox = createCheckbox("Create Git tag (vX.X.X)", false);
        createTagCheckbox.addActionListener(e -> updateGitOptionsState());
        optionsPanel.add(createTagCheckbox);
        
        pushCheckbox = createCheckbox("Push changes to remote", false);
        pushCheckbox.addActionListener(e -> updateGitOptionsState());
        optionsPanel.add(pushCheckbox);
        
        pushTagsCheckbox = createCheckbox("Push tags", false);
        pushTagsCheckbox.setEnabled(false);
        pushTagsCheckbox.setBorder(JBUI.Borders.emptyLeft(20));
        optionsPanel.add(pushTagsCheckbox);
        
        optionsPanel.add(Box.createVerticalStrut(15));
        
        // ========== Workflow Info ==========
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new JBColor(0xE3F2FD, 0x2D3A4A)),
            JBUI.Borders.empty(8)
        ));
        infoPanel.setBackground(new JBColor(0xE3F2FD, 0x2D3A4A));
        infoPanel.setMaximumSize(new Dimension(270, 100));
        
        JLabel infoLabel = new JLabel("<html><small>" +
            "<b>Workflow Levels:</b><br>" +
            "• <b>Simple:</b> Generate CHANGELOG only<br>" +
            "• <b>Version:</b> + update project version<br>" +
            "• <b>Release:</b> + tag + push" +
            "</small></html>");
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(infoLabel);
        
        optionsPanel.add(infoPanel);
        
        optionsPanel.add(Box.createVerticalGlue());
        
        // Initialize states
        onVersionChanged();
        
        return optionsPanel;
    }
    
    private void addSectionHeader(String title) {
        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(JBUI.Borders.empty(5, 0, 3, 0));
        optionsPanel.add(label);
    }
    
    private JCheckBox createCheckbox(String text, boolean selected) {
        JCheckBox checkbox = new JCheckBox(text);
        checkbox.setSelected(selected);
        checkbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        return checkbox;
    }
    
    private void onVersionChanged() {
        String version = versionField.getText().trim();
        boolean hasVersion = !version.isEmpty();
        
        // Archive requires version
        archiveFragmentsCheckbox.setEnabled(hasVersion);
        if (!hasVersion) {
            archiveFragmentsCheckbox.setSelected(false);
        }
        
        // Tag requires version
        createTagCheckbox.setEnabled(hasVersion);
        if (!hasVersion) {
            createTagCheckbox.setSelected(false);
        }
        
        // Update project version requires version
        updateProjectVersionCheckbox.setEnabled(hasVersion);
        if (!hasVersion) {
            updateProjectVersionCheckbox.setSelected(false);
        }
        
        // Calculate next snapshot
        if (hasVersion) {
            String nextSnapshot = versionService.calculateNextSnapshot(version);
            nextSnapshotField.setText(nextSnapshot);
        } else {
            nextSnapshotField.setText("");
        }
        
        updateVersionOptionsState();
        updatePreview();
    }
    
    private void updateVersionOptionsState() {
        boolean canBump = updateProjectVersionCheckbox.isSelected() && updateProjectVersionCheckbox.isEnabled();
        bumpToSnapshotCheckbox.setEnabled(canBump);
        if (!canBump) {
            bumpToSnapshotCheckbox.setSelected(false);
        }
        updateNextSnapshotState();
    }
    
    private void updateNextSnapshotState() {
        nextSnapshotField.setEnabled(bumpToSnapshotCheckbox.isSelected() && bumpToSnapshotCheckbox.isEnabled());
    }
    
    private void updateGitOptionsState() {
        boolean canPushTags = createTagCheckbox.isSelected() && pushCheckbox.isSelected();
        pushTagsCheckbox.setEnabled(canPushTags);
        if (!canPushTags) {
            pushTagsCheckbox.setSelected(false);
        }
    }
    
    private void updatePreview() {
        String version = versionField.getText().trim();
        boolean includeAuthor = includeAuthorCheckbox.isSelected();
        
        String preview = generator.previewChangelog(
            version.isEmpty() ? null : version,
            includeAuthor
        );
        
        previewArea.setText(preview);
        previewArea.setCaretPosition(0);
    }
    
    @Override
    protected void doOKAction() {
        String version = versionField.getText().trim();
        boolean includeAuthor = includeAuthorCheckbox.isSelected();
        boolean updateProjectVersion = updateProjectVersionCheckbox.isSelected();
        boolean bumpToSnapshot = bumpToSnapshotCheckbox.isSelected();
        String nextSnapshot = nextSnapshotField.getText().trim();
        boolean archive = archiveFragmentsCheckbox.isSelected();
        boolean delete = deleteFragmentsCheckbox.isSelected();
        boolean createTag = createTagCheckbox.isSelected();
        boolean push = pushCheckbox.isSelected();
        boolean pushTags = pushTagsCheckbox.isSelected();
        
        // Validate
        if (createTag && version.isEmpty()) {
            Messages.showWarningDialog(project, "Version is required to create a tag.", "Validation Error");
            return;
        }
        
        if (archive && version.isEmpty()) {
            Messages.showWarningDialog(project, "Version is required to archive fragments.", "Validation Error");
            return;
        }
        
        if (updateProjectVersion && version.isEmpty()) {
            Messages.showWarningDialog(project, "Version is required to update project version.", "Validation Error");
            return;
        }
        
        // Check if tag already exists
        if (createTag && gitService.tagExists("v" + version)) {
            int result = Messages.showYesNoDialog(
                project,
                "Tag v" + version + " already exists. Continue without creating tag?",
                "Tag Exists",
                Messages.getWarningIcon()
            );
            if (result != Messages.YES) {
                return;
            }
            createTag = false;
        }
        
        // Confirm push
        if (push) {
            int result = Messages.showYesNoDialog(
                project,
                "This will push changes to the remote repository. Continue?",
                "Confirm Push",
                Messages.getQuestionIcon()
            );
            if (result != Messages.YES) {
                push = false;
                pushTags = false;
            }
        }
        
        // Execute workflow
        executeWorkflow(version, includeAuthor, updateProjectVersion, bumpToSnapshot, nextSnapshot,
                       archive, delete, createTag, push, pushTags);
        
        super.doOKAction();
    }
    
    private void executeWorkflow(String version, boolean includeAuthor, boolean updateProjectVersion,
                                  boolean bumpToSnapshot, String nextSnapshot, boolean archive, 
                                  boolean delete, boolean createTag, boolean push, boolean pushTags) {
        
        logger.info("=== Starting CHANGELOG Generation Workflow ===");
        
        int step = 1;
        int totalSteps = calculateTotalSteps(updateProjectVersion, bumpToSnapshot, archive || delete, createTag, push);
        
        // Step: Update project version (release)
        if (updateProjectVersion && !version.isEmpty()) {
            logger.info("Step " + step + "/" + totalSteps + ": Updating project version to " + version);
            if (!versionService.setVersion(version)) {
                logger.warn("Failed to update project version");
            }
            step++;
        }
        
        // Step: Generate CHANGELOG.md
        logger.info("Step " + step + "/" + totalSteps + ": Generating CHANGELOG.md");
        String content = generator.generateChangelog(
            version.isEmpty() ? null : version,
            includeAuthor
        );
        
        if (!generator.writeChangelog(content)) {
            logger.error("Failed to write CHANGELOG.md");
            Messages.showErrorDialog(project, "Failed to write CHANGELOG.md", "Error");
            return;
        }
        step++;
        
        // Step: Stage changes
        logger.info("Step " + step + "/" + totalSteps + ": Staging files");
        gitService.stageFile("CHANGELOG.md");
        
        // Stage build files if version was updated
        if (updateProjectVersion) {
            ProjectVersionService.ProjectType projectType = versionService.detectProjectType();
            if (projectType.getBuildFile() != null) {
                gitService.stageFile(projectType.getBuildFile());
            }
            // Also stage gradle.properties if exists
            gitService.stageFile("gradle.properties");
        }
        step++;
        
        // Step: Handle fragments
        if (archive && !version.isEmpty()) {
            logger.info("Step " + step + "/" + totalSteps + ": Archiving fragments to v" + version);
            if (!generator.archiveFragments(version)) {
                logger.warn("Failed to archive some fragments");
            }
            gitService.stageFile(".changes/");
            step++;
        } else if (delete) {
            logger.info("Step " + step + "/" + totalSteps + ": Deleting unreleased fragments");
            if (!generator.deleteUnreleasedFragments()) {
                logger.warn("Failed to delete some fragments");
            }
            gitService.stageFile(".changes/");
            step++;
        }
        
        // Step: Commit release
        String commitMessage;
        if (!version.isEmpty()) {
            commitMessage = "chore(release): release version " + version;
        } else {
            commitMessage = "chore(changelog): update CHANGELOG.md";
        }
        
        logger.info("Step " + step + "/" + totalSteps + ": Committing: " + commitMessage);
        if (!gitService.commit(commitMessage)) {
            logger.warn("Commit may have failed or there were no changes");
        }
        step++;
        
        // Step: Create tag
        if (createTag && !version.isEmpty()) {
            logger.info("Step " + step + "/" + totalSteps + ": Creating tag v" + version);
            if (!gitService.createTag("v" + version, "Release " + version)) {
                logger.error("Failed to create tag");
            }
            step++;
        }
        
        // Step: Bump to next snapshot
        if (bumpToSnapshot && !nextSnapshot.isEmpty()) {
            logger.info("Step " + step + "/" + totalSteps + ": Bumping to next snapshot " + nextSnapshot);
            if (versionService.setVersion(nextSnapshot)) {
                // Stage and commit
                ProjectVersionService.ProjectType projectType = versionService.detectProjectType();
                if (projectType.getBuildFile() != null) {
                    gitService.stageFile(projectType.getBuildFile());
                }
                gitService.stageFile("gradle.properties");
                gitService.commit("chore(release): prepare for next development iteration " + nextSnapshot);
            } else {
                logger.warn("Failed to bump to snapshot version");
            }
            step++;
        }
        
        // Step: Push
        if (push) {
            logger.info("Step " + step + "/" + totalSteps + ": Pushing to remote");
            if (pushTags) {
                if (!gitService.pushWithTags()) {
                    logger.error("Failed to push with tags");
                }
            } else {
                if (!gitService.push()) {
                    logger.error("Failed to push");
                }
            }
            step++;
        }
        
        logger.success("=== Workflow Completed Successfully ===");
        
        // Show summary
        showSummary(version, updateProjectVersion, bumpToSnapshot, nextSnapshot, archive, delete, createTag, push, pushTags);
    }
    
    private int calculateTotalSteps(boolean updateVersion, boolean bumpSnapshot, boolean handleFragments, 
                                     boolean createTag, boolean push) {
        int steps = 3; // Generate, stage, commit
        if (updateVersion) steps++;
        if (bumpSnapshot) steps++;
        if (handleFragments) steps++;
        if (createTag) steps++;
        if (push) steps++;
        return steps;
    }
    
    private void showSummary(String version, boolean updateProjectVersion, boolean bumpToSnapshot,
                             String nextSnapshot, boolean archive, boolean delete, 
                             boolean createTag, boolean push, boolean pushTags) {
        StringBuilder summary = new StringBuilder();
        summary.append("CHANGELOG.md generated successfully!\n\n");
        
        if (!version.isEmpty()) {
            summary.append("✓ Version: ").append(version).append("\n");
        }
        
        if (updateProjectVersion) {
            summary.append("✓ Project version updated\n");
        }
        
        if (archive) {
            summary.append("✓ Fragments archived to: .changes/v").append(version).append("/\n");
        } else if (delete) {
            summary.append("✓ Fragments deleted\n");
        }
        
        if (createTag) {
            summary.append("✓ Tag created: v").append(version).append("\n");
        }
        
        if (bumpToSnapshot) {
            summary.append("✓ Bumped to: ").append(nextSnapshot).append("\n");
        }
        
        if (push) {
            summary.append("✓ Pushed to remote");
            if (pushTags) {
                summary.append(" (with tags)");
            }
            summary.append("\n");
        }
        
        Messages.showInfoMessage(project, summary.toString(), "Success");
    }
    
    @Override
    protected Action[] createActions() {
        return new Action[]{getOKAction(), getCancelAction()};
    }
    
}
