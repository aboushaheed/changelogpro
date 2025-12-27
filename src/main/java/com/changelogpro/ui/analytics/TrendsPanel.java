package com.changelogpro.ui.analytics;

import com.changelogpro.analytics.model.AnalyticsData;
import com.changelogpro.analytics.model.CommitInfo;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel displaying trend analysis and predictions.
 * Includes:
 * - Release velocity trends
 * - Commit patterns (day of week, time of day)
 * - Change type trends over time
 * - Next release prediction
 * - Technical debt indicators
 */
public class TrendsPanel extends JPanel {
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d");
    
    private final Project project;
    
    // UI Components
    private JPanel velocityPanel;
    private JPanel patternsPanel;
    private JPanel predictionPanel;
    private JPanel debtPanel;
    
    // Data
    private List<ReleaseInfo> releases = new ArrayList<>();
    private List<CommitInfo> commits = new ArrayList<>();
    private Map<String, Integer> commitsByMonth = new LinkedHashMap<>();
    
    public TrendsPanel(@NotNull Project project) {
        super(new BorderLayout(0, 15));
        this.project = project;
        setBorder(JBUI.Borders.empty(10));
        
        initUI();
    }
    
    private void initUI() {
        // Header
        JBLabel titleLabel = new JBLabel("Trends & Predictions");
        titleLabel.setIcon(AllIcons.Actions.Lightning);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        add(titleLabel, BorderLayout.NORTH);
        
        // Main content in grid
        JPanel contentPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        velocityPanel = createVelocityPanel();
        contentPanel.add(velocityPanel);
        
        patternsPanel = createPatternsPanel();
        contentPanel.add(patternsPanel);
        
        predictionPanel = createPredictionPanel();
        contentPanel.add(predictionPanel);
        
        debtPanel = createDebtPanel();
        contentPanel.add(debtPanel);
        
        JBScrollPane scrollPane = new JBScrollPane(contentPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createVelocityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "📈 Release Velocity"
        ));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10));
        content.add(new JBLabel("Loading..."));
        
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createPatternsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "⏰ Commit Patterns"
        ));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10));
        content.add(new JBLabel("Loading..."));
        
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createPredictionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "🔮 Next Release Prediction"
        ));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10));
        content.add(new JBLabel("Loading..."));
        
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createDebtPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "⚠️ Technical Debt Indicators"
        ));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10));
        content.add(new JBLabel("Loading..."));
        
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * Updates the panel with new analytics data.
     */
    public void updateData(@NotNull AnalyticsData data) {
        this.releases = new ArrayList<>(data.getReleases());
        this.commits = new ArrayList<>(data.getRecentCommits());
        this.commitsByMonth = new LinkedHashMap<>(data.getCommitsByMonth());
        
        updateVelocityPanel(data);
        updatePatternsPanel();
        updatePredictionPanel(data);
        updateDebtPanel(data);
    }
    
    private void updateVelocityPanel(AnalyticsData data) {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10));
        
        // Calculate velocity metrics
        double avgCycle = data.getAverageReleaseCycleDays();
        int releaseCount = data.getReleaseCount();
        
        // Get recent release intervals
        List<Long> intervals = new ArrayList<>();
        List<ReleaseInfo> sortedReleases = releases.stream()
            .filter(r -> !r.isUnreleased() && r.getReleaseDate() != null)
            .sorted(Comparator.comparing(ReleaseInfo::getReleaseDate).reversed())
            .limit(10)
            .collect(Collectors.toList());
        
        for (int i = 0; i < sortedReleases.size() - 1; i++) {
            LocalDate d1 = sortedReleases.get(i).getReleaseDate();
            LocalDate d2 = sortedReleases.get(i + 1).getReleaseDate();
            intervals.add(ChronoUnit.DAYS.between(d2, d1));
        }
        
        // Average cycle
        content.add(createMetricRow("Average Release Cycle", 
            avgCycle > 0 ? String.format("%.0f days", avgCycle) : "N/A"));
        content.add(Box.createVerticalStrut(8));
        
        // Trend analysis
        if (intervals.size() >= 3) {
            double recentAvg = intervals.stream().limit(3).mapToLong(l -> l).average().orElse(0);
            double olderAvg = intervals.stream().skip(3).mapToLong(l -> l).average().orElse(recentAvg);
            
            String trend;
            Color trendColor;
            if (recentAvg < olderAvg * 0.9) {
                trend = "↗ Accelerating (faster releases)";
                trendColor = new JBColor(new Color(0x1a7f37), new Color(0x3fb950));
            } else if (recentAvg > olderAvg * 1.1) {
                trend = "↘ Slowing down";
                trendColor = new JBColor(new Color(0xbf8700), new Color(0xd29922));
            } else {
                trend = "→ Stable";
                trendColor = JBColor.foreground();
            }
            
            JBLabel trendLabel = new JBLabel(trend);
            trendLabel.setForeground(trendColor);
            trendLabel.setFont(trendLabel.getFont().deriveFont(Font.BOLD));
            content.add(trendLabel);
            content.add(Box.createVerticalStrut(8));
        }
        
        // Release frequency
        if (!sortedReleases.isEmpty() && sortedReleases.get(sortedReleases.size() - 1).getReleaseDate() != null) {
            LocalDate firstRelease = sortedReleases.get(sortedReleases.size() - 1).getReleaseDate();
            long totalDays = ChronoUnit.DAYS.between(firstRelease, LocalDate.now());
            double releasesPerMonth = totalDays > 0 ? (releaseCount * 30.0 / totalDays) : 0;
            
            content.add(createMetricRow("Releases/Month", String.format("%.1f", releasesPerMonth)));
        }
        
        content.add(Box.createVerticalGlue());
        
        velocityPanel.removeAll();
        velocityPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "📈 Release Velocity"
        ));
        velocityPanel.add(content, BorderLayout.CENTER);
        velocityPanel.revalidate();
        velocityPanel.repaint();
    }
    
    private void updatePatternsPanel() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10));
        
        if (commits.isEmpty()) {
            content.add(new JBLabel("No commit data available"));
        } else {
            // Day of week analysis
            Map<DayOfWeek, Integer> commitsByDay = new EnumMap<>(DayOfWeek.class);
            Map<Integer, Integer> commitsByHour = new TreeMap<>();
            
            for (CommitInfo commit : commits) {
                LocalDateTime dt = commit.getDateTime();
                commitsByDay.merge(dt.getDayOfWeek(), 1, Integer::sum);
                commitsByHour.merge(dt.getHour(), 1, Integer::sum);
            }
            
            // Most productive day
            DayOfWeek bestDay = commitsByDay.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(DayOfWeek.MONDAY);
            
            content.add(createMetricRow("Most Productive Day", bestDay.toString()));
            content.add(Box.createVerticalStrut(8));
            
            // Peak hours
            int peakHour = commitsByHour.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(10);
            
            String peakTime = String.format("%02d:00 - %02d:00", peakHour, (peakHour + 1) % 24);
            content.add(createMetricRow("Peak Commit Hour", peakTime));
            content.add(Box.createVerticalStrut(8));
            
            // Weekend vs Weekday
            int weekdayCommits = commitsByDay.entrySet().stream()
                .filter(e -> e.getKey() != DayOfWeek.SATURDAY && e.getKey() != DayOfWeek.SUNDAY)
                .mapToInt(Map.Entry::getValue)
                .sum();
            int weekendCommits = commits.size() - weekdayCommits;
            double weekendPct = commits.size() > 0 ? (weekendCommits * 100.0 / commits.size()) : 0;
            
            content.add(createMetricRow("Weekend Commits", String.format("%.0f%%", weekendPct)));
            
            // Day distribution visual
            content.add(Box.createVerticalStrut(10));
            JPanel dayChart = createDayChart(commitsByDay);
            content.add(dayChart);
        }
        
        content.add(Box.createVerticalGlue());
        
        patternsPanel.removeAll();
        patternsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "⏰ Commit Patterns"
        ));
        patternsPanel.add(content, BorderLayout.CENTER);
        patternsPanel.revalidate();
        patternsPanel.repaint();
    }
    
    private JPanel createDayChart(Map<DayOfWeek, Integer> commitsByDay) {
        JPanel chart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int maxVal = commitsByDay.values().stream().mapToInt(i -> i).max().orElse(1);
                String[] days = {"M", "T", "W", "T", "F", "S", "S"};
                int barWidth = 20;
                int spacing = 5;
                int maxHeight = 40;
                int x = 10;
                
                for (int i = 0; i < 7; i++) {
                    DayOfWeek day = DayOfWeek.of(i + 1);
                    int count = commitsByDay.getOrDefault(day, 0);
                    int barHeight = maxVal > 0 ? (count * maxHeight / maxVal) : 0;
                    
                    // Bar
                    boolean isWeekend = (i >= 5);
                    g2.setColor(isWeekend ? 
                        new JBColor(new Color(0xbf8700), new Color(0xd29922)) :
                        new JBColor(new Color(0x0969da), new Color(0x58a6ff)));
                    g2.fillRoundRect(x, maxHeight - barHeight + 5, barWidth, barHeight, 3, 3);
                    
                    // Label
                    g2.setColor(JBColor.GRAY);
                    g2.setFont(g2.getFont().deriveFont(9f));
                    g2.drawString(days[i], x + 6, maxHeight + 18);
                    
                    x += barWidth + spacing;
                }
            }
        };
        chart.setPreferredSize(new Dimension(200, 70));
        chart.setMaximumSize(new Dimension(200, 70));
        return chart;
    }
    
    private void updatePredictionPanel(AnalyticsData data) {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10));
        
        double avgCycle = data.getAverageReleaseCycleDays();
        int unreleasedCount = data.getUnreleasedEntryCount();
        
        // Last release date
        LocalDate lastReleaseDate = releases.stream()
            .filter(r -> !r.isUnreleased() && r.getReleaseDate() != null)
            .map(ReleaseInfo::getReleaseDate)
            .max(Comparator.naturalOrder())
            .orElse(null);
        
        if (lastReleaseDate != null && avgCycle > 0) {
            long daysSinceRelease = ChronoUnit.DAYS.between(lastReleaseDate, LocalDate.now());
            LocalDate predictedDate = lastReleaseDate.plusDays((long) avgCycle);
            long daysUntilPredicted = ChronoUnit.DAYS.between(LocalDate.now(), predictedDate);
            
            content.add(createMetricRow("Last Release", lastReleaseDate.format(DATE_FORMAT)));
            content.add(Box.createVerticalStrut(5));
            content.add(createMetricRow("Days Since", String.valueOf(daysSinceRelease)));
            content.add(Box.createVerticalStrut(10));
            
            // Prediction
            JBLabel predictionLabel = new JBLabel("Predicted Next Release:");
            predictionLabel.setFont(predictionLabel.getFont().deriveFont(Font.BOLD));
            content.add(predictionLabel);
            
            String predictionText;
            Color predictionColor;
            if (daysUntilPredicted < 0) {
                predictionText = String.format("Overdue by %d days!", Math.abs(daysUntilPredicted));
                predictionColor = new JBColor(new Color(0xcf222e), new Color(0xf85149));
            } else if (daysUntilPredicted < 7) {
                predictionText = String.format("~%s (%d days)", predictedDate.format(DATE_FORMAT), daysUntilPredicted);
                predictionColor = new JBColor(new Color(0xbf8700), new Color(0xd29922));
            } else {
                predictionText = String.format("~%s (%d days)", predictedDate.format(DATE_FORMAT), daysUntilPredicted);
                predictionColor = JBColor.foreground();
            }
            
            JBLabel dateLabel = new JBLabel(predictionText);
            dateLabel.setForeground(predictionColor);
            dateLabel.setFont(dateLabel.getFont().deriveFont(12f));
            content.add(dateLabel);
            content.add(Box.createVerticalStrut(10));
            
            // Readiness indicator
            double readiness = Math.min(100, (daysSinceRelease / avgCycle) * 100);
            content.add(createMetricRow("Release Readiness", String.format("%.0f%%", readiness)));
            
            JProgressBar readinessBar = new JProgressBar(0, 100);
            readinessBar.setValue((int) readiness);
            readinessBar.setStringPainted(false);
            readinessBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
            content.add(readinessBar);
        } else {
            content.add(new JBLabel("Not enough data for prediction"));
        }
        
        content.add(Box.createVerticalStrut(10));
        content.add(createMetricRow("Unreleased Changes", String.valueOf(unreleasedCount)));
        
        content.add(Box.createVerticalGlue());
        
        predictionPanel.removeAll();
        predictionPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "🔮 Next Release Prediction"
        ));
        predictionPanel.add(content, BorderLayout.CENTER);
        predictionPanel.revalidate();
        predictionPanel.repaint();
    }
    
    private void updateDebtPanel(AnalyticsData data) {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10));
        
        List<DebtIndicator> indicators = new ArrayList<>();
        
        // Undocumented commits
        int undocumented = data.getUndocumentedCommitCount();
        int totalCommits = data.getTotalCommitCount();
        if (totalCommits > 0) {
            double undocPct = (undocumented * 100.0 / totalCommits);
            String severity = undocPct > 50 ? "HIGH" : undocPct > 25 ? "MEDIUM" : "LOW";
            indicators.add(new DebtIndicator(
                "Undocumented Commits",
                String.format("%d (%.0f%%)", undocumented, undocPct),
                severity
            ));
        }
        
        // Non-conventional commits
        double ccPct = data.getConventionalCommitPercentage();
        if (ccPct < 50) {
            indicators.add(new DebtIndicator(
                "Non-Conventional Commits",
                String.format("%.0f%% adoption", ccPct),
                ccPct < 25 ? "HIGH" : "MEDIUM"
            ));
        }
        
        // Breaking changes without major version
        int breakingCount = data.getBreakingChangeCount();
        if (breakingCount > 3) {
            indicators.add(new DebtIndicator(
                "Breaking Changes",
                String.valueOf(breakingCount) + " changes",
                "MEDIUM"
            ));
        }
        
        // Bus factor
        int busFactor = data.getBusFactor();
        if (busFactor <= 1) {
            indicators.add(new DebtIndicator(
                "Low Bus Factor",
                "Only " + busFactor + " key contributor(s)",
                "HIGH"
            ));
        }
        
        // Deprecated features not removed
        long deprecatedCount = releases.stream()
            .flatMap(r -> r.getEntryCounts().entrySet().stream())
            .filter(e -> e.getKey() == ChangeType.DEPRECATED)
            .mapToInt(Map.Entry::getValue)
            .sum();
        if (deprecatedCount > 5) {
            indicators.add(new DebtIndicator(
                "Deprecated Features",
                deprecatedCount + " pending removal",
                "LOW"
            ));
        }
        
        if (indicators.isEmpty()) {
            JBLabel noDebt = new JBLabel("✅ No significant debt indicators");
            noDebt.setForeground(new JBColor(new Color(0x1a7f37), new Color(0x3fb950)));
            content.add(noDebt);
        } else {
            for (DebtIndicator indicator : indicators) {
                JPanel row = new JPanel(new BorderLayout(5, 0));
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
                
                // Severity badge
                JBLabel severityBadge = new JBLabel(indicator.severity);
                severityBadge.setFont(severityBadge.getFont().deriveFont(9f));
                severityBadge.setForeground(getSeverityColor(indicator.severity));
                severityBadge.setPreferredSize(new Dimension(50, 15));
                row.add(severityBadge, BorderLayout.WEST);
                
                // Name
                JBLabel nameLabel = new JBLabel(indicator.name);
                nameLabel.setFont(nameLabel.getFont().deriveFont(11f));
                row.add(nameLabel, BorderLayout.CENTER);
                
                // Value
                JBLabel valueLabel = new JBLabel(indicator.value);
                valueLabel.setFont(valueLabel.getFont().deriveFont(10f));
                valueLabel.setForeground(JBColor.GRAY);
                row.add(valueLabel, BorderLayout.EAST);
                
                content.add(row);
                content.add(Box.createVerticalStrut(5));
            }
        }
        
        content.add(Box.createVerticalGlue());
        
        debtPanel.removeAll();
        debtPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "⚠️ Technical Debt Indicators"
        ));
        debtPanel.add(content, BorderLayout.CENTER);
        debtPanel.revalidate();
        debtPanel.repaint();
    }
    
    private Color getSeverityColor(String severity) {
        return switch (severity) {
            case "HIGH" -> new JBColor(new Color(0xcf222e), new Color(0xf85149));
            case "MEDIUM" -> new JBColor(new Color(0xbf8700), new Color(0xd29922));
            default -> new JBColor(new Color(0x0969da), new Color(0x58a6ff));
        };
    }
    
    private JPanel createMetricRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        
        JBLabel labelComponent = new JBLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(11f));
        row.add(labelComponent, BorderLayout.WEST);
        
        JBLabel valueComponent = new JBLabel(value);
        valueComponent.setFont(valueComponent.getFont().deriveFont(Font.BOLD, 11f));
        row.add(valueComponent, BorderLayout.EAST);
        
        return row;
    }
    
    private static class DebtIndicator {
        final String name;
        final String value;
        final String severity;
        
        DebtIndicator(String name, String value, String severity) {
            this.name = name;
            this.value = value;
            this.severity = severity;
        }
    }
}
