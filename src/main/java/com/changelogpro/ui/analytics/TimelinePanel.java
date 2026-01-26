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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Panel displaying the release timeline with expandable release cards.
 * Shows releases chronologically with entry counts by type.
 */
public class TimelinePanel extends JPanel {
    
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMM yyyy");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    
    private final Project project;
    private JPanel timelineContainer;
    private JComboBox<String> periodFilter;
    private JBLabel countLabel;
    
    // Data
    private List<ReleaseInfo> allReleases = new ArrayList<>();
    private List<ReleaseInfo> filteredReleases = new ArrayList<>();
    
    public TimelinePanel(@NotNull Project project) {
        super(new BorderLayout(0, 10));
        this.project = project;
        setBorder(JBUI.Borders.empty(10));
        
        initUI();
    }
    
    private void initUI() {
        // Header with filter
        JPanel headerPanel = new JPanel(new BorderLayout());
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JBLabel titleLabel = new JBLabel("Release Timeline");
        titleLabel.setIcon(AllIcons.Vcs.History);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        leftPanel.add(titleLabel);
        
        countLabel = new JBLabel("");
        countLabel.setForeground(JBColor.GRAY);
        leftPanel.add(countLabel);
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        
        periodFilter = new JComboBox<>(new String[]{"All Time", "Last Year", "Last 6 Months", "Last 3 Months"});
        periodFilter.addActionListener(e -> applyFilter());
        headerPanel.add(periodFilter, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Timeline container
        timelineContainer = new JPanel();
        timelineContainer.setLayout(new BoxLayout(timelineContainer, BoxLayout.Y_AXIS));
        
        JBScrollPane scrollPane = new JBScrollPane(timelineContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Empty state
        showEmptyState();
    }
    
    private void showEmptyState() {
        timelineContainer.removeAll();
        
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        JBLabel emptyLabel = new JBLabel("No releases found");
        emptyLabel.setForeground(JBColor.GRAY);
        emptyLabel.setIcon(AllIcons.General.Information);
        emptyPanel.add(emptyLabel);
        
        timelineContainer.add(emptyPanel);
        timelineContainer.revalidate();
        timelineContainer.repaint();
    }
    
    /**
     * Updates the timeline with new data.
     */
    public void updateData(@NotNull AnalyticsData data) {
        this.allReleases = new ArrayList<>(data.getReleases());
        applyFilter();
    }
    
    private void applyFilter() {
        filteredReleases = new ArrayList<>();
        LocalDate cutoffDate = getCutoffDate();
        
        for (ReleaseInfo release : allReleases) {
            // Unreleased always shown
            if (release.isUnreleased()) {
                filteredReleases.add(release);
                continue;
            }
            
            // Apply date filter
            if (cutoffDate == null || release.getReleaseDate() == null) {
                filteredReleases.add(release);
            } else if (!release.getReleaseDate().isBefore(cutoffDate)) {
                filteredReleases.add(release);
            }
        }
        
        updateCountLabel();
        renderTimeline();
    }
    
    private LocalDate getCutoffDate() {
        LocalDate now = LocalDate.now();
        return switch (periodFilter.getSelectedIndex()) {
            case 1 -> now.minusYears(1);     // Last Year
            case 2 -> now.minusMonths(6);    // Last 6 Months
            case 3 -> now.minusMonths(3);    // Last 3 Months
            default -> null;                  // All Time
        };
    }
    
    private void updateCountLabel() {
        int shown = filteredReleases.size();
        int total = allReleases.size();
        if (shown == total) {
            countLabel.setText("(" + total + " releases)");
        } else {
            countLabel.setText("(" + shown + " of " + total + " releases)");
        }
    }
    
    private void renderTimeline() {
        if (filteredReleases.isEmpty()) {
            showEmptyState();
            return;
        }
        
        timelineContainer.removeAll();
        
        // Group releases by year
        Map<Integer, List<ReleaseInfo>> byYear = new LinkedHashMap<>();
        for (ReleaseInfo release : filteredReleases) {
            int year = release.getReleaseDate() != null 
                ? release.getReleaseDate().getYear() 
                : LocalDate.now().getYear();
            byYear.computeIfAbsent(year, k -> new ArrayList<>()).add(release);
        }
        
        // Sort years descending
        List<Integer> years = new ArrayList<>(byYear.keySet());
        years.sort(Collections.reverseOrder());
        
        for (Integer year : years) {
            // Year header
            JPanel yearHeader = createYearHeader(year);
            timelineContainer.add(yearHeader);
            timelineContainer.add(Box.createVerticalStrut(5));
            
            // Releases for this year
            for (ReleaseInfo release : byYear.get(year)) {
                JPanel releaseCard = createReleaseCard(release);
                timelineContainer.add(releaseCard);
                timelineContainer.add(Box.createVerticalStrut(8));
            }
            
            timelineContainer.add(Box.createVerticalStrut(10));
        }
        
        // Add glue at the end
        timelineContainer.add(Box.createVerticalGlue());
        
        timelineContainer.revalidate();
        timelineContainer.repaint();
    }
    
    private JPanel createYearHeader(int year) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.setOpaque(false);
        
        JBLabel yearLabel = new JBLabel(String.valueOf(year));
        yearLabel.setFont(yearLabel.getFont().deriveFont(Font.BOLD, 16f));
        yearLabel.setForeground(new JBColor(new Color(0x0969da), new Color(0x58a6ff)));
        panel.add(yearLabel);
        
        // Separator line
        JPanel line = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(JBColor.border());
                g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
            }
        };
        line.setPreferredSize(new Dimension(200, 2));
        line.setOpaque(false);
        
        return panel;
    }
    
    private JPanel createReleaseCard(@NotNull ReleaseInfo release) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(JBColor.border(), 1),
            JBUI.Borders.empty(10)
        ));
        card.setBackground(JBColor.background());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        // Left: Timeline indicator
        JPanel indicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                
                // Dot
                g2.setColor(release.isUnreleased() 
                    ? new JBColor(new Color(0xbf8700), new Color(0xd29922))
                    : new JBColor(new Color(0x1a7f37), new Color(0x3fb950)));
                g2.fillOval(centerX - 6, centerY - 6, 12, 12);
                
                // Line
                g2.setColor(JBColor.border());
                g2.drawLine(centerX, 0, centerX, centerY - 8);
                g2.drawLine(centerX, centerY + 8, centerX, getHeight());
            }
        };
        indicator.setPreferredSize(new Dimension(30, 0));
        indicator.setOpaque(false);
        card.add(indicator, BorderLayout.WEST);
        
        // Center: Content
        JPanel contentPanel = new JPanel(new BorderLayout(0, 5));
        contentPanel.setOpaque(false);
        
        // Version and date
        JPanel headerLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerLine.setOpaque(false);
        
        JBLabel versionLabel = new JBLabel(release.isUnreleased() ? "Unreleased" : "v" + release.getVersion());
        versionLabel.setFont(versionLabel.getFont().deriveFont(Font.BOLD, 14f));
        if (release.isUnreleased()) {
            versionLabel.setForeground(new JBColor(new Color(0xbf8700), new Color(0xd29922)));
        }
        headerLine.add(versionLabel);
        
        if (release.getReleaseDate() != null) {
            JBLabel dateLabel = new JBLabel(release.getReleaseDate().format(DATE_FORMAT));
            dateLabel.setForeground(JBColor.GRAY);
            dateLabel.setFont(dateLabel.getFont().deriveFont(12f));
            headerLine.add(dateLabel);
        }
        
        contentPanel.add(headerLine, BorderLayout.NORTH);
        
        // Change type badges
        JPanel badgesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        badgesPanel.setOpaque(false);
        
        for (ChangeType type : ChangeType.values()) {
            int count = release.getEntryCount(type);
            if (count > 0) {
                JPanel badge = createBadge(type, count);
                badgesPanel.add(badge);
            }
        }
        
        contentPanel.add(badgesPanel, BorderLayout.CENTER);
        
        // Summary (truncated)
        String summary = release.getSummary();
        if (!summary.isEmpty()) {
            JBLabel summaryLabel = new JBLabel(truncate(summary, 80));
            summaryLabel.setForeground(JBColor.GRAY);
            summaryLabel.setFont(summaryLabel.getFont().deriveFont(11f));
            contentPanel.add(summaryLabel, BorderLayout.SOUTH);
        }
        
        card.add(contentPanel, BorderLayout.CENTER);
        
        // Right: Total count
        JBLabel totalLabel = new JBLabel(String.valueOf(release.getTotalEntryCount()));
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 18f));
        totalLabel.setForeground(JBColor.GRAY);
        totalLabel.setToolTipText("Total entries");
        card.add(totalLabel, BorderLayout.EAST);
        
        return card;
    }
    
    private JPanel createBadge(ChangeType type, int count) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        badge.setOpaque(true);
        badge.setBackground(getBadgeColor(type));
        badge.setBorder(JBUI.Borders.empty(2, 6));
        
        JBLabel label = new JBLabel(count + " " + type.getDisplayName());
        label.setFont(label.getFont().deriveFont(10f));
        label.setForeground(JBColor.WHITE);
        badge.add(label);
        
        return badge;
    }
    
    private Color getBadgeColor(ChangeType type) {
        return switch (type) {
            case ADDED -> new JBColor(new Color(0x1a7f37), new Color(0x238636));
            case CHANGED -> new JBColor(new Color(0x0969da), new Color(0x1f6feb));
            case DEPRECATED -> new JBColor(new Color(0xbf8700), new Color(0x9e6a03));
            case REMOVED -> new JBColor(new Color(0xcf222e), new Color(0xda3633));
            case FIXED -> new JBColor(new Color(0x8250df), new Color(0x8957e5));
            case SECURITY -> new JBColor(new Color(0xcf222e), new Color(0xda3633));
        };
    }
    
    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
