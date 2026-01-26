package com.changelogpro.ui.analytics;

import com.changelogpro.analytics.model.AnalyticsData;
import com.changelogpro.analytics.model.ReleaseInfo;
import com.changelogpro.config.ChangeType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Panel displaying the distribution of changes by type.
 * Shows a bar chart and breakdown of change types.
 */
public class ChangesPanel extends JPanel {
    
    private final Project project;
    private JPanel chartPanel;
    private JPanel breakdownPanel;
    private JPanel breakingChangesPanel;
    private Map<ChangeType, Double> currentDistribution;
    private int totalEntries;
    
    public ChangesPanel(@NotNull Project project) {
        super(new BorderLayout(0, 15));
        this.project = project;
        setBorder(JBUI.Borders.empty(10));
        
        initUI();
    }
    
    private void initUI() {
        // Header
        JBLabel titleLabel = new JBLabel("Changes Distribution");
        titleLabel.setIcon(AllIcons.Actions.GroupByPrefix);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        add(titleLabel, BorderLayout.NORTH);
        
        // Main content split
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        
        // Left: Bar chart
        chartPanel = createChartPanel();
        contentPanel.add(chartPanel);
        
        // Right: Breakdown and trends
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        
        breakdownPanel = createBreakdownPanel();
        rightPanel.add(breakdownPanel, BorderLayout.NORTH);
        
        breakingChangesPanel = createBreakingChangesPanel();
        rightPanel.add(breakingChangesPanel, BorderLayout.CENTER);
        
        contentPanel.add(rightPanel);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createChartPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "By Type"
        ));
        
        // Chart will be rendered here
        JPanel chart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (currentDistribution == null || currentDistribution.isEmpty()) {
                    drawEmptyState(g);
                } else {
                    drawBarChart(g);
                }
            }
        };
        chart.setPreferredSize(new Dimension(250, 200));
        panel.add(chart, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void drawEmptyState(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        String text = "No data available";
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = getHeight() / 2;
        
        g2.setColor(JBColor.GRAY);
        g2.drawString(text, x, y);
    }
    
    private void drawBarChart(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int padding = 20;
        int barHeight = 25;
        int spacing = 10;
        int maxBarWidth = chartPanel.getWidth() - padding * 4 - 80;
        int y = padding;
        
        for (ChangeType type : ChangeType.values()) {
            double percentage = currentDistribution.getOrDefault(type, 0.0);
            int barWidth = (int) (maxBarWidth * percentage / 100.0);
            
            // Type label
            g2.setColor(JBColor.foreground());
            g2.setFont(g2.getFont().deriveFont(11f));
            g2.drawString(type.getDisplayName(), padding, y + barHeight / 2 + 4);
            
            // Bar background
            int barX = padding + 80;
            g2.setColor(JBColor.border());
            g2.fillRect(barX, y, maxBarWidth, barHeight);
            
            // Bar fill
            g2.setColor(getTypeColor(type));
            g2.fillRect(barX, y, barWidth, barHeight);
            
            // Percentage label
            String pctStr = String.format("%.0f%%", percentage);
            g2.setColor(JBColor.foreground());
            g2.drawString(pctStr, barX + maxBarWidth + 5, y + barHeight / 2 + 4);
            
            y += barHeight + spacing;
        }
    }
    
    private JPanel createBreakdownPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Breakdown"
        ));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10));
        
        // Will be populated with data
        content.add(new JBLabel("Loading..."));
        
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createBreakingChangesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Breaking Changes"
        ));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10));
        
        panel.add(new JBScrollPane(content), BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * Updates the panel with new analytics data.
     */
    public void updateData(@NotNull AnalyticsData data) {
        this.currentDistribution = data.getChangeTypeDistribution();
        this.totalEntries = data.getTotalEntryCount();
        
        updateBreakdownPanel(data);
        updateBreakingChangesPanel(data);
        
        chartPanel.repaint();
    }
    
    private void updateBreakdownPanel(@NotNull AnalyticsData data) {
        // Find the content panel inside breakdown
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10));
        
        Map<ChangeType, Integer> entriesByType = data.getTotalEntriesByType();
        
        // Total
        JPanel totalRow = createStatRow("Total entries", String.valueOf(totalEntries));
        content.add(totalRow);
        content.add(Box.createVerticalStrut(10));
        
        // By type
        for (ChangeType type : ChangeType.values()) {
            int count = entriesByType.getOrDefault(type, 0);
            if (count > 0) {
                JPanel row = createStatRow(type.getDisplayName(), String.valueOf(count));
                row.setForeground(getTypeColor(type));
                content.add(row);
                content.add(Box.createVerticalStrut(5));
            }
        }
        
        // Replace content
        breakdownPanel.removeAll();
        breakdownPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Breakdown"
        ));
        breakdownPanel.add(content, BorderLayout.CENTER);
        breakdownPanel.revalidate();
        breakdownPanel.repaint();
    }
    
    private void updateBreakingChangesPanel(@NotNull AnalyticsData data) {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10));
        
        // Find breaking changes
        List<ReleaseInfo> releasesWithBreaking = data.getReleases().stream()
            .filter(ReleaseInfo::hasBreakingChanges)
            .limit(5)
            .toList();
        
        if (releasesWithBreaking.isEmpty()) {
            JBLabel noBreaking = new JBLabel("No breaking changes detected");
            noBreaking.setForeground(JBColor.GRAY);
            noBreaking.setIcon(AllIcons.General.InspectionsOK);
            content.add(noBreaking);
        } else {
            int totalBreaking = data.getBreakingChangeCount();
            JBLabel countLabel = new JBLabel(totalBreaking + " breaking change(s) found");
            countLabel.setIcon(AllIcons.General.Warning);
            countLabel.setForeground(new JBColor(new Color(0xbf8700), new Color(0xd29922)));
            content.add(countLabel);
            content.add(Box.createVerticalStrut(10));
            
            for (ReleaseInfo release : releasesWithBreaking) {
                for (String bc : release.getBreakingChanges()) {
                    JPanel bcRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                    bcRow.setOpaque(false);
                    
                    JBLabel vLabel = new JBLabel("v" + release.getVersion() + ":");
                    vLabel.setForeground(JBColor.GRAY);
                    vLabel.setFont(vLabel.getFont().deriveFont(11f));
                    bcRow.add(vLabel);
                    
                    String truncated = bc.length() > 50 ? bc.substring(0, 47) + "..." : bc;
                    JBLabel descLabel = new JBLabel(truncated);
                    descLabel.setFont(descLabel.getFont().deriveFont(11f));
                    bcRow.add(descLabel);
                    
                    content.add(bcRow);
                    content.add(Box.createVerticalStrut(5));
                }
            }
        }
        
        // Replace content
        breakingChangesPanel.removeAll();
        breakingChangesPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Breaking Changes"
        ));
        breakingChangesPanel.add(new JBScrollPane(content), BorderLayout.CENTER);
        breakingChangesPanel.revalidate();
        breakingChangesPanel.repaint();
    }
    
    private JPanel createStatRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        JBLabel labelComponent = new JBLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(12f));
        row.add(labelComponent, BorderLayout.WEST);
        
        JBLabel valueComponent = new JBLabel(value);
        valueComponent.setFont(valueComponent.getFont().deriveFont(Font.BOLD, 12f));
        row.add(valueComponent, BorderLayout.EAST);
        
        return row;
    }
    
    private Color getTypeColor(ChangeType type) {
        return switch (type) {
            case ADDED -> new JBColor(new Color(0x1a7f37), new Color(0x3fb950));
            case CHANGED -> new JBColor(new Color(0x0969da), new Color(0x58a6ff));
            case DEPRECATED -> new JBColor(new Color(0xbf8700), new Color(0xd29922));
            case REMOVED -> new JBColor(new Color(0xcf222e), new Color(0xf85149));
            case FIXED -> new JBColor(new Color(0x8250df), new Color(0xa371f7));
            case SECURITY -> new JBColor(new Color(0xcf222e), new Color(0xf85149));
        };
    }
}
