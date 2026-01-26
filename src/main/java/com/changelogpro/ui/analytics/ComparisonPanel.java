package com.changelogpro.ui.analytics;

import com.changelogpro.analytics.model.AnalyticsData;
import com.changelogpro.analytics.model.ReleaseInfo;
import com.changelogpro.config.ChangeType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Panel for comparing two releases side by side.
 * Shows differences in change counts, timing, and content.
 */
public class ComparisonPanel extends JPanel {
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final Project project;
    
    // UI Components
    private ComboBox<ReleaseWrapper> release1Combo;
    private ComboBox<ReleaseWrapper> release2Combo;
    private JPanel comparisonContent;
    
    // Data
    private List<ReleaseInfo> releases = new ArrayList<>();
    
    public ComparisonPanel(@NotNull Project project) {
        super(new BorderLayout(0, 15));
        this.project = project;
        setBorder(JBUI.Borders.empty(10));
        
        initUI();
    }
    
    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        
        JBLabel titleLabel = new JBLabel("Release Comparison");
        titleLabel.setIcon(AllIcons.Actions.Diff);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Selector panel
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        selectorPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Select Releases to Compare"
        ));
        
        selectorPanel.add(new JBLabel("Release 1:"));
        release1Combo = new ComboBox<>();
        release1Combo.setPreferredSize(new Dimension(150, 28));
        release1Combo.addActionListener(e -> updateComparison());
        selectorPanel.add(release1Combo);
        
        selectorPanel.add(new JBLabel("  vs  "));
        
        selectorPanel.add(new JBLabel("Release 2:"));
        release2Combo = new ComboBox<>();
        release2Combo.setPreferredSize(new Dimension(150, 28));
        release2Combo.addActionListener(e -> updateComparison());
        selectorPanel.add(release2Combo);
        
        JButton swapButton = new JButton(AllIcons.Actions.Refresh);
        swapButton.setToolTipText("Swap releases");
        swapButton.addActionListener(e -> swapReleases());
        selectorPanel.add(swapButton);
        
        add(selectorPanel, BorderLayout.NORTH);
        
        // Comparison content
        comparisonContent = new JPanel(new BorderLayout());
        comparisonContent.setBorder(JBUI.Borders.empty(10, 0));
        
        JBScrollPane scrollPane = new JBScrollPane(comparisonContent);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Updates the panel with new analytics data.
     */
    public void updateData(@NotNull AnalyticsData data) {
        this.releases = new ArrayList<>(data.getReleases());
        this.releases.sort(Comparator.comparing((ReleaseInfo r) -> r.getReleaseDate() != null ? r.getReleaseDate() : LocalDate.MAX).reversed());

        // Update combo boxes
        release1Combo.removeAllItems();
        release2Combo.removeAllItems();
        
        for (ReleaseInfo release : releases) {
            ReleaseWrapper wrapper = new ReleaseWrapper(release);
            release1Combo.addItem(wrapper);
            release2Combo.addItem(wrapper);
        }
        
        // Select first two releases by default
        if (releases.size() >= 2) {
            release1Combo.setSelectedIndex(0);
            release2Combo.setSelectedIndex(1);
        }
        
        updateComparison();
    }
    
    private void swapReleases() {
        int idx1 = release1Combo.getSelectedIndex();
        int idx2 = release2Combo.getSelectedIndex();
        
        release1Combo.setSelectedIndex(idx2);
        release2Combo.setSelectedIndex(idx1);
    }
    
    private void updateComparison() {
        comparisonContent.removeAll();
        
        ReleaseWrapper wrapper1 = (ReleaseWrapper) release1Combo.getSelectedItem();
        ReleaseWrapper wrapper2 = (ReleaseWrapper) release2Combo.getSelectedItem();
        
        if (wrapper1 == null || wrapper2 == null) {
            comparisonContent.add(new JBLabel("Select two releases to compare"), BorderLayout.CENTER);
            comparisonContent.revalidate();
            comparisonContent.repaint();
            return;
        }
        
        if (wrapper1.release == wrapper2.release) {
            comparisonContent.add(new JBLabel("Please select two different releases"), BorderLayout.CENTER);
            comparisonContent.revalidate();
            comparisonContent.repaint();
            return;
        }
        
        ReleaseInfo r1 = wrapper1.release;
        ReleaseInfo r2 = wrapper2.release;
        
        // Create comparison grid
        JPanel gridPanel = new JPanel(new GridLayout(0, 3, 15, 10));
        gridPanel.setBorder(JBUI.Borders.empty(10));
        
        // Header row
        gridPanel.add(createHeaderCell("Metric"));
        gridPanel.add(createHeaderCell(r1.getVersion()));
        gridPanel.add(createHeaderCell(r2.getVersion()));
        
        // Date row
        gridPanel.add(createLabelCell("Release Date"));
        gridPanel.add(createValueCell(formatDate(r1.getReleaseDate())));
        gridPanel.add(createValueCell(formatDate(r2.getReleaseDate())));
        
        // Days between releases
        if (r1.getReleaseDate() != null && r2.getReleaseDate() != null) {
            long daysBetween = Math.abs(ChronoUnit.DAYS.between(r1.getReleaseDate(), r2.getReleaseDate()));
            gridPanel.add(createLabelCell("Days Between"));
            gridPanel.add(createValueCell(""));
            gridPanel.add(createValueCell(daysBetween + " days"));
        }
        
        // Total entries
        int total1 = r1.getTotalEntryCount();
        int total2 = r2.getTotalEntryCount();
        gridPanel.add(createLabelCell("Total Entries"));
        gridPanel.add(createValueCell(String.valueOf(total1)));
        gridPanel.add(createComparisonCell(total2, total1));
        
        // Breaking changes
        int breaking1 = r1.getBreakingChanges().size();
        int breaking2 = r2.getBreakingChanges().size();
        gridPanel.add(createLabelCell("Breaking Changes"));
        gridPanel.add(createValueCell(String.valueOf(breaking1), breaking1 > 0 ? 
            new JBColor(new Color(0xcf222e), new Color(0xf85149)) : null));
        gridPanel.add(createComparisonCell(breaking2, breaking1, true));
        
        // Separator
        gridPanel.add(createSeparator());
        gridPanel.add(createSeparator());
        gridPanel.add(createSeparator());
        
        // Change type breakdown
        for (ChangeType type : ChangeType.values()) {
            int count1 = r1.getEntryCount(type);
            int count2 = r2.getEntryCount(type);
            
            if (count1 > 0 || count2 > 0) {
                gridPanel.add(createLabelCell(type.getDisplayName()));
                gridPanel.add(createValueCell(String.valueOf(count1), getTypeColor(type)));
                gridPanel.add(createComparisonCell(count2, count1));
            }
        }
        
        comparisonContent.add(gridPanel, BorderLayout.NORTH);
        
        // Summary panel
        JPanel summaryPanel = createSummaryPanel(r1, r2);
        comparisonContent.add(summaryPanel, BorderLayout.CENTER);
        
        comparisonContent.revalidate();
        comparisonContent.repaint();
    }
    
    private JPanel createSummaryPanel(ReleaseInfo r1, ReleaseInfo r2) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "📊 Comparison Summary"
        ));
        
        int total1 = r1.getTotalEntryCount();
        int total2 = r2.getTotalEntryCount();
        int diff = total2 - total1;
        
        // Size comparison
        String sizeText;
        if (diff > 0) {
            sizeText = String.format("📈 %s has %d more changes than %s", r2.getVersion(), diff, r1.getVersion());
        } else if (diff < 0) {
            sizeText = String.format("📉 %s has %d fewer changes than %s", r2.getVersion(), Math.abs(diff), r1.getVersion());
        } else {
            sizeText = String.format("📊 Both releases have the same number of changes (%d)", total1);
        }
        panel.add(new JBLabel(sizeText));
        panel.add(Box.createVerticalStrut(8));
        
        // Breaking changes warning
        int breaking1 = r1.getBreakingChanges().size();
        int breaking2 = r2.getBreakingChanges().size();
        if (breaking1 > 0 || breaking2 > 0) {
            JBLabel breakingLabel = new JBLabel(String.format(
                "⚠️ Breaking changes: %s has %d, %s has %d",
                r1.getVersion(), breaking1, r2.getVersion(), breaking2
            ));
            breakingLabel.setForeground(new JBColor(new Color(0xbf8700), new Color(0xd29922)));
            panel.add(breakingLabel);
            panel.add(Box.createVerticalStrut(8));
        }
        
        // Focus analysis
        ChangeType dominant1 = getDominantType(r1);
        ChangeType dominant2 = getDominantType(r2);
        
        if (dominant1 != null || dominant2 != null) {
            String focusText = "🎯 Release focus: ";
            if (dominant1 != null) {
                focusText += String.format("%s → %s", r1.getVersion(), dominant1.getDisplayName());
            }
            if (dominant1 != null && dominant2 != null) {
                focusText += ", ";
            }
            if (dominant2 != null) {
                focusText += String.format("%s → %s", r2.getVersion(), dominant2.getDisplayName());
            }
            panel.add(new JBLabel(focusText));
        }
        
        return panel;
    }
    
    private ChangeType getDominantType(ReleaseInfo release) {
        return release.getEntryCounts().entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    private JPanel createHeaderCell(String text) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(new JBColor(new Color(0xf6f8fa), new Color(0x21262d)));
        cell.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, JBColor.border()),
            JBUI.Borders.empty(8)
        ));
        
        JBLabel label = new JBLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        cell.add(label, BorderLayout.CENTER);
        
        return cell;
    }
    
    private JPanel createLabelCell(String text) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBorder(JBUI.Borders.empty(5, 0));
        
        JBLabel label = new JBLabel(text);
        label.setForeground(JBColor.GRAY);
        cell.add(label, BorderLayout.WEST);
        
        return cell;
    }
    
    private JPanel createValueCell(String text) {
        return createValueCell(text, null);
    }
    
    private JPanel createValueCell(String text, Color color) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBorder(JBUI.Borders.empty(5, 0));
        
        JBLabel label = new JBLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        if (color != null) {
            label.setForeground(color);
        }
        cell.add(label, BorderLayout.CENTER);
        
        return cell;
    }
    
    private JPanel createComparisonCell(int value, int baseValue) {
        return createComparisonCell(value, baseValue, false);
    }
    
    private JPanel createComparisonCell(int value, int baseValue, boolean invertColors) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBorder(JBUI.Borders.empty(5, 0));
        
        int diff = value - baseValue;
        String text = String.valueOf(value);
        
        JBLabel valueLabel = new JBLabel(text);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        if (diff != 0) {
            String arrow = diff > 0 ? " ↑" : " ↓";
            valueLabel.setText(text + arrow + Math.abs(diff));
            
            Color color;
            if (invertColors) {
                // For breaking changes, more is bad
                color = diff > 0 ? 
                    new JBColor(new Color(0xcf222e), new Color(0xf85149)) :
                    new JBColor(new Color(0x1a7f37), new Color(0x3fb950));
            } else {
                // For normal changes, more is neutral/good
                color = diff > 0 ? 
                    new JBColor(new Color(0x1a7f37), new Color(0x3fb950)) :
                    new JBColor(new Color(0xbf8700), new Color(0xd29922));
            }
            valueLabel.setForeground(color);
        }
        
        cell.add(valueLabel, BorderLayout.CENTER);
        return cell;
    }
    
    private JPanel createSeparator() {
        JPanel sep = new JPanel();
        sep.setPreferredSize(new Dimension(0, 1));
        sep.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border()));
        return sep;
    }
    
    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "N/A";
    }
    
    private Color getTypeColor(ChangeType type) {
        return switch (type) {
            case ADDED -> new JBColor(new Color(0x1a7f37), new Color(0x3fb950));
            case FIXED -> new JBColor(new Color(0x8250df), new Color(0xa371f7));
            case CHANGED -> new JBColor(new Color(0x0969da), new Color(0x58a6ff));
            case DEPRECATED -> new JBColor(new Color(0xbf8700), new Color(0xd29922));
            case REMOVED -> new JBColor(new Color(0xcf222e), new Color(0xf85149));
            case SECURITY -> new JBColor(new Color(0xcf222e), new Color(0xf85149));
        };
    }
    
    /**
     * Wrapper class for releases in combo box.
     */
    private static class ReleaseWrapper {
        final ReleaseInfo release;
        
        ReleaseWrapper(ReleaseInfo release) {
            this.release = release;
        }
        
        @Override
        public String toString() {
            if (release.isUnreleased()) {
                return "Unreleased (" + release.getTotalEntryCount() + " changes)";
            }
            return release.getVersion();
        }
    }
}
