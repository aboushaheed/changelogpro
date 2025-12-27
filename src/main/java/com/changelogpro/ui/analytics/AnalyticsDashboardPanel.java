package com.changelogpro.ui.analytics;

import com.changelogpro.analytics.AnalyticsAggregator;
import com.changelogpro.analytics.model.AnalyticsData;
import com.changelogpro.services.ChangeLogProjectService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CompletableFuture;

/**
 * Main analytics dashboard panel.
 * Contains overview cards and sub-tabs for detailed analytics.
 * 
 * <p>Structure:
 * <pre>
 * ┌─────────────────────────────────────┐
 * │ Overview Cards (KPIs)               │
 * ├─────────────────────────────────────┤
 * │ [Timeline][Changes][Git][Team][Health][Trends]
 * │ ┌───────────────────────────────────┐
 * │ │ Sub-panel content                 │
 * │ └───────────────────────────────────┘
 * └─────────────────────────────────────┘
 * </pre>
 */
public class AnalyticsDashboardPanel extends JPanel {
    
    private final Project project;
    private final ChangeLogProjectService projectService;
    private final AnalyticsAggregator aggregator;
    
    // UI Components
    private JPanel overviewPanel;
    private JBTabbedPane subTabs;
    private JPanel loadingPanel;
    private JPanel contentPanel;
    private JButton exportButton;
    
    // Overview cards (row 1)
    private KpiCard releasesCard;
    private KpiCard commitsCard;
    private KpiCard cycleCard;
    private KpiCard versionCard;
    
    // Overview cards (row 2)
    private KpiCard contributorsCard;
    private KpiCard conventionalCard;
    private KpiCard healthCard;
    private KpiCard breakingCard;
    
    // Sub-panels
    private TimelinePanel timelinePanel;
    private ChangesPanel changesPanel;
    private GitInsightsPanel gitInsightsPanel;
    private ContributorsPanel contributorsPanel;
    private HealthPanel healthPanel;
    private TrendsPanel trendsPanel;
    private ComparisonPanel comparisonPanel;
    
    // Data
    private AnalyticsData currentData;
    private boolean isLoading = false;
    
    public AnalyticsDashboardPanel(@NotNull Project project) {
        super(new BorderLayout());
        this.project = project;
        this.projectService = ChangeLogProjectService.getInstance(project);
        this.aggregator = new AnalyticsAggregator(project);
        
        initUI();
        loadDataAsync(false);
    }
    
    private void initUI() {
        // Main container with padding
        setBorder(JBUI.Borders.empty(10));
        
        // Create loading panel
        loadingPanel = createLoadingPanel();
        
        // Create content panel
        contentPanel = new JPanel(new BorderLayout(0, 15));
        
        // Overview section
        overviewPanel = createOverviewPanel();
        contentPanel.add(overviewPanel, BorderLayout.NORTH);
        
        // Sub-tabs section
        subTabs = createSubTabs();
        contentPanel.add(subTabs, BorderLayout.CENTER);
        
        // Start with loading panel
        add(loadingPanel, BorderLayout.CENTER);
    }
    
    private JPanel createLoadingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        
        JPanel centerBox = new JPanel();
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
        
        // Animated loading icon
        JLabel iconLabel = new JLabel(AllIcons.Process.Big.Step_1);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerBox.add(iconLabel);
        
        centerBox.add(Box.createVerticalStrut(10));
        
        JLabel textLabel = new JLabel("Loading analytics data...");
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textLabel.setForeground(JBColor.GRAY);
        centerBox.add(textLabel);
        
        centerBox.add(Box.createVerticalStrut(5));
        
        JLabel subLabel = new JLabel("Analyzing Git history and changelog entries");
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subLabel.setForeground(JBColor.GRAY);
        subLabel.setFont(subLabel.getFont().deriveFont(10f));
        centerBox.add(subLabel);
        
        panel.add(centerBox);
        return panel;
    }
    
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        
        // Title with buttons
        JPanel headerPanel = new JPanel(new BorderLayout());
        JBLabel titleLabel = new JBLabel("📊 Analytics Overview");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Button panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        // Export button
        exportButton = new JButton("Export HTML");
        exportButton.setIcon(AllIcons.ToolbarDecorator.Export);
        exportButton.setToolTipText("Export analytics as HTML report");
        exportButton.addActionListener(e -> exportAsHtml());
        exportButton.setEnabled(false);
        buttonsPanel.add(exportButton);
        
        // Git Import button
        JButton importButton = new JButton("Import Git");
        importButton.setIcon(AllIcons.Vcs.Vendors.Github);
        importButton.setToolTipText("Import changelog entries from Git history");
        importButton.addActionListener(e -> openGitImport());
        buttonsPanel.add(importButton);
        
        // Refresh button
        JButton refreshButton = new JButton(AllIcons.Actions.Refresh);
        refreshButton.setToolTipText("Refresh analytics data (force reload)");
        refreshButton.setBorderPainted(false);
        refreshButton.setContentAreaFilled(false);
        refreshButton.addActionListener(e -> loadDataAsync(true));
        buttonsPanel.add(refreshButton);
        
        headerPanel.add(buttonsPanel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // KPI Cards in 2 rows
        JPanel cardsContainer = new JPanel(new GridLayout(2, 4, 10, 10));
        
        // Row 1: Core metrics
        releasesCard = new KpiCard("Releases", "0", AllIcons.Vcs.History, "Total releases published");
        commitsCard = new KpiCard("Commits", "0", AllIcons.Vcs.CommitNode, "Total commits analyzed");
        cycleCard = new KpiCard("Avg Cycle", "-- days", AllIcons.Actions.Lightning, "Average days between releases");
        versionCard = new KpiCard("Version", "--", AllIcons.Nodes.Tag, "Current version");
        
        cardsContainer.add(releasesCard);
        cardsContainer.add(commitsCard);
        cardsContainer.add(cycleCard);
        cardsContainer.add(versionCard);
        
        // Row 2: Quality metrics
        contributorsCard = new KpiCard("Contributors", "0", AllIcons.General.User, "Active contributors");
        conventionalCard = new KpiCard("Conventional", "0%", AllIcons.Actions.Checked, "Conventional commits adoption");
        healthCard = new KpiCard("Health", "--", AllIcons.General.InspectionsOK, "Project health score");
        breakingCard = new KpiCard("Breaking", "0", AllIcons.General.Warning, "Breaking changes count");
        
        cardsContainer.add(contributorsCard);
        cardsContainer.add(conventionalCard);
        cardsContainer.add(healthCard);
        cardsContainer.add(breakingCard);
        
        panel.add(cardsContainer, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JBTabbedPane createSubTabs() {
        JBTabbedPane tabs = new JBTabbedPane();
        
        // Create sub-panels
        timelinePanel = new TimelinePanel(project);
        changesPanel = new ChangesPanel(project);
        gitInsightsPanel = new GitInsightsPanel(project);
        contributorsPanel = new ContributorsPanel(project);
        healthPanel = new HealthPanel(project);
        trendsPanel = new TrendsPanel(project);
        comparisonPanel = new ComparisonPanel(project);
        
        // Add tabs with icons and tooltips
        tabs.addTab("Timeline", AllIcons.Vcs.Branch, timelinePanel, "Release timeline and history");
        tabs.addTab("Changes", AllIcons.Actions.GroupByPrefix, changesPanel, "Changes distribution by type");
        tabs.addTab("Git Insights", AllIcons.Vcs.Vendors.Github, gitInsightsPanel, "Git commit analysis");
        tabs.addTab("Contributors", AllIcons.General.User, contributorsPanel, "Team activity and heatmap");
        tabs.addTab("Health", AllIcons.General.InspectionsOK, healthPanel, "Project health score");
        tabs.addTab("Trends", AllIcons.Actions.Lightning, trendsPanel, "Trends and predictions");
        tabs.addTab("Compare", AllIcons.Actions.Diff, comparisonPanel, "Compare releases");
        
        return tabs;
    }
    
    /**
     * Loads analytics data asynchronously.
     * 
     * @param forceRefresh If true, bypasses cache
     */
    public void loadDataAsync(boolean forceRefresh) {
        if (isLoading) return;
        isLoading = true;
        
        // Show loading state
        showLoading(true);
        exportButton.setEnabled(false);
        
        CompletableFuture.supplyAsync(() -> {
            return aggregator.collectAnalytics(forceRefresh);
        }).thenAccept(data -> {
            ApplicationManager.getApplication().invokeLater(() -> {
                updateUI(data);
                showLoading(false);
                isLoading = false;
                exportButton.setEnabled(true);
            });
        }).exceptionally(ex -> {
            ApplicationManager.getApplication().invokeLater(() -> {
                projectService.getLogService().error("Failed to load analytics: " + ex.getMessage());
                showLoading(false);
                isLoading = false;
            });
            return null;
        });
    }
    
    /**
     * Legacy method for compatibility.
     */
    public void loadDataAsync() {
        loadDataAsync(false);
    }
    
    private void showLoading(boolean loading) {
        removeAll();
        if (loading) {
            add(loadingPanel, BorderLayout.CENTER);
        } else {
            add(contentPanel, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }
    
    /**
     * Updates all UI components with new data.
     */
    private void updateUI(@NotNull AnalyticsData data) {
        this.currentData = data;
        
        // Update row 1 cards
        releasesCard.setValue(String.valueOf(data.getReleaseCount()));
        commitsCard.setValue(String.valueOf(data.getTotalCommitCount()));
        
        double avgCycle = data.getAverageReleaseCycleDays();
        cycleCard.setValue(avgCycle > 0 ? String.format("%.0f days", avgCycle) : "-- days");
        
        String version = data.getCurrentVersion();
        versionCard.setValue(version != null ? version : "--");
        
        // Update row 2 cards
        contributorsCard.setValue(String.valueOf(data.getActiveContributorCount()));
        conventionalCard.setValue(String.format("%.0f%%", data.getConventionalCommitPercentage()));
        
        if (data.getHealthScore() != null) {
            healthCard.setValue(data.getHealthScore().getGrade());
            healthCard.setValueColor(getGradeColor(data.getHealthScore().getGrade()));
        } else {
            healthCard.setValue("--");
        }
        
        int breakingCount = data.getBreakingChangeCount();
        breakingCard.setValue(String.valueOf(breakingCount));
        if (breakingCount > 0) {
            breakingCard.setValueColor(new JBColor(new Color(0xbf8700), new Color(0xd29922)));
        }
        
        // Update sub-panels
        timelinePanel.updateData(data);
        changesPanel.updateData(data);
        gitInsightsPanel.updateData(data);
        contributorsPanel.updateData(data);
        healthPanel.updateData(data);
        trendsPanel.updateData(data);
        comparisonPanel.updateData(data);
    }
    
    private Color getGradeColor(String grade) {
        return switch (grade) {
            case "A+", "A" -> new JBColor(new Color(0x1a7f37), new Color(0x3fb950));
            case "B+", "B" -> new JBColor(new Color(0x0969da), new Color(0x58a6ff));
            case "C+", "C" -> new JBColor(new Color(0xbf8700), new Color(0xd29922));
            default -> new JBColor(new Color(0xcf222e), new Color(0xf85149));
        };
    }
    
    /**
     * Opens the Git Import dialog.
     */
    private void openGitImport() {
        if (!projectService.isInitialized()) {
            JOptionPane.showMessageDialog(this,
                "Please initialize the project first.\nGo to Actions tab and click 'Initialize Project'.",
                "Project Not Initialized",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        GitImportDialog dialog = new GitImportDialog(project);
        if (dialog.showAndGet()) {
            // Refresh after import
            loadDataAsync(true);
        }
    }
    
    /**
     * Opens the HTML export dialog.
     */
    private void exportAsHtml() {
        if (currentData == null) {
            JOptionPane.showMessageDialog(this,
                "No data available to export. Please wait for data to load.",
                "No Data",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        ExportHtmlDialog dialog = new ExportHtmlDialog(project, currentData);
        dialog.show();
    }
    
    /**
     * Returns the current analytics data.
     */
    @Nullable
    public AnalyticsData getCurrentData() {
        return currentData;
    }
    
    /**
     * KPI Card component for the overview section.
     */
    private static class KpiCard extends JPanel {
        private final JBLabel valueLabel;
        
        public KpiCard(String title, String value, Icon icon, String tooltip) {
            super(new BorderLayout(5, 5));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.border(), 1),
                JBUI.Borders.empty(10)
            ));
            setBackground(JBColor.background());
            setToolTipText(tooltip);
            
            // Icon and title at top
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            topPanel.setOpaque(false);
            topPanel.add(new JBLabel(icon));
            JBLabel titleLabel = new JBLabel(title);
            titleLabel.setForeground(JBColor.GRAY);
            titleLabel.setFont(titleLabel.getFont().deriveFont(10f));
            topPanel.add(titleLabel);
            add(topPanel, BorderLayout.NORTH);
            
            // Value at center
            valueLabel = new JBLabel(value);
            valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 20f));
            valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(valueLabel, BorderLayout.CENTER);
        }
        
        public void setValue(String value) {
            valueLabel.setText(value);
        }
        
        public void setValueColor(Color color) {
            valueLabel.setForeground(color);
        }
    }
}
