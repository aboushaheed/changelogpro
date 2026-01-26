package com.changelogpro.ui.analytics;

import com.changelogpro.analytics.model.AnalyticsData;
import com.changelogpro.analytics.model.ContributorStats;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Panel displaying contributor statistics and activity.
 * Shows leaderboard, GitHub-style activity heatmap, and team metrics.
 */
public class ContributorsPanel extends JPanel {
    
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMM");
    private static final int WEEKS_TO_SHOW = 52; // Full year like GitHub
    private static final int BOX_SIZE = 12;
    private static final int BOX_SPACING = 3;
    
    private final Project project;
    
    // UI Components
    private JPanel leaderboardPanel;
    private HeatmapPanel heatmapPanel;
    private JPanel teamStatsPanel;
    private JBLabel heatmapTooltip;
    
    // Data
    private List<ContributorStats> contributors = new ArrayList<>();
    private Map<LocalDate, Integer> aggregatedActivity = new HashMap<>();
    private int totalCommits = 0;
    private int busFactor = 0;
    
    public ContributorsPanel(@NotNull Project project) {
        super(new BorderLayout(0, 15));
        this.project = project;
        setBorder(JBUI.Borders.empty(10));
        
        initUI();
    }
    
    private void initUI() {
        // Header
        JBLabel titleLabel = new JBLabel("Contributors");
        titleLabel.setIcon(AllIcons.General.User);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        add(titleLabel, BorderLayout.NORTH);
        
        // Main content split
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.4);
        splitPane.setBorder(null);
        splitPane.setDividerSize(5);
        
        // Left: Leaderboard
        leaderboardPanel = createLeaderboardPanel();
        splitPane.setLeftComponent(leaderboardPanel);
        
        // Right: Heatmap and stats
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        
        // Heatmap section
        JPanel heatmapSection = createHeatmapSection();
        rightPanel.add(heatmapSection, BorderLayout.CENTER);
        
        // Team stats at bottom
        teamStatsPanel = createTeamStatsPanel();
        rightPanel.add(teamStatsPanel, BorderLayout.SOUTH);
        
        splitPane.setRightComponent(rightPanel);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel createLeaderboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Leaderboard"
        ));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10));
        
        // Placeholder
        content.add(new JBLabel("Loading..."));
        
        JBScrollPane scrollPane = new JBScrollPane(content);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createHeatmapSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Activity (Last 12 Months)"
        ));
        
        // Heatmap canvas
        heatmapPanel = new HeatmapPanel();
        panel.add(heatmapPanel, BorderLayout.CENTER);
        
        // Tooltip label
        heatmapTooltip = new JBLabel(" ");
        heatmapTooltip.setFont(heatmapTooltip.getFont().deriveFont(11f));
        heatmapTooltip.setForeground(JBColor.GRAY);
        heatmapTooltip.setBorder(JBUI.Borders.empty(5, 10));
        panel.add(heatmapTooltip, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * GitHub-style contribution heatmap panel.
     */
    private class HeatmapPanel extends JPanel {
        private static final int LEFT_PADDING = 35; // For day labels
        private static final int TOP_PADDING = 20;  // For month labels
        
        public HeatmapPanel() {
            setPreferredSize(new Dimension(
                LEFT_PADDING + (WEEKS_TO_SHOW * (BOX_SIZE + BOX_SPACING)) + 50,
                TOP_PADDING + (7 * (BOX_SIZE + BOX_SPACING)) + 30
            ));
            
            // Mouse motion for tooltips
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    updateTooltip(e.getX(), e.getY());
                }
            });
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    heatmapTooltip.setText(" ");
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (aggregatedActivity.isEmpty()) {
                g2.setColor(JBColor.GRAY);
                g2.setFont(g2.getFont().deriveFont(12f));
                g2.drawString("No activity data available", 50, 80);
                return;
            }
            
            // Calculate date range
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusWeeks(WEEKS_TO_SHOW - 1).with(DayOfWeek.MONDAY);
            
            // Find max value for color scaling
            int maxActivity = aggregatedActivity.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(1);
            
            // Draw day labels (Mon, Wed, Fri)
            g2.setColor(JBColor.GRAY);
            g2.setFont(g2.getFont().deriveFont(9f));
            String[] dayLabels = {"Mon", "", "Wed", "", "Fri", "", ""};
            for (int d = 0; d < 7; d++) {
                if (!dayLabels[d].isEmpty()) {
                    int y = TOP_PADDING + d * (BOX_SIZE + BOX_SPACING) + BOX_SIZE - 2;
                    g2.drawString(dayLabels[d], 5, y);
                }
            }
            
            // Draw month labels and grid
            String currentMonth = "";
            LocalDate date = startDate;
            
            for (int week = 0; week < WEEKS_TO_SHOW; week++) {
                int x = LEFT_PADDING + week * (BOX_SIZE + BOX_SPACING);
                
                // Month label (only when month changes)
                String month = date.format(MONTH_FORMAT);
                if (!month.equals(currentMonth)) {
                    g2.setColor(JBColor.GRAY);
                    g2.setFont(g2.getFont().deriveFont(9f));
                    g2.drawString(month, x, TOP_PADDING - 5);
                    currentMonth = month;
                }
                
                // Draw boxes for each day
                for (int day = 0; day < 7; day++) {
                    LocalDate cellDate = date.plusDays(day);
                    if (cellDate.isAfter(today)) continue;
                    
                    int y = TOP_PADDING + day * (BOX_SIZE + BOX_SPACING);
                    int activity = aggregatedActivity.getOrDefault(cellDate, 0);
                    double intensity = maxActivity > 0 ? (double) activity / maxActivity : 0;
                    
                    // Draw box
                    g2.setColor(getHeatmapColor(intensity));
                    g2.fillRoundRect(x, y, BOX_SIZE, BOX_SIZE, 2, 2);
                    
                    // Border
                    g2.setColor(new JBColor(new Color(0, 0, 0, 20), new Color(255, 255, 255, 20)));
                    g2.drawRoundRect(x, y, BOX_SIZE, BOX_SIZE, 2, 2);
                }
                
                date = date.plusWeeks(1);
            }
            
            // Draw legend
            drawLegend(g2);
        }
        
        private void drawLegend(Graphics2D g2) {
            int legendX = LEFT_PADDING + (WEEKS_TO_SHOW - 8) * (BOX_SIZE + BOX_SPACING);
            int legendY = TOP_PADDING + 7 * (BOX_SIZE + BOX_SPACING) + 10;
            
            g2.setColor(JBColor.GRAY);
            g2.setFont(g2.getFont().deriveFont(10f));
            g2.drawString("Less", legendX, legendY + BOX_SIZE - 2);
            
            legendX += 30;
            double[] intensities = {0, 0.25, 0.5, 0.75, 1.0};
            for (double intensity : intensities) {
                g2.setColor(getHeatmapColor(intensity));
                g2.fillRoundRect(legendX, legendY, BOX_SIZE, BOX_SIZE, 2, 2);
                legendX += BOX_SIZE + 2;
            }
            
            g2.setColor(JBColor.GRAY);
            g2.drawString("More", legendX + 5, legendY + BOX_SIZE - 2);
        }
        
        private void updateTooltip(int mouseX, int mouseY) {
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusWeeks(WEEKS_TO_SHOW - 1).with(DayOfWeek.MONDAY);
            
            // Calculate which cell was hovered
            int week = (mouseX - LEFT_PADDING) / (BOX_SIZE + BOX_SPACING);
            int day = (mouseY - TOP_PADDING) / (BOX_SIZE + BOX_SPACING);
            
            if (week >= 0 && week < WEEKS_TO_SHOW && day >= 0 && day < 7) {
                LocalDate cellDate = startDate.plusWeeks(week).plusDays(day);
                if (!cellDate.isAfter(today)) {
                    int activity = aggregatedActivity.getOrDefault(cellDate, 0);
                    String dateStr = cellDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
                    String commits = activity == 1 ? "1 commit" : activity + " commits";
                    heatmapTooltip.setText(commits + " on " + dateStr);
                    return;
                }
            }
            heatmapTooltip.setText(" ");
        }
    }
    
    private Color getHeatmapColor(double intensity) {
        if (intensity <= 0) {
            return new JBColor(new Color(0xebedf0), new Color(0x161b22));
        } else if (intensity < 0.25) {
            return new JBColor(new Color(0x9be9a8), new Color(0x0e4429));
        } else if (intensity < 0.5) {
            return new JBColor(new Color(0x40c463), new Color(0x006d32));
        } else if (intensity < 0.75) {
            return new JBColor(new Color(0x30a14e), new Color(0x26a641));
        } else {
            return new JBColor(new Color(0x216e39), new Color(0x39d353));
        }
    }
    
    private JPanel createTeamStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Team Stats"
        ));
        
        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        content.setBorder(JBUI.Borders.empty(5));
        
        // Placeholder - will be populated
        content.add(new JBLabel("Loading..."));
        
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * Updates the panel with new analytics data.
     */
    public void updateData(@NotNull AnalyticsData data) {
        this.contributors = new ArrayList<>(data.getContributors());
        this.totalCommits = data.getTotalCommitCount();
        this.busFactor = data.getBusFactor();
        
        // Aggregate activity from all contributors
        aggregatedActivity.clear();
        for (ContributorStats contributor : contributors) {
            for (Map.Entry<LocalDate, Integer> entry : contributor.getActivityByDate().entrySet()) {
                aggregatedActivity.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }
        
        updateLeaderboard();
        updateTeamStats(data);
        heatmapPanel.repaint();
    }
    
    private void updateLeaderboard() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10));
        
        if (contributors.isEmpty()) {
            JBLabel emptyLabel = new JBLabel("No contributors found");
            emptyLabel.setForeground(JBColor.GRAY);
            content.add(emptyLabel);
        } else {
            int rank = 1;
            for (ContributorStats contributor : contributors) {
                if (rank > 15) break; // Top 15
                
                JPanel row = createContributorRow(rank, contributor);
                content.add(row);
                content.add(Box.createVerticalStrut(8));
                rank++;
            }
        }
        
        content.add(Box.createVerticalGlue());
        
        // Replace panel content
        leaderboardPanel.removeAll();
        leaderboardPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Leaderboard (" + contributors.size() + " contributors)"
        ));
        
        JBScrollPane scrollPane = new JBScrollPane(content);
        scrollPane.setBorder(null);
        leaderboardPanel.add(scrollPane, BorderLayout.CENTER);
        leaderboardPanel.revalidate();
        leaderboardPanel.repaint();
    }
    
    private JPanel createContributorRow(int rank, ContributorStats contributor) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        
        // Rank badge
        String rankEmoji = switch (rank) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> String.format("%2d.", rank);
        };
        JBLabel rankLabel = new JBLabel(rankEmoji);
        rankLabel.setPreferredSize(new Dimension(30, 20));
        rankLabel.setFont(rankLabel.getFont().deriveFont(rank <= 3 ? 14f : 11f));
        row.add(rankLabel, BorderLayout.WEST);
        
        // Name and stats
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        
        JBLabel nameLabel = new JBLabel(contributor.getName());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));
        infoPanel.add(nameLabel, BorderLayout.NORTH);
        
        // Detailed stats
        String primaryType = contributor.getPrimaryConventionalType();
        String typeInfo = primaryType != null ? " | Primary: " + primaryType : "";
        String stats = String.format("%d commits%s", 
            contributor.getCommitCount(), typeInfo);
        JBLabel statsLabel = new JBLabel(stats);
        statsLabel.setFont(statsLabel.getFont().deriveFont(10f));
        statsLabel.setForeground(JBColor.GRAY);
        infoPanel.add(statsLabel, BorderLayout.SOUTH);
        
        row.add(infoPanel, BorderLayout.CENTER);
        
        // Progress bar (percentage of total commits)
        double percentage = totalCommits > 0 ? (contributor.getCommitCount() * 100.0 / totalCommits) : 0;
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int) percentage);
        progressBar.setString(String.format("%.0f%%", percentage));
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(70, 16));
        progressBar.setFont(progressBar.getFont().deriveFont(9f));
        
        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
        progressPanel.setOpaque(false);
        progressPanel.add(progressBar);
        row.add(progressPanel, BorderLayout.EAST);
        
        return row;
    }
    
    private void updateTeamStats(@NotNull AnalyticsData data) {
        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        content.setBorder(JBUI.Borders.empty(5));
        
        // Active contributors
        JPanel stat1 = createStatBadge("Contributors", String.valueOf(contributors.size()), AllIcons.General.User);
        content.add(stat1);
        
        // Bus factor
        String busFDesc = busFactor <= 1 ? "⚠️ Risk" : busFactor <= 2 ? "Fair" : "Good";
        JPanel stat2 = createStatBadge("Bus Factor", busFactor + " (" + busFDesc + ")", AllIcons.General.Warning);
        content.add(stat2);
        
        // Total commits
        JPanel stat3 = createStatBadge("Total Commits", String.valueOf(totalCommits), AllIcons.Vcs.CommitNode);
        content.add(stat3);
        
        // Average commits per contributor
        double avgCommits = contributors.isEmpty() ? 0 : 
            (double) totalCommits / contributors.size();
        JPanel stat4 = createStatBadge("Avg/Person", String.format("%.1f", avgCommits), AllIcons.Actions.GroupByMethod);
        content.add(stat4);
        
        // Conventional commits adoption
        double ccAdoption = data.getConventionalCommitPercentage();
        JPanel stat5 = createStatBadge("CC Adoption", String.format("%.0f%%", ccAdoption), AllIcons.Actions.Checked);
        content.add(stat5);
        
        // Replace panel content
        teamStatsPanel.removeAll();
        teamStatsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Team Stats"
        ));
        teamStatsPanel.add(content, BorderLayout.CENTER);
        teamStatsPanel.revalidate();
        teamStatsPanel.repaint();
    }
    
    private JPanel createStatBadge(String label, String value, Icon icon) {
        JPanel badge = new JPanel(new BorderLayout(5, 2));
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            JBUI.Borders.empty(6, 10)
        ));
        badge.setBackground(JBColor.background());
        
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        topRow.setOpaque(false);
        topRow.add(new JBLabel(icon));
        JBLabel valueLabel = new JBLabel(value);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 13f));
        topRow.add(valueLabel);
        badge.add(topRow, BorderLayout.CENTER);
        
        JBLabel labelComponent = new JBLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(9f));
        labelComponent.setForeground(JBColor.GRAY);
        badge.add(labelComponent, BorderLayout.SOUTH);
        
        return badge;
    }
}
