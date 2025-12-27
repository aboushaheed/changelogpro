package com.changelogpro.ui.analytics;

import com.changelogpro.analytics.model.*;
import com.changelogpro.config.ChangeType;
import com.changelogpro.services.ChangeLogProjectService;
import com.changelogpro.services.LogService;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Dialog for exporting analytics data as an HTML report.
 * Creates a standalone, styled HTML file with charts and tables.
 */
public class ExportHtmlDialog extends DialogWrapper {
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private final Project project;
    private final AnalyticsData data;
    private final LogService logger;
    
    // Options
    private JBCheckBox includeTimelineCheckbox;
    private JBCheckBox includeChangesCheckbox;
    private JBCheckBox includeGitInsightsCheckbox;
    private JBCheckBox includeContributorsCheckbox;
    private JBCheckBox includeHealthCheckbox;
    private JBCheckBox includeCommitsTableCheckbox;
    private JBCheckBox darkModeCheckbox;
    
    public ExportHtmlDialog(@NotNull Project project, @NotNull AnalyticsData data) {
        super(project, true);
        this.project = project;
        this.data = data;
        this.logger = ChangeLogProjectService.getInstance(project).getLogService();
        
        setTitle("Export Analytics Report");
        setOKButtonText("Export");
        init();
    }
    
    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setPreferredSize(new Dimension(400, 350));
        mainPanel.setBorder(JBUI.Borders.empty(10));
        
        // Header info
        JPanel infoPanel = new JPanel(new BorderLayout(10, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Report Information"));
        
        JPanel infoContent = new JPanel(new GridLayout(4, 2, 10, 5));
        infoContent.setBorder(JBUI.Borders.empty(10));
        
        infoContent.add(new JBLabel("Project:"));
        infoContent.add(new JBLabel(data.getProjectName() != null ? data.getProjectName() : project.getName()));
        
        infoContent.add(new JBLabel("Version:"));
        infoContent.add(new JBLabel(data.getCurrentVersion() != null ? data.getCurrentVersion() : "N/A"));
        
        infoContent.add(new JBLabel("Releases:"));
        infoContent.add(new JBLabel(String.valueOf(data.getReleaseCount())));
        
        infoContent.add(new JBLabel("Total Commits:"));
        infoContent.add(new JBLabel(String.valueOf(data.getTotalCommitCount())));
        
        infoPanel.add(infoContent, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Options
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Include Sections"));
        
        JPanel optionsContent = new JPanel(new GridLayout(0, 2, 10, 5));
        optionsContent.setBorder(JBUI.Borders.empty(10));
        
        includeTimelineCheckbox = new JBCheckBox("Release Timeline", true);
        includeChangesCheckbox = new JBCheckBox("Changes Distribution", true);
        includeGitInsightsCheckbox = new JBCheckBox("Git Insights", true);
        includeContributorsCheckbox = new JBCheckBox("Contributors", true);
        includeHealthCheckbox = new JBCheckBox("Health Score", true);
        includeCommitsTableCheckbox = new JBCheckBox("Recent Commits Table", true);
        
        optionsContent.add(includeTimelineCheckbox);
        optionsContent.add(includeChangesCheckbox);
        optionsContent.add(includeGitInsightsCheckbox);
        optionsContent.add(includeContributorsCheckbox);
        optionsContent.add(includeHealthCheckbox);
        optionsContent.add(includeCommitsTableCheckbox);
        
        optionsPanel.add(optionsContent, BorderLayout.CENTER);
        mainPanel.add(optionsPanel, BorderLayout.CENTER);
        
        // Theme
        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        themePanel.setBorder(BorderFactory.createTitledBorder("Theme"));
        
        darkModeCheckbox = new JBCheckBox("Dark mode", false);
        themePanel.add(darkModeCheckbox);
        
        mainPanel.add(themePanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    @Override
    protected void doOKAction() {
        // Choose save location
        FileSaverDescriptor descriptor = new FileSaverDescriptor(
            "Export Analytics Report",
            "Choose location to save HTML report",
            "html"
        );
        
        FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project);
        String defaultName = "analytics-report-" + LocalDate.now().format(DATE_FORMAT);
        VirtualFileWrapper wrapper = dialog.save(Path.of(Objects.requireNonNull(project.getBasePath())), defaultName);
        
        if (wrapper == null) {
            return; // User cancelled
        }
        
        File file = wrapper.getFile();
        if (!file.getName().endsWith(".html")) {
            file = new File(file.getAbsolutePath() + ".html");
        }
        
        try {
            String html = generateHtml();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(html);
            }
            
            logger.success("Analytics report exported to: " + file.getAbsolutePath());
            JOptionPane.showMessageDialog(getContentPane(),
                "Report exported successfully!\n" + file.getAbsolutePath(),
                "Export Complete",
                JOptionPane.INFORMATION_MESSAGE);
            
            super.doOKAction();
            
        } catch (IOException e) {
            logger.error("Failed to export report: " + e.getMessage());
            JOptionPane.showMessageDialog(getContentPane(),
                "Failed to export report: " + e.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String generateHtml() {
        boolean dark = darkModeCheckbox.isSelected();
        StringBuilder html = new StringBuilder();
        
        // HTML Header
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("  <meta charset=\"UTF-8\">\n");
        html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("  <title>Analytics Report - ").append(escapeHtml(project.getName())).append("</title>\n");
        html.append(generateStyles(dark));
        html.append("</head>\n");
        html.append("<body>\n");
        
        // Header
        html.append("<header>\n");
        html.append("  <h1>📊 Analytics Report</h1>\n");
        html.append("  <div class=\"subtitle\">").append(escapeHtml(project.getName())).append("</div>\n");
        html.append("  <div class=\"generated\">Generated: ").append(LocalDateTime.now().format(DATETIME_FORMAT)).append("</div>\n");
        html.append("</header>\n");
        
        // Overview Cards
        html.append("<section class=\"overview\">\n");
        html.append("  <div class=\"card\"><div class=\"value\">").append(data.getReleaseCount()).append("</div><div class=\"label\">Releases</div></div>\n");
        html.append("  <div class=\"card\"><div class=\"value\">").append(data.getTotalCommitCount()).append("</div><div class=\"label\">Commits</div></div>\n");
        html.append("  <div class=\"card\"><div class=\"value\">").append(String.format("%.0f", data.getAverageReleaseCycleDays())).append("d</div><div class=\"label\">Avg Cycle</div></div>\n");
        html.append("  <div class=\"card\"><div class=\"value\">").append(data.getCurrentVersion() != null ? escapeHtml(data.getCurrentVersion()) : "N/A").append("</div><div class=\"label\">Version</div></div>\n");
        html.append("</section>\n");
        
        // Health Score
        if (includeHealthCheckbox.isSelected() && data.getHealthScore() != null) {
            html.append(generateHealthSection(data.getHealthScore()));
        }
        
        // Timeline
        if (includeTimelineCheckbox.isSelected() && !data.getReleases().isEmpty()) {
            html.append(generateTimelineSection(data.getReleases()));
        }
        
        // Changes Distribution
        if (includeChangesCheckbox.isSelected()) {
            html.append(generateChangesSection(data));
        }
        
        // Git Insights
        if (includeGitInsightsCheckbox.isSelected()) {
            html.append(generateGitInsightsSection(data));
        }
        
        // Contributors
        if (includeContributorsCheckbox.isSelected() && !data.getContributors().isEmpty()) {
            html.append(generateContributorsSection(data.getContributors(), data.getTotalCommitCount()));
        }
        
        // Recent Commits Table
        if (includeCommitsTableCheckbox.isSelected() && !data.getRecentCommits().isEmpty()) {
            html.append(generateCommitsTable(data.getRecentCommits()));
        }
        
        // Footer
        html.append("<footer>\n");
        html.append("  <p>Generated by <strong>ChangeLog Pro</strong> - IntelliJ Plugin</p>\n");
        html.append("</footer>\n");
        
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }
    
    private String generateStyles(boolean dark) {
        String bg = dark ? "#0d1117" : "#ffffff";
        String fg = dark ? "#c9d1d9" : "#24292f";
        String cardBg = dark ? "#161b22" : "#f6f8fa";
        String border = dark ? "#30363d" : "#d0d7de";
        String accent = dark ? "#58a6ff" : "#0969da";
        String green = dark ? "#3fb950" : "#1a7f37";
        String purple = dark ? "#a371f7" : "#8250df";
        String yellow = dark ? "#d29922" : "#bf8700";
        String red = dark ? "#f85149" : "#cf222e";
        
        return """
            <style>
              * { box-sizing: border-box; margin: 0; padding: 0; }
              body { 
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif;
                background: %s; color: %s; line-height: 1.6; padding: 20px; max-width: 1200px; margin: 0 auto;
              }
              header { text-align: center; margin-bottom: 30px; padding: 20px; }
              h1 { font-size: 2em; margin-bottom: 5px; }
              h2 { font-size: 1.4em; margin: 30px 0 15px; padding-bottom: 10px; border-bottom: 1px solid %s; }
              .subtitle { font-size: 1.2em; color: %s; }
              .generated { font-size: 0.85em; color: %s; margin-top: 10px; }
              .overview { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; margin-bottom: 30px; }
              .card { background: %s; border: 1px solid %s; border-radius: 8px; padding: 20px; text-align: center; }
              .card .value { font-size: 2em; font-weight: bold; color: %s; }
              .card .label { font-size: 0.9em; color: %s; margin-top: 5px; }
              section { margin-bottom: 30px; }
              table { width: 100%%; border-collapse: collapse; margin-top: 15px; }
              th, td { padding: 10px 12px; text-align: left; border-bottom: 1px solid %s; }
              th { background: %s; font-weight: 600; }
              tr:hover { background: %s; }
              .badge { display: inline-block; padding: 3px 8px; border-radius: 12px; font-size: 0.8em; font-weight: 500; margin-right: 5px; }
              .badge-added { background: %s20; color: %s; }
              .badge-fixed { background: %s20; color: %s; }
              .badge-changed { background: %s20; color: %s; }
              .badge-deprecated { background: %s20; color: %s; }
              .badge-removed { background: %s20; color: %s; }
              .badge-security { background: %s20; color: %s; }
              .health-score { display: flex; align-items: center; gap: 20px; }
              .score-circle { width: 120px; height: 120px; border-radius: 50%%; display: flex; flex-direction: column; align-items: center; justify-content: center; font-weight: bold; }
              .score-number { font-size: 2.5em; }
              .score-grade { font-size: 1em; margin-top: 5px; }
              .criteria-list { flex: 1; }
              .criteria-item { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid %s; }
              .progress-bar { height: 8px; background: %s; border-radius: 4px; overflow: hidden; margin-top: 5px; }
              .progress-fill { height: 100%%; border-radius: 4px; }
              .contributor-row { display: flex; align-items: center; padding: 10px 0; border-bottom: 1px solid %s; }
              .contributor-rank { width: 40px; font-size: 1.2em; }
              .contributor-info { flex: 1; }
              .contributor-name { font-weight: 600; }
              .contributor-stats { font-size: 0.85em; color: %s; }
              .contributor-bar { width: 100px; }
              .hash { font-family: monospace; color: %s; }
              .type-feat { color: %s; font-weight: 600; }
              .type-fix { color: %s; font-weight: 600; }
              .type-docs { color: %s; font-weight: 600; }
              .type-other { color: %s; }
              .chart-bar { display: flex; align-items: center; margin: 8px 0; }
              .chart-label { width: 80px; font-size: 0.9em; }
              .chart-value { flex: 1; height: 24px; background: %s; border-radius: 4px; position: relative; }
              .chart-fill { height: 100%%; border-radius: 4px; display: flex; align-items: center; padding-left: 10px; color: white; font-size: 0.85em; }
              footer { text-align: center; margin-top: 40px; padding: 20px; color: %s; font-size: 0.9em; }
              @media (max-width: 768px) { .overview { grid-template-columns: repeat(2, 1fr); } }
            </style>
            """.formatted(
                bg, fg, border, accent, fg, // body, header
                cardBg, border, accent, fg, // card
                border, cardBg, cardBg, // table
                green, green, purple, purple, accent, accent, yellow, yellow, red, red, red, red, // badges
                border, border, border, fg, // criteria, progress, contributor
                accent, green, purple, accent, fg, // hash, types
                border, fg // chart, footer
            );
    }
    
    private String generateHealthSection(HealthScore score) {
        StringBuilder html = new StringBuilder();
        html.append("<section>\n");
        html.append("  <h2>🏥 Health Score</h2>\n");
        html.append("  <div class=\"health-score\">\n");
        
        // Score circle
        String color = score.getGradeColor();
        html.append("    <div class=\"score-circle\" style=\"border: 8px solid ").append(color).append(";\">\n");
        html.append("      <div class=\"score-number\">").append(score.getScorePercentage()).append("</div>\n");
        html.append("      <div class=\"score-grade\" style=\"color: ").append(color).append(";\">").append(score.getGrade()).append("</div>\n");
        html.append("    </div>\n");
        
        // Criteria
        html.append("    <div class=\"criteria-list\">\n");
        for (HealthScore.Criterion criterion : score.getCriteria()) {
            html.append("      <div class=\"criteria-item\">\n");
            html.append("        <span>").append(criterion.getIcon()).append(" ").append(escapeHtml(criterion.getName())).append("</span>\n");
            html.append("        <span>").append(criterion.getScore()).append("/").append(criterion.getMaxScore()).append("</span>\n");
            html.append("      </div>\n");
        }
        html.append("    </div>\n");
        
        html.append("  </div>\n");
        
        // Recommendations
        if (!score.getRecommendations().isEmpty()) {
            html.append("  <h3 style=\"margin-top: 20px;\">💡 Recommendations</h3>\n");
            html.append("  <ul>\n");
            for (HealthScore.Recommendation rec : score.getRecommendations()) {
                html.append("    <li><strong>").append(escapeHtml(rec.getTitle())).append("</strong>: ");
                html.append(escapeHtml(rec.getDescription())).append("</li>\n");
            }
            html.append("  </ul>\n");
        }
        
        html.append("</section>\n");
        return html.toString();
    }
    
    private String generateTimelineSection(List<ReleaseInfo> releases) {
        StringBuilder html = new StringBuilder();
        html.append("<section>\n");
        html.append("  <h2>📅 Release Timeline</h2>\n");
        html.append("  <table>\n");
        html.append("    <thead><tr><th>Version</th><th>Date</th><th>Changes</th><th>Entries</th></tr></thead>\n");
        html.append("    <tbody>\n");
        
        for (ReleaseInfo release : releases) {
            html.append("    <tr>\n");
            html.append("      <td><strong>").append(escapeHtml(release.getVersion())).append("</strong>");
            if (release.hasBreakingChanges()) {
                html.append(" ⚠️");
            }
            html.append("</td>\n");
            html.append("      <td>").append(release.getReleaseDate() != null ? release.getReleaseDate().format(DATE_FORMAT) : "-").append("</td>\n");
            
            // Change badges
            html.append("      <td>");
            for (ChangeType type : ChangeType.values()) {
                int count = release.getEntryCount(type);
                if (count > 0) {
                    html.append("<span class=\"badge badge-").append(type.name().toLowerCase()).append("\">");
                    html.append(count).append(" ").append(type.getDisplayName()).append("</span>");
                }
            }
            html.append("</td>\n");
            
            html.append("      <td>").append(release.getTotalEntryCount()).append("</td>\n");
            html.append("    </tr>\n");
        }
        
        html.append("    </tbody>\n");
        html.append("  </table>\n");
        html.append("</section>\n");
        return html.toString();
    }
    
    private String generateChangesSection(AnalyticsData data) {
        StringBuilder html = new StringBuilder();
        html.append("<section>\n");
        html.append("  <h2>📊 Changes Distribution</h2>\n");
        
        Map<ChangeType, Double> distribution = data.getChangeTypeDistribution();
        int total = data.getTotalEntryCount();
        
        for (ChangeType type : ChangeType.values()) {
            double pct = distribution.getOrDefault(type, 0.0);
            if (pct > 0) {
                String color = getTypeColor(type);
                html.append("  <div class=\"chart-bar\">\n");
                html.append("    <div class=\"chart-label\">").append(type.getDisplayName()).append("</div>\n");
                html.append("    <div class=\"chart-value\">\n");
                html.append("      <div class=\"chart-fill\" style=\"width: ").append(pct).append("%; background: ").append(color).append(";\">");
                html.append(String.format("%.0f%%", pct)).append("</div>\n");
                html.append("    </div>\n");
                html.append("  </div>\n");
            }
        }
        
        html.append("</section>\n");
        return html.toString();
    }
    
    private String generateGitInsightsSection(AnalyticsData data) {
        StringBuilder html = new StringBuilder();
        html.append("<section>\n");
        html.append("  <h2>🔍 Git Insights</h2>\n");
        
        html.append("  <div style=\"display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px;\">\n");
        html.append("    <div class=\"card\"><div class=\"value\">").append(data.getTotalCommitCount()).append("</div><div class=\"label\">Total Commits</div></div>\n");
        html.append("    <div class=\"card\"><div class=\"value\">").append(String.format("%.0f%%", data.getConventionalCommitPercentage())).append("</div><div class=\"label\">Conventional</div></div>\n");
        html.append("    <div class=\"card\"><div class=\"value\">").append(String.format("%.0f%%", data.getDocumentationCoverage())).append("</div><div class=\"label\">Documented</div></div>\n");
        html.append("    <div class=\"card\"><div class=\"value\">").append(data.getActiveContributorCount()).append("</div><div class=\"label\">Contributors</div></div>\n");
        html.append("  </div>\n");
        
        // Monthly activity
        if (!data.getCommitsByMonth().isEmpty()) {
            html.append("  <h3 style=\"margin-top: 20px;\">Monthly Activity</h3>\n");
            int maxCommits = data.getCommitsByMonth().values().stream().mapToInt(i -> i).max().orElse(1);
            
            for (Map.Entry<String, Integer> entry : data.getCommitsByMonth().entrySet()) {
                double pct = (entry.getValue() * 100.0) / maxCommits;
                html.append("  <div class=\"chart-bar\">\n");
                html.append("    <div class=\"chart-label\">").append(entry.getKey()).append("</div>\n");
                html.append("    <div class=\"chart-value\">\n");
                html.append("      <div class=\"chart-fill\" style=\"width: ").append(pct).append("%; background: #0969da;\">");
                html.append(entry.getValue()).append("</div>\n");
                html.append("    </div>\n");
                html.append("  </div>\n");
            }
        }
        
        html.append("</section>\n");
        return html.toString();
    }
    
    private String generateContributorsSection(List<ContributorStats> contributors, int totalCommits) {
        StringBuilder html = new StringBuilder();
        html.append("<section>\n");
        html.append("  <h2>👥 Contributors</h2>\n");
        
        int rank = 1;
        for (ContributorStats contributor : contributors) {
            if (rank > 15) break;
            
            String medal = switch (rank) {
                case 1 -> "🥇";
                case 2 -> "🥈";
                case 3 -> "🥉";
                default -> rank + ".";
            };
            
            double pct = totalCommits > 0 ? (contributor.getCommitCount() * 100.0 / totalCommits) : 0;
            
            html.append("  <div class=\"contributor-row\">\n");
            html.append("    <div class=\"contributor-rank\">").append(medal).append("</div>\n");
            html.append("    <div class=\"contributor-info\">\n");
            html.append("      <div class=\"contributor-name\">").append(escapeHtml(contributor.getName())).append("</div>\n");
            html.append("      <div class=\"contributor-stats\">").append(contributor.getCommitCount()).append(" commits</div>\n");
            html.append("    </div>\n");
            html.append("    <div class=\"contributor-bar\">\n");
            html.append("      <div class=\"progress-bar\"><div class=\"progress-fill\" style=\"width: ").append(pct).append("%; background: #0969da;\"></div></div>\n");
            html.append("      <div style=\"font-size: 0.85em;\">").append(String.format("%.0f%%", pct)).append("</div>\n");
            html.append("    </div>\n");
            html.append("  </div>\n");
            
            rank++;
        }
        
        html.append("</section>\n");
        return html.toString();
    }
    
    private String generateCommitsTable(List<CommitInfo> commits) {
        StringBuilder html = new StringBuilder();
        html.append("<section>\n");
        html.append("  <h2>📝 Recent Commits</h2>\n");
        html.append("  <table>\n");
        html.append("    <thead><tr><th>Hash</th><th>Type</th><th>Subject</th><th>Author</th><th>Date</th></tr></thead>\n");
        html.append("    <tbody>\n");
        
        int count = 0;
        for (CommitInfo commit : commits) {
            if (count++ >= 50) break;
            
            String typeClass = commit.getConventionalType() != null ? 
                "type-" + commit.getConventionalType().toLowerCase() : "type-other";
            
            html.append("    <tr>\n");
            html.append("      <td class=\"hash\">").append(commit.getShortHash()).append("</td>\n");
            html.append("      <td class=\"").append(typeClass).append("\">");
            html.append(commit.getConventionalType() != null ? commit.getConventionalType() : "-").append("</td>\n");
            html.append("      <td>").append(escapeHtml(truncate(commit.getSubject(), 60)));
            if (commit.isBreakingChange()) {
                html.append(" ⚠️");
            }
            html.append("</td>\n");
            html.append("      <td>").append(escapeHtml(commit.getAuthor())).append("</td>\n");
            html.append("      <td>").append(commit.getDateTime().format(DATETIME_FORMAT)).append("</td>\n");
            html.append("    </tr>\n");
        }
        
        html.append("    </tbody>\n");
        html.append("  </table>\n");
        html.append("</section>\n");
        return html.toString();
    }
    
    private String getTypeColor(ChangeType type) {
        return switch (type) {
            case ADDED -> "#1a7f37";
            case FIXED -> "#8250df";
            case CHANGED -> "#0969da";
            case DEPRECATED -> "#bf8700";
            case REMOVED -> "#cf222e";
            case SECURITY -> "#cf222e";
        };
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
    
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
