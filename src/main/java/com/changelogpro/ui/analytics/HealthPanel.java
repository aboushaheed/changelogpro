package com.changelogpro.ui.analytics;

import com.changelogpro.analytics.model.AnalyticsData;
import com.changelogpro.analytics.model.HealthScore;
import com.changelogpro.services.ChangeLogProjectService;
import com.changelogpro.wizard.GenerateChangelogDialog;
import com.changelogpro.wizard.NewEntryWizardDialog;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Panel displaying the project health score and recommendations.
 * Shows a composite score based on changelog management practices.
 */
public class HealthPanel extends JPanel {
    
    private final Project project;
    
    // UI Components
    private JPanel scoreCirclePanel;
    private JPanel criteriaPanel;
    private JPanel recommendationsPanel;
    
    // Data
    private HealthScore healthScore;
    
    public HealthPanel(@NotNull Project project) {
        super(new BorderLayout(0, 15));
        this.project = project;
        setBorder(JBUI.Borders.empty(10));
        
        initUI();
    }
    
    private void initUI() {
        // Header
        JBLabel titleLabel = new JBLabel("Project Health Score");
        titleLabel.setIcon(AllIcons.General.InspectionsOK);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        add(titleLabel, BorderLayout.NORTH);
        
        // Main content
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        
        // Top: Score circle and criteria
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        
        scoreCirclePanel = createScoreCirclePanel();
        topPanel.add(scoreCirclePanel);
        
        criteriaPanel = createCriteriaPanel();
        topPanel.add(criteriaPanel);
        
        contentPanel.add(topPanel, BorderLayout.NORTH);
        
        // Bottom: Recommendations
        recommendationsPanel = createRecommendationsPanel();
        contentPanel.add(recommendationsPanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createScoreCirclePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Overall Score"
        ));
        
        // Score circle canvas
        JPanel circleCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawScoreCircle(g);
            }
        };
        circleCanvas.setPreferredSize(new Dimension(200, 180));
        panel.add(circleCanvas, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void drawScoreCircle(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = scoreCirclePanel.getWidth() - 40;
        int height = 150;
        int centerX = width / 2 + 20;
        int centerY = height / 2 + 10;
        int radius = Math.min(width, height) / 2 - 20;
        
        // Background circle
        g2.setColor(JBColor.border());
        g2.setStroke(new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 0, 360);
        
        if (healthScore == null) {
            // No data state
            g2.setColor(JBColor.GRAY);
            g2.setFont(g2.getFont().deriveFont(14f));
            String text = "No data";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, centerX - fm.stringWidth(text) / 2, centerY + 5);
            return;
        }
        
        // Score arc
        int scorePercentage = healthScore.getScorePercentage();
        int arcAngle = (int) (scorePercentage * 3.6); // 360 degrees = 100%
        
        Color scoreColor = parseColor(healthScore.getGradeColor());
        g2.setColor(scoreColor);
        g2.setStroke(new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 90, -arcAngle);
        
        // Score text
        g2.setColor(JBColor.foreground());
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32f));
        String scoreText = String.valueOf(scorePercentage);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(scoreText, centerX - fm.stringWidth(scoreText) / 2, centerY + 10);
        
        // "/100" text
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 14f));
        g2.setColor(JBColor.GRAY);
        g2.drawString("/100", centerX - 15, centerY + 30);
        
        // Grade text
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
        g2.setColor(scoreColor);
        String grade = healthScore.getGrade();
        fm = g2.getFontMetrics();
        g2.drawString(grade, centerX - fm.stringWidth(grade) / 2, centerY + 55);
    }
    
    private JPanel createCriteriaPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Score Breakdown"
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
    
    private JPanel createRecommendationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "💡 Recommendations"
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
    
    /**
     * Updates the panel with new analytics data.
     */
    public void updateData(@NotNull AnalyticsData data) {
        this.healthScore = data.getHealthScore();
        
        updateCriteriaPanel();
        updateRecommendationsPanel();
        scoreCirclePanel.repaint();
    }
    
    private void updateCriteriaPanel() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10));
        
        if (healthScore == null) {
            content.add(new JBLabel("No health data available"));
        } else {
            for (HealthScore.Criterion criterion : healthScore.getCriteria()) {
                JPanel row = createCriterionRow(criterion);
                content.add(row);
                content.add(Box.createVerticalStrut(8));
            }
        }
        
        content.add(Box.createVerticalGlue());
        
        // Replace panel content
        criteriaPanel.removeAll();
        criteriaPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "Score Breakdown"
        ));
        
        JBScrollPane scrollPane = new JBScrollPane(content);
        scrollPane.setBorder(null);
        criteriaPanel.add(scrollPane, BorderLayout.CENTER);
        criteriaPanel.revalidate();
        criteriaPanel.repaint();
    }
    
    private JPanel createCriterionRow(@NotNull HealthScore.Criterion criterion) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        // Icon
        JBLabel iconLabel = new JBLabel(criterion.getIcon());
        iconLabel.setPreferredSize(new Dimension(25, 20));
        row.add(iconLabel, BorderLayout.WEST);
        
        // Name and description
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        
        JBLabel nameLabel = new JBLabel(criterion.getName());
        nameLabel.setFont(nameLabel.getFont().deriveFont(12f));
        textPanel.add(nameLabel, BorderLayout.NORTH);
        
        JBLabel descLabel = new JBLabel(criterion.getDescription());
        descLabel.setFont(descLabel.getFont().deriveFont(10f));
        descLabel.setForeground(JBColor.GRAY);
        textPanel.add(descLabel, BorderLayout.SOUTH);
        
        row.add(textPanel, BorderLayout.CENTER);
        
        // Score
        JBLabel scoreLabel = new JBLabel(criterion.getScore() + "/" + criterion.getMaxScore());
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD, 12f));
        if (criterion.isPassed()) {
            scoreLabel.setForeground(new JBColor(new Color(0x1a7f37), new Color(0x3fb950)));
        } else if (criterion.getPercentage() >= 50) {
            scoreLabel.setForeground(new JBColor(new Color(0xbf8700), new Color(0xd29922)));
        } else {
            scoreLabel.setForeground(new JBColor(new Color(0xcf222e), new Color(0xf85149)));
        }
        row.add(scoreLabel, BorderLayout.EAST);
        
        return row;
    }
    
    private void updateRecommendationsPanel() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10));
        
        if (healthScore == null || healthScore.getRecommendations().isEmpty()) {
            JBLabel allGood = new JBLabel("No recommendations - your project is in great shape! 🎉");
            allGood.setForeground(new JBColor(new Color(0x1a7f37), new Color(0x3fb950)));
            content.add(allGood);
        } else {
            List<HealthScore.Recommendation> recommendations = healthScore.getRecommendations();
            
            for (HealthScore.Recommendation rec : recommendations) {
                JPanel recPanel = createRecommendationCard(rec);
                content.add(recPanel);
                content.add(Box.createVerticalStrut(10));
            }
        }
        
        content.add(Box.createVerticalGlue());
        
        // Replace panel content
        recommendationsPanel.removeAll();
        recommendationsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            "💡 Recommendations"
        ));
        
        JBScrollPane scrollPane = new JBScrollPane(content);
        scrollPane.setBorder(null);
        recommendationsPanel.add(scrollPane, BorderLayout.CENTER);
        recommendationsPanel.revalidate();
        recommendationsPanel.repaint();
    }
    
    private JPanel createRecommendationCard(@NotNull HealthScore.Recommendation rec) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(getSeverityColor(rec.getSeverity()), 1),
            JBUI.Borders.empty(10)
        ));
        card.setBackground(JBColor.background());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        // Left: Severity icon
        JBLabel iconLabel = new JBLabel(rec.getSeverity().getIcon());
        iconLabel.setFont(iconLabel.getFont().deriveFont(16f));
        iconLabel.setVerticalAlignment(SwingConstants.TOP);
        card.add(iconLabel, BorderLayout.WEST);
        
        // Center: Title and description
        JPanel textPanel = new JPanel(new BorderLayout(0, 3));
        textPanel.setOpaque(false);
        
        JBLabel titleLabel = new JBLabel(rec.getTitle());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 12f));
        textPanel.add(titleLabel, BorderLayout.NORTH);
        
        JBLabel descLabel = new JBLabel("<html>" + rec.getDescription() + "</html>");
        descLabel.setFont(descLabel.getFont().deriveFont(11f));
        descLabel.setForeground(JBColor.GRAY);
        textPanel.add(descLabel, BorderLayout.CENTER);
        
        card.add(textPanel, BorderLayout.CENTER);
        
        // Right: Action button
        if (rec.getActionLabel() != null && !rec.getActionLabel().isEmpty()) {
            JButton actionButton = new JButton(rec.getActionLabel());
            actionButton.setFont(actionButton.getFont().deriveFont(10f));
            actionButton.addActionListener(e -> handleRecommendationAction(rec.getActionId()));
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            buttonPanel.setOpaque(false);
            buttonPanel.add(actionButton);
            card.add(buttonPanel, BorderLayout.EAST);
        }
        
        return card;
    }
    
    private Color getSeverityColor(HealthScore.Severity severity) {
        return switch (severity) {
            case HIGH -> new JBColor(new Color(0xcf222e), new Color(0xf85149));
            case MEDIUM -> new JBColor(new Color(0xbf8700), new Color(0xd29922));
            case LOW -> new JBColor(new Color(0x1a7f37), new Color(0x3fb950));
        };
    }
    
    private void handleRecommendationAction(@Nullable String actionId) {
        if (actionId == null) return;
        
        // These actions trigger other parts of the plugin
        switch (actionId) {
            case "import_git_history" -> {
                // Open Git Import Dialog
                GitImportDialog dialog = new GitImportDialog(project);
                dialog.show();
            }
            case "new_entry" -> {
                // Open new entry wizard
                if (ChangeLogProjectService.getInstance(project).isInitialized()) {
                    NewEntryWizardDialog dialog = new NewEntryWizardDialog(project);
                    dialog.show();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Please initialize the project first.",
                        "Not Initialized",
                        JOptionPane.WARNING_MESSAGE);
                }
            }
            case "generate_changelog" -> {
                // Open generate changelog dialog
                if (ChangeLogProjectService.getInstance(project).isInitialized()) {
                    GenerateChangelogDialog dialog = new GenerateChangelogDialog(project);
                    dialog.show();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Please initialize the project first.",
                        "Not Initialized",
                        JOptionPane.WARNING_MESSAGE);
                }
            }
            case "open_changelog" -> {
                // Open CHANGELOG.md in editor
                String basePath = project.getBasePath();
                if (basePath != null) {
                    File changelogFile = new File(basePath, "CHANGELOG.md");
                    if (changelogFile.exists()) {
                        VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(changelogFile);
                        if (vf != null) {
                            FileEditorManager.getInstance(project).openFile(vf, true);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "CHANGELOG.md not found in project root.",
                            "File Not Found",
                            JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
            default -> { }
        }
    }
    
    private Color parseColor(String hexColor) {
        try {
            return Color.decode(hexColor);
        } catch (NumberFormatException e) {
            return JBColor.foreground();
        }
    }
}
