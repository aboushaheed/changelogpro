package com.changelogpro.ui.analytics;

import com.changelogpro.analytics.model.AnalyticsData;
import com.changelogpro.analytics.model.CommitInfo;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel displaying comprehensive Git repository insights.
 * Shows commit activity, conventional commits adoption, coverage metrics, and detailed commit analysis.
 */
public class GitInsightsPanel extends JPanel {
    
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d, HH:mm");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMM");
    
    private final Project project;
    
    // UI Components
    private JPanel activityChartPanel;
    private JPanel metricsPanel;
    private JPanel conventionalBreakdownPanel;
    private JBTable recentCommitsTable;
    private CommitsTableModel tableModel;
    private JBLabel summaryLabel;
    
    // Data
    private Map<String, Integer> commitsByMonth = new LinkedHashMap<>();
    private Map<String, Integer> conventionalTypeCount = new LinkedHashMap<>();
    private List<CommitInfo> recentCommits = new ArrayList<>();
    private int totalCommits = 0;
    private int conventionalCount = 0;
    
    public GitInsightsPanel(@NotNull Project project) {
        super(new BorderLayout(0, 10));
        this.project = project;
        setBorder(JBUI.Borders.empty(10));
        
        initUI();
    }
    
    private void initUI() {
        // Header with summary
        JPanel headerPanel = new JPanel(new BorderLayout());
        JBLabel titleLabel = new JBLabel("Git Insights");
        titleLabel.setIcon(AllIcons.Vcs.Vendors.Github);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        summaryLabel = new JBLabel("");
        summaryLabel.setForeground(JBColor.GRAY);
        headerPanel.add(summaryLabel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        
        // Top row: Charts and metrics
        JPanel topPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        
        activityChartPanel = createActivityChartPanel();
        topPanel.add(activityChartPanel);
        
        metricsPanel = createMetricsPanel();
        topPanel.add(metricsPanel);
        
        conventionalBreakdownPanel = createConventionalBreakdownPanel();
        topPanel.add(conventionalBreakdownPanel);
        
        contentPanel.add(topPanel, BorderLayout.NORTH);
        
        // Bottom: Recent commits table
        JPanel commitsPanel = createCommitsPanel();
        contentPanel.add(commitsPanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createActivityChartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Monthly Activity"
        ));
        
        // Chart canvas
        JPanel chart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawActivityChart(g);
            }
        };
        chart.setPreferredSize(new Dimension(250, 150));
        panel.add(chart, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void drawActivityChart(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (commitsByMonth.isEmpty()) {
            g2.setColor(JBColor.GRAY);
            g2.drawString("No Git data available", 20, 50);
            return;
        }
        
        int padding = 35;
        int chartWidth = activityChartPanel.getWidth() - padding * 2 - 30;
        int chartHeight = 110;
        
        // Take last 6 months
        List<String> months = new ArrayList<>(commitsByMonth.keySet());
        if (months.size() > 6) {
            months = months.subList(months.size() - 6, months.size());
        }
        
        int barWidth = Math.max(25, (chartWidth / Math.max(1, months.size())) - 8);
        
        // Find max value
        int maxValue = months.stream()
            .mapToInt(m -> commitsByMonth.getOrDefault(m, 0))
            .max().orElse(1);
        
        int x = padding;
        int baseY = chartHeight + 15;
        
        for (String month : months) {
            int value = commitsByMonth.getOrDefault(month, 0);
            int barHeight = maxValue > 0 ? (int) ((value * chartHeight) / (double) maxValue) : 0;
            
            // Bar with gradient
            GradientPaint gradient = new GradientPaint(
                x, baseY - barHeight, new JBColor(new Color(0x58a6ff), new Color(0x388bfd)),
                x, baseY, new JBColor(new Color(0x0969da), new Color(0x1f6feb))
            );
            g2.setPaint(gradient);
            g2.fillRoundRect(x, baseY - barHeight, barWidth, barHeight, 4, 4);
            
            // Month label
            g2.setColor(JBColor.GRAY);
            g2.setFont(g2.getFont().deriveFont(9f));
            String monthLabel = month.length() >= 7 ? month.substring(5) : month;
            g2.drawString(monthLabel, x + barWidth / 4, baseY + 12);
            
            // Value on top
            if (barHeight > 15) {
                g2.setColor(JBColor.WHITE);
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 10f));
                String valueStr = String.valueOf(value);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(valueStr, x + (barWidth - fm.stringWidth(valueStr)) / 2, baseY - barHeight + 12);
            }
            
            x += barWidth + 8;
        }
    }
    
    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Key Metrics"
        ));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10));
        
        content.add(new JBLabel("Loading..."));
        
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createConventionalBreakdownPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Conventional Commits"
        ));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10));
        
        content.add(new JBLabel("Loading..."));
        
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createCommitsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Recent Commits"
        ));
        
        tableModel = new CommitsTableModel();
        recentCommitsTable = new JBTable(tableModel);
        recentCommitsTable.setRowHeight(30);
        recentCommitsTable.setShowGrid(false);
        recentCommitsTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Custom renderer for commit hash column
        recentCommitsTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
                if (!isSelected) {
                    setForeground(new JBColor(new Color(0x0969da), new Color(0x58a6ff)));
                }
                return this;
            }
        });
        
        // Type column with colors
        recentCommitsTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected && value != null && !value.toString().equals("-")) {
                    setForeground(getTypeColor(value.toString()));
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                return this;
            }
        });
        
        // Breaking change indicator
        recentCommitsTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if ("⚠️".equals(value)) {
                    setForeground(new JBColor(new Color(0xbf8700), new Color(0xd29922)));
                }
                return this;
            }
        });
        
        // Column widths
        recentCommitsTable.getColumnModel().getColumn(0).setPreferredWidth(70);   // Hash
        recentCommitsTable.getColumnModel().getColumn(1).setPreferredWidth(70);   // Type
        recentCommitsTable.getColumnModel().getColumn(2).setPreferredWidth(350);  // Subject
        recentCommitsTable.getColumnModel().getColumn(3).setPreferredWidth(30);   // Breaking
        recentCommitsTable.getColumnModel().getColumn(4).setPreferredWidth(50);   // Issue
        recentCommitsTable.getColumnModel().getColumn(5).setPreferredWidth(100);  // Author
        recentCommitsTable.getColumnModel().getColumn(6).setPreferredWidth(100);  // Date
        
        JBScrollPane scrollPane = new JBScrollPane(recentCommitsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private Color getTypeColor(String type) {
        return switch (type.toLowerCase()) {
            case "feat" -> new JBColor(new Color(0x1a7f37), new Color(0x3fb950));
            case "fix" -> new JBColor(new Color(0x8250df), new Color(0xa371f7));
            case "docs" -> new JBColor(new Color(0x0969da), new Color(0x58a6ff));
            case "perf" -> new JBColor(new Color(0xbf8700), new Color(0xd29922));
            case "refactor" -> new JBColor(new Color(0x0969da), new Color(0x58a6ff));
            case "test" -> new JBColor(new Color(0x6e7781), new Color(0x8b949e));
            case "build", "ci" -> new JBColor(new Color(0x6e7781), new Color(0x8b949e));
            case "chore" -> new JBColor(new Color(0x6e7781), new Color(0x8b949e));
            case "security" -> new JBColor(new Color(0xcf222e), new Color(0xf85149));
            default -> JBColor.foreground();
        };
    }
    
    /**
     * Updates the panel with new analytics data.
     */
    public void updateData(@NotNull AnalyticsData data) {
        this.commitsByMonth = new LinkedHashMap<>(data.getCommitsByMonth());
        this.recentCommits = new ArrayList<>(data.getRecentCommits());
        this.totalCommits = data.getTotalCommitCount();
        this.conventionalCount = data.getConventionalCommitCount();
        
        // Count conventional types
        conventionalTypeCount.clear();
        for (CommitInfo commit : recentCommits) {
            if (commit.getConventionalType() != null) {
                conventionalTypeCount.merge(commit.getConventionalType(), 1, Integer::sum);
            }
        }
        
        updateSummaryLabel(data);
        updateMetricsPanel(data);
        updateConventionalBreakdownPanel();
        tableModel.setCommits(recentCommits);
        
        activityChartPanel.repaint();
    }
    
    private void updateSummaryLabel(AnalyticsData data) {
        String summary = String.format("%d commits | %.0f%% conventional | %.0f%% documented",
            totalCommits,
            data.getConventionalCommitPercentage(),
            data.getDocumentationCoverage()
        );
        summaryLabel.setText(summary);
    }
    
    private void updateMetricsPanel(@NotNull AnalyticsData data) {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10));
        
        // Total commits with icon
        content.add(createMetricRow(AllIcons.Vcs.CommitNode, "Total commits", String.valueOf(totalCommits)));
        content.add(Box.createVerticalStrut(8));
        
        // Conventional commits with progress
        double ccPct = data.getConventionalCommitPercentage();
        content.add(createMetricRow(AllIcons.Actions.Checked, "Conventional", String.format("%d (%.0f%%)", conventionalCount, ccPct)));
        JProgressBar ccProgress = new JProgressBar(0, 100);
        ccProgress.setValue((int) ccPct);
        ccProgress.setStringPainted(false);
        ccProgress.setPreferredSize(new Dimension(150, 6));
        ccProgress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        content.add(ccProgress);
        content.add(Box.createVerticalStrut(8));
        
        // Documentation coverage
        double docCoverage = data.getDocumentationCoverage();
        content.add(createMetricRow(AllIcons.Actions.Edit, "Doc coverage", String.format("%.0f%%", docCoverage)));
        JProgressBar docProgress = new JProgressBar(0, 100);
        docProgress.setValue((int) docCoverage);
        docProgress.setStringPainted(false);
        docProgress.setPreferredSize(new Dimension(150, 6));
        docProgress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        content.add(docProgress);
        content.add(Box.createVerticalStrut(8));
        
        // Undocumented commits
        int undocumented = data.getUndocumentedCommitCount();
        JPanel undocRow = createMetricRow(AllIcons.General.Warning, "Undocumented", String.valueOf(undocumented));
        if (undocumented > 20) {
            undocRow.setBackground(new JBColor(new Color(0xFFF8E1), new Color(0x3D2E00)));
            undocRow.setOpaque(true);
        }
        content.add(undocRow);
        
        content.add(Box.createVerticalGlue());
        
        // Replace panel content
        metricsPanel.removeAll();
        metricsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Key Metrics"
        ));
        metricsPanel.add(content, BorderLayout.CENTER);
        metricsPanel.revalidate();
        metricsPanel.repaint();
    }
    
    private void updateConventionalBreakdownPanel() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10));
        
        if (conventionalTypeCount.isEmpty()) {
            JBLabel emptyLabel = new JBLabel("No conventional commits");
            emptyLabel.setForeground(JBColor.GRAY);
            content.add(emptyLabel);
        } else {
            // Sort by count descending
            List<Map.Entry<String, Integer>> sorted = conventionalTypeCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
            
            int total = sorted.stream().mapToInt(Map.Entry::getValue).sum();
            
            for (Map.Entry<String, Integer> entry : sorted) {
                String type = entry.getKey();
                int count = entry.getValue();
                double pct = total > 0 ? (count * 100.0 / total) : 0;
                
                JPanel row = new JPanel(new BorderLayout(5, 0));
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
                
                JBLabel typeLabel = new JBLabel(type);
                typeLabel.setForeground(getTypeColor(type));
                typeLabel.setFont(typeLabel.getFont().deriveFont(Font.BOLD, 11f));
                row.add(typeLabel, BorderLayout.WEST);
                
                JBLabel countLabel = new JBLabel(String.format("%d (%.0f%%)", count, pct));
                countLabel.setFont(countLabel.getFont().deriveFont(11f));
                row.add(countLabel, BorderLayout.EAST);
                
                content.add(row);
                content.add(Box.createVerticalStrut(4));
            }
        }
        
        content.add(Box.createVerticalGlue());
        
        // Replace panel content
        conventionalBreakdownPanel.removeAll();
        conventionalBreakdownPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Conventional Commits"
        ));
        conventionalBreakdownPanel.add(content, BorderLayout.CENTER);
        conventionalBreakdownPanel.revalidate();
        conventionalBreakdownPanel.repaint();
    }
    
    private JPanel createMetricRow(Icon icon, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(5, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(new JBLabel(icon));
        JBLabel labelComponent = new JBLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(11f));
        leftPanel.add(labelComponent);
        row.add(leftPanel, BorderLayout.WEST);
        
        JBLabel valueComponent = new JBLabel(value);
        valueComponent.setFont(valueComponent.getFont().deriveFont(Font.BOLD, 11f));
        row.add(valueComponent, BorderLayout.EAST);
        
        return row;
    }
    
    /**
     * Table model for recent commits with more details.
     */
    private class CommitsTableModel extends AbstractTableModel {
        private final String[] columns = {"Hash", "Type", "Subject", "⚠️", "Issue", "Author", "Date"};
        private List<CommitInfo> commits = new ArrayList<>();
        
        public void setCommits(List<CommitInfo> commits) {
            this.commits = commits != null ? commits : new ArrayList<>();
            fireTableDataChanged();
        }
        
        @Override
        public int getRowCount() {
            return commits.size();
        }
        
        @Override
        public int getColumnCount() {
            return columns.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return columns[column];
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex >= commits.size()) return "";
            CommitInfo commit = commits.get(rowIndex);
            
            return switch (columnIndex) {
                case 0 -> commit.getShortHash();
                case 1 -> commit.getConventionalType() != null ? commit.getConventionalType() : "-";
                case 2 -> truncate(commit.getSubject(), 55);
                case 3 -> commit.isBreakingChange() ? "⚠️" : "";
                case 4 -> commit.getIssueReference() != null ? commit.getIssueReference() : "";
                case 5 -> commit.getAuthor();
                case 6 -> commit.getDateTime().format(TIME_FORMAT);
                default -> "";
            };
        }
        
        private String truncate(String text, int maxLength) {
            if (text == null) return "";
            if (text.length() <= maxLength) return text;
            return text.substring(0, maxLength - 3) + "...";
        }
    }
}
