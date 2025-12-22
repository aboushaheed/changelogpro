package com.changelogpro.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Comprehensive documentation dialog for ChangeLog Pro.
 */
public class DocumentationDialog extends DialogWrapper {
    
    public DocumentationDialog(@NotNull Project project) {
        super(project, true);
        setTitle("ChangeLog Pro - Documentation");
        setSize(800, 600);
        init();
    }
    
    @Override
    protected @Nullable JComponent createCenterPanel() {
        JBTabbedPane tabbedPane = new JBTabbedPane();
        
        tabbedPane.addTab("Overview", AllIcons.General.Information, createOverviewPanel());
        tabbedPane.addTab("Why Changelog?", AllIcons.Actions.Help, createWhyChangelogPanel());
        tabbedPane.addTab("Getting Started", AllIcons.Actions.Execute, createGettingStartedPanel());
        tabbedPane.addTab("Change Types", AllIcons.Nodes.Tag, createChangeTypesPanel());
        tabbedPane.addTab("Best Practices", AllIcons.General.InspectionsOK, createBestPracticesPanel());
        tabbedPane.addTab("Supported Platforms", AllIcons.Nodes.Plugin, createPlatformsPanel());
        
        return tabbedPane;
    }
    
    private JComponent createOverviewPanel() {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style=\"font-family: sans-serif; padding: 20px; line-height: 1.6;\">");
        html.append("<h1 style=\"color: #2196F3;\">ChangeLog Pro</h1>");
        html.append("<p style=\"font-size: 14px; color: #666;\">Professional Changelog Management for IntelliJ IDEA</p>");
        html.append("<p style=\"font-size: 12px;\">Developed by <b>Abdelmoula SOUIDI</b></p>");
        html.append("<h2>What is ChangeLog Pro?</h2>");
        html.append("<p>ChangeLog Pro is a powerful IntelliJ IDEA plugin that helps you maintain professional, ");
        html.append("well-structured changelogs following the <b>Keep a Changelog</b> standard and ");
        html.append("<b>Semantic Versioning</b> principles.</p>");
        html.append("<h2>Key Features</h2>");
        html.append("<ul>");
        html.append("<li><b>Universal Git Support</b> - Works with GitHub, GitLab, Bitbucket, Azure DevOps, Gitea, Gogs, and any Git repository</li>");
        html.append("<li><b>Multi-Step Wizard</b> - Guided changelog entry creation with validation</li>");
        html.append("<li><b>Issue Tracker Integration</b> - JIRA, GitHub Issues, GitLab Issues, YouTrack, and more</li>");
        html.append("<li><b>Auto-Detection</b> - Automatically detects your Git provider and issue tracker</li>");
        html.append("<li><b>Keep a Changelog Format</b> - Follows the industry-standard changelog format</li>");
        html.append("<li><b>Git Integration</b> - Automatic staging of changelog files</li>");
        html.append("</ul>");
        html.append("<h2>How It Works</h2>");
        html.append("<p>ChangeLog Pro uses a fragment-based approach to changelog management:</p>");
        html.append("<ol>");
        html.append("<li>Each change is stored as a separate YAML file in <code>.changes/unreleased/</code></li>");
        html.append("<li>Files are organized by change type (added, changed, fixed, etc.)</li>");
        html.append("<li>During release, fragments are compiled into CHANGELOG.md</li>");
        html.append("<li>This prevents merge conflicts and enables parallel development</li>");
        html.append("</ol>");
        html.append("<h2>Version</h2>");
        html.append("<p>ChangeLog Pro v1.0.0</p>");
        html.append("<p>Compatible with IntelliJ IDEA 2022.1 - 2025.3</p>");
        html.append("</body></html>");
        return createHtmlPanel(html.toString());
    }
    
    private JComponent createWhyChangelogPanel() {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style=\"font-family: sans-serif; padding: 20px; line-height: 1.6;\">");
        html.append("<h1 style=\"color: #4CAF50;\">Why Keep a Changelog?</h1>");
        html.append("<h2>What is a Changelog?</h2>");
        html.append("<p>A changelog is a file which contains a curated, chronologically ordered list of notable ");
        html.append("changes for each version of a project.</p>");
        html.append("<h2>Why Should You Keep One?</h2>");
        html.append("<p>To make it easier for users and contributors to see precisely what notable changes ");
        html.append("have been made between each release (or version) of the project.</p>");
        html.append("<h2>Who Needs a Changelog?</h2>");
        html.append("<p>People do. Whether consumers or developers, the end users of software are human beings ");
        html.append("who care about what's in the software. When the software changes, people want to know ");
        html.append("why and how.</p>");
        html.append("<h2>Benefits of a Good Changelog</h2>");
        html.append("<h3>For Users</h3>");
        html.append("<ul>");
        html.append("<li>Understand what has changed between versions</li>");
        html.append("<li>Know if an upgrade is safe or contains breaking changes</li>");
        html.append("<li>See if bugs they reported have been fixed</li>");
        html.append("<li>Discover new features they can use</li>");
        html.append("</ul>");
        html.append("<h3>For Developers</h3>");
        html.append("<ul>");
        html.append("<li>Document changes as they happen (not at release time)</li>");
        html.append("<li>Avoid merge conflicts with fragment-based approach</li>");
        html.append("<li>Track what needs to be communicated to users</li>");
        html.append("<li>Maintain a professional project image</li>");
        html.append("</ul>");
        html.append("<h2>The Problem with Git Logs</h2>");
        html.append("<p>Git commit logs are <b>not</b> changelogs. They are:</p>");
        html.append("<ul>");
        html.append("<li>Too detailed and technical</li>");
        html.append("<li>Full of noise (typo fixes, merge commits, etc.)</li>");
        html.append("<li>Not curated for end users</li>");
        html.append("<li>Missing context about why changes matter</li>");
        html.append("</ul>");
        html.append("<p style=\"margin-top: 20px; padding: 15px; background: #E3F2FD; border-radius: 5px;\">");
        html.append("<b>Remember:</b> A changelog is for humans, not machines.</p>");
        html.append("</body></html>");
        return createHtmlPanel(html.toString());
    }
    
    private JComponent createGettingStartedPanel() {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style=\"font-family: sans-serif; padding: 20px; line-height: 1.6;\">");
        html.append("<h1 style=\"color: #FF9800;\">Getting Started</h1>");
        html.append("<h2>Step 1: Initialize Your Project</h2>");
        html.append("<ol>");
        html.append("<li>Open the ChangeLog Pro panel from the right sidebar</li>");
        html.append("<li>Click <b>\"Initialize Project\"</b></li>");
        html.append("<li>Configure your repository URL (auto-detected from Git)</li>");
        html.append("<li>Select your Git provider (GitHub, GitLab, etc.)</li>");
        html.append("<li>Configure your issue tracker (JIRA, GitHub Issues, etc.)</li>");
        html.append("<li>Click OK to create the directory structure</li>");
        html.append("</ol>");
        html.append("<h2>Step 2: Create Changelog Entries</h2>");
        html.append("<p>You have two options:</p>");
        html.append("<h3>Option A: Use the Wizard</h3>");
        html.append("<ol>");
        html.append("<li>Click <b>\"New Entry (Wizard)\"</b></li>");
        html.append("<li>Select the change type</li>");
        html.append("<li>Enter description, issue ID, and PR number</li>");
        html.append("<li>Review and confirm</li>");
        html.append("</ol>");
        html.append("<h3>Option B: Quick Add</h3>");
        html.append("<ol>");
        html.append("<li>Click the appropriate Quick Add button</li>");
        html.append("<li>Fill in the minimal required information</li>");
        html.append("<li>Click OK</li>");
        html.append("</ol>");
        html.append("<h2>Directory Structure</h2>");
        html.append("<pre style=\"background: #f5f5f5; padding: 10px; border-radius: 5px;\">");
        html.append(".changes/\n");
        html.append("  unreleased/\n");
        html.append("    added/\n");
        html.append("    changed/\n");
        html.append("    deprecated/\n");
        html.append("    removed/\n");
        html.append("    fixed/\n");
        html.append("    security/\n");
        html.append("</pre>");
        html.append("<h2>Keyboard Shortcuts</h2>");
        html.append("<table border=\"1\" cellpadding=\"8\" style=\"border-collapse: collapse;\">");
        html.append("<tr style=\"background: #f5f5f5;\"><th>Action</th><th>Shortcut</th></tr>");
        html.append("<tr><td>New Changelog Entry</td><td><code>Ctrl+Alt+L</code></td></tr>");
        html.append("</table>");
        html.append("</body></html>");
        return createHtmlPanel(html.toString());
    }
    
    private JComponent createChangeTypesPanel() {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style=\"font-family: sans-serif; padding: 20px; line-height: 1.6;\">");
        html.append("<h1 style=\"color: #9C27B0;\">Change Types</h1>");
        html.append("<p>ChangeLog Pro follows the <b>Keep a Changelog</b> standard, which defines six types of changes:</p>");
        html.append("<h2 style=\"color: #4CAF50;\">Added</h2>");
        html.append("<p>For new features.</p>");
        html.append("<p><b>Examples:</b> Added user authentication, Added dark mode support</p>");
        html.append("<h2 style=\"color: #2196F3;\">Changed</h2>");
        html.append("<p>For changes in existing functionality.</p>");
        html.append("<p><b>Examples:</b> Changed default timeout, Improved search performance</p>");
        html.append("<h2 style=\"color: #FF9800;\">Deprecated</h2>");
        html.append("<p>For soon-to-be removed features.</p>");
        html.append("<p><b>Examples:</b> Deprecated legacy API endpoints</p>");
        html.append("<h2 style=\"color: #F44336;\">Removed</h2>");
        html.append("<p>For now removed features.</p>");
        html.append("<p><b>Examples:</b> Removed support for Python 2.7</p>");
        html.append("<h2 style=\"color: #9C27B0;\">Fixed</h2>");
        html.append("<p>For any bug fixes.</p>");
        html.append("<p><b>Examples:</b> Fixed crash when opening large files</p>");
        html.append("<h2 style=\"color: #E91E63;\">Security</h2>");
        html.append("<p>For vulnerability fixes.</p>");
        html.append("<p><b>Examples:</b> Fixed SQL injection vulnerability</p>");
        html.append("</body></html>");
        return createHtmlPanel(html.toString());
    }
    
    private JComponent createBestPracticesPanel() {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style=\"font-family: sans-serif; padding: 20px; line-height: 1.6;\">");
        html.append("<h1 style=\"color: #00BCD4;\">Best Practices</h1>");
        html.append("<h2>Guiding Principles</h2>");
        html.append("<ul>");
        html.append("<li><b>Changelogs are for humans</b>, not machines</li>");
        html.append("<li>There should be an entry for <b>every single version</b></li>");
        html.append("<li>The same types of changes should be <b>grouped</b></li>");
        html.append("<li>Versions and sections should be <b>linkable</b></li>");
        html.append("<li>The <b>latest version</b> comes first</li>");
        html.append("</ul>");
        html.append("<h2>Writing Good Entries</h2>");
        html.append("<h3>Do:</h3>");
        html.append("<ul>");
        html.append("<li>Write in <b>plain language</b> that users understand</li>");
        html.append("<li>Start with a <b>verb</b> (Added, Fixed, Changed, etc.)</li>");
        html.append("<li>Be <b>specific</b> about what changed</li>");
        html.append("<li>Include <b>issue/PR references</b> for traceability</li>");
        html.append("</ul>");
        html.append("<h3>Don't:</h3>");
        html.append("<ul>");
        html.append("<li>Use technical jargon unnecessarily</li>");
        html.append("<li>Include every tiny commit</li>");
        html.append("<li>Write vague entries like \"Bug fixes\"</li>");
        html.append("</ul>");
        html.append("<h2>Semantic Versioning</h2>");
        html.append("<ul>");
        html.append("<li><b>MAJOR</b> (X.0.0) - Breaking changes</li>");
        html.append("<li><b>MINOR</b> (0.X.0) - New features (backward compatible)</li>");
        html.append("<li><b>PATCH</b> (0.0.X) - Bug fixes (backward compatible)</li>");
        html.append("</ul>");
        html.append("</body></html>");
        return createHtmlPanel(html.toString());
    }
    
    private JComponent createPlatformsPanel() {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style=\"font-family: sans-serif; padding: 20px; line-height: 1.6;\">");
        html.append("<h1 style=\"color: #673AB7;\">Supported Platforms</h1>");
        html.append("<h2>Git Providers</h2>");
        html.append("<table border=\"1\" cellpadding=\"10\" style=\"border-collapse: collapse; width: 100%;\">");
        html.append("<tr style=\"background: #f5f5f5;\"><th>Provider</th><th>PR/MR Term</th><th>Auto-Detection</th></tr>");
        html.append("<tr><td><b>GitHub</b></td><td>Pull Request (PR)</td><td>github.com</td></tr>");
        html.append("<tr><td><b>GitLab</b></td><td>Merge Request (MR)</td><td>gitlab.com</td></tr>");
        html.append("<tr><td><b>Bitbucket</b></td><td>Pull Request (PR)</td><td>bitbucket.org</td></tr>");
        html.append("<tr><td><b>Azure DevOps</b></td><td>Pull Request (PR)</td><td>dev.azure.com</td></tr>");
        html.append("<tr><td><b>Gitea</b></td><td>Pull Request (PR)</td><td>gitea.*</td></tr>");
        html.append("<tr><td><b>Gogs</b></td><td>Pull Request (PR)</td><td>gogs.*</td></tr>");
        html.append("</table>");
        html.append("<h2>Issue Trackers</h2>");
        html.append("<table border=\"1\" cellpadding=\"10\" style=\"border-collapse: collapse; width: 100%;\">");
        html.append("<tr style=\"background: #f5f5f5;\"><th>Tracker</th><th>ID Format</th></tr>");
        html.append("<tr><td><b>JIRA</b></td><td>PROJECT-123</td></tr>");
        html.append("<tr><td><b>GitHub Issues</b></td><td>#123</td></tr>");
        html.append("<tr><td><b>GitLab Issues</b></td><td>#123</td></tr>");
        html.append("<tr><td><b>YouTrack</b></td><td>PROJECT-123</td></tr>");
        html.append("<tr><td><b>Linear</b></td><td>ABC-123</td></tr>");
        html.append("<tr><td><b>Azure Boards</b></td><td>123</td></tr>");
        html.append("</table>");
        html.append("<h2>IntelliJ IDEA Compatibility</h2>");
        html.append("<p>ChangeLog Pro is compatible with:</p>");
        html.append("<ul>");
        html.append("<li>IntelliJ IDEA Community Edition 2022.1+</li>");
        html.append("<li>IntelliJ IDEA Ultimate 2022.1+</li>");
        html.append("<li>All versions up to 2025.3</li>");
        html.append("</ul>");
        html.append("</body></html>");
        return createHtmlPanel(html.toString());
    }
    
    private JComponent createHtmlPanel(String html) {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setText(html);
        editorPane.setEditable(false);
        editorPane.setCaretPosition(0);
        
        editorPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(new URI(e.getURL().toString()));
                } catch (IOException | URISyntaxException ex) {
                    // Ignore
                }
            }
        });
        
        JBScrollPane scrollPane = new JBScrollPane(editorPane);
        scrollPane.setBorder(JBUI.Borders.empty());
        return scrollPane;
    }
    
    @Override
    protected Action[] createActions() {
        return new Action[]{getOKAction()};
    }
}
