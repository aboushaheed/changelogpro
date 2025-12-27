package com.changelogpro.ui.analytics;

import com.changelogpro.analytics.GitAnalyticsService;
import com.changelogpro.analytics.model.CommitInfo;
import com.changelogpro.config.ChangeEntry;
import com.changelogpro.config.ChangeType;
import com.changelogpro.services.ChangeLogProjectService;
import com.changelogpro.services.FileService;
import com.changelogpro.services.LogService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for importing changelog entries from Git commit history.
 * Allows selecting commits and mapping them to changelog entry types.
 */
public class GitImportDialog extends DialogWrapper {
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private final Project project;
    private final GitAnalyticsService gitService;
    private final LogService logger;
    
    // UI Components
    private JBTable commitsTable;
    private CommitsTableModel tableModel;
    private ComboBox<String> rangeCombo;
    private JBCheckBox conventionalOnlyCheckbox;
    private JBCheckBox excludeMergesCheckbox;
    private JBLabel statusLabel;
    private JButton refreshButton;
    
    // Data
    private List<CommitInfo> allCommits = new ArrayList<>();
    private List<CommitInfo> filteredCommits = new ArrayList<>();
    
    public GitImportDialog(@NotNull Project project) {
        super(project, true);
        this.project = project;
        this.logger = ChangeLogProjectService.getInstance(project).getLogService();
        this.gitService = new GitAnalyticsService(project, logger);
        
        setTitle("Import from Git History");
        setOKButtonText("Import Selected");
        init();
        
        loadCommits();
    }
    
    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setPreferredSize(new Dimension(800, 500));
        mainPanel.setBorder(JBUI.Borders.empty(10));
        
        // Top: Filters
        JPanel filterPanel = createFilterPanel();
        mainPanel.add(filterPanel, BorderLayout.NORTH);
        
        // Center: Commits table
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        // Bottom: Status and help
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Filters"));
        
        JPanel filtersRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        
        // Range selector
        filtersRow.add(new JBLabel("Range:"));
        rangeCombo = new ComboBox<>(new String[]{
            "Since last tag",
            "Last 50 commits",
            "Last 100 commits",
            "Last 200 commits",
            "All commits"
        });
        rangeCombo.addActionListener(e -> loadCommits());
        filtersRow.add(rangeCombo);
        
        // Conventional commits only
        conventionalOnlyCheckbox = new JBCheckBox("Conventional commits only", false);
        conventionalOnlyCheckbox.addActionListener(e -> applyFilters());
        filtersRow.add(conventionalOnlyCheckbox);
        
        // Exclude merges
        excludeMergesCheckbox = new JBCheckBox("Exclude merge commits", true);
        excludeMergesCheckbox.addActionListener(e -> applyFilters());
        filtersRow.add(excludeMergesCheckbox);
        
        // Refresh button
        refreshButton = new JButton(AllIcons.Actions.Refresh);
        refreshButton.setToolTipText("Refresh commits");
        refreshButton.addActionListener(e -> loadCommits());
        filtersRow.add(refreshButton);
        
        panel.add(filtersRow, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Select Commits to Import"));
        
        tableModel = new CommitsTableModel();
        commitsTable = new JBTable(tableModel);
        commitsTable.setRowHeight(28);
        commitsTable.setShowGrid(true);
        commitsTable.setGridColor(JBColor.border());
        
        // Column widths
        commitsTable.getColumnModel().getColumn(0).setPreferredWidth(40);  // Checkbox
        commitsTable.getColumnModel().getColumn(0).setMaxWidth(40);
        commitsTable.getColumnModel().getColumn(1).setPreferredWidth(70);  // Hash
        commitsTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Type
        commitsTable.getColumnModel().getColumn(3).setPreferredWidth(350); // Subject
        commitsTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Author
        commitsTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Date
        commitsTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Map to
        
        // Hash column renderer (monospace, blue)
        commitsTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
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
        commitsTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected && value != null && !value.toString().equals("-")) {
                    setForeground(getTypeColor(value.toString()));
                }
                return this;
            }
        });
        
        // Map to column - ComboBox editor
        TableColumn mapToColumn = commitsTable.getColumnModel().getColumn(6);
        JComboBox<String> mapToCombo = new JComboBox<>(new String[]{
            "Auto", "Added", "Changed", "Deprecated", "Removed", "Fixed", "Security", "Skip"
        });
        mapToColumn.setCellEditor(new DefaultCellEditor(mapToCombo));
        
        // Select all / none buttons
        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton selectAllBtn = new JButton("Select All");
        selectAllBtn.addActionListener(e -> tableModel.setAllSelected(true));
        JButton selectNoneBtn = new JButton("Select None");
        selectNoneBtn.addActionListener(e -> tableModel.setAllSelected(false));
        JButton selectConventionalBtn = new JButton("Select Conventional");
        selectConventionalBtn.addActionListener(e -> tableModel.selectConventional());
        
        selectPanel.add(selectAllBtn);
        selectPanel.add(selectNoneBtn);
        selectPanel.add(selectConventionalBtn);
        
        panel.add(selectPanel, BorderLayout.NORTH);
        panel.add(new JBScrollPane(commitsTable), BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        
        statusLabel = new JBLabel("Loading commits...");
        statusLabel.setIcon(AllIcons.General.Information);
        panel.add(statusLabel, BorderLayout.WEST);
        
        JBLabel helpLabel = new JBLabel("<html><small>Tip: Conventional commits (feat, fix, etc.) are auto-mapped to changelog types</small></html>");
        helpLabel.setForeground(JBColor.GRAY);
        panel.add(helpLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void loadCommits() {
        refreshButton.setEnabled(false);
        statusLabel.setText("Loading commits...");
        statusLabel.setIcon(AllIcons.Process.Step_1);
        
        SwingWorker<List<CommitInfo>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CommitInfo> doInBackground() {
                int limit = switch (rangeCombo.getSelectedIndex()) {
                    case 0 -> -1; // Since last tag
                    case 1 -> 50;
                    case 2 -> 100;
                    case 3 -> 200;
                    default -> 500;
                };
                
                if (rangeCombo.getSelectedIndex() == 0) {
                    return gitService.getCommitsSinceLastTag();
                } else {
                    return gitService.getRecentCommits(limit);
                }
            }
            
            @Override
            protected void done() {
                try {
                    allCommits = get();
                    applyFilters();
                    refreshButton.setEnabled(true);
                } catch (Exception e) {
                    statusLabel.setText("Error loading commits: " + e.getMessage());
                    statusLabel.setIcon(AllIcons.General.Error);
                    refreshButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
    
    private void applyFilters() {
        filteredCommits = new ArrayList<>();
        
        for (CommitInfo commit : allCommits) {
            // Filter merge commits
            if (excludeMergesCheckbox.isSelected() && commit.isMergeCommit()) {
                continue;
            }
            
            // Filter non-conventional
            if (conventionalOnlyCheckbox.isSelected() && !commit.isConventionalCommit()) {
                continue;
            }
            
            filteredCommits.add(commit);
        }
        
        tableModel.setCommits(filteredCommits);
        updateStatus();
    }
    
    private void updateStatus() {
        int total = filteredCommits.size();
        int selected = tableModel.getSelectedCount();
        int conventional = (int) filteredCommits.stream().filter(CommitInfo::isConventionalCommit).count();
        
        statusLabel.setText(String.format("%d commits (%d conventional) | %d selected for import", 
            total, conventional, selected));
        statusLabel.setIcon(AllIcons.General.Information);
    }
    
    private Color getTypeColor(String type) {
        return switch (type.toLowerCase()) {
            case "feat" -> new JBColor(new Color(0x1a7f37), new Color(0x3fb950));
            case "fix" -> new JBColor(new Color(0x8250df), new Color(0xa371f7));
            case "docs" -> new JBColor(new Color(0x0969da), new Color(0x58a6ff));
            case "perf" -> new JBColor(new Color(0xbf8700), new Color(0xd29922));
            case "refactor" -> new JBColor(new Color(0x0969da), new Color(0x58a6ff));
            case "security" -> new JBColor(new Color(0xcf222e), new Color(0xf85149));
            default -> JBColor.foreground();
        };
    }
    
    @Override
    protected void doOKAction() {
        List<CommitInfo> selectedCommits = tableModel.getSelectedCommits();
        
        if (selectedCommits.isEmpty()) {
            JOptionPane.showMessageDialog(getContentPane(),
                "Please select at least one commit to import.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Import selected commits
        int imported = importCommits(selectedCommits);
        
        logger.success("Imported " + imported + " changelog entries from Git history");
        
        JOptionPane.showMessageDialog(getContentPane(),
            "Successfully imported " + imported + " changelog entries.",
            "Import Complete",
            JOptionPane.INFORMATION_MESSAGE);
        
        super.doOKAction();
    }
    
    private int importCommits(List<CommitInfo> commits) {
        FileService fileService = ChangeLogProjectService.getInstance(project).getFileService();
        int count = 0;
        
        for (int i = 0; i < commits.size(); i++) {
            CommitInfo commit = commits.get(i);
            String mapTo = tableModel.getMapTo(i);
            
            if ("Skip".equals(mapTo)) {
                continue;
            }
            
            ChangeType type = determineChangeType(commit, mapTo);
            if (type == null) {
                continue;
            }
            
            // Create description from commit
            String description = commit.getSubject();
            // Remove conventional commit prefix if present
            if (commit.isConventionalCommit()) {
                int colonIndex = description.indexOf(':');
                if (colonIndex > 0) {
                    description = description.substring(colonIndex + 1).trim();
                }
            }
            
            // Add issue reference if present
            if (commit.getIssueReference() != null) {
                description += " (" + commit.getIssueReference() + ")";
            }
            
            // Create the entry
            ChangeEntry entry = new ChangeEntry(type, description);
            entry.setCommitHash(commit.getShortHash());
            entry.setAuthor(commit.getAuthor());
            entry.setBreakingChange(commit.isBreakingChange());
            
            if (fileService.createChangeEntry(entry)) {
                count++;
            }
        }
        
        return count;
    }
    
    @Nullable
    private ChangeType determineChangeType(CommitInfo commit, String mapTo) {
        if ("Auto".equals(mapTo)) {
            // Auto-detect from conventional commit type
            if (commit.getConventionalType() != null) {
                return switch (commit.getConventionalType().toLowerCase()) {
                    case "feat" -> ChangeType.ADDED;
                    case "fix", "perf" -> ChangeType.FIXED;
                    case "refactor" -> ChangeType.CHANGED;
                    case "revert" -> ChangeType.REMOVED;
                    case "security" -> ChangeType.SECURITY;
                    default -> null; // Skip docs, style, test, build, ci, chore
                };
            }
            return null; // Non-conventional commits need explicit mapping
        }
        
        // Explicit mapping
        return switch (mapTo) {
            case "Added" -> ChangeType.ADDED;
            case "Changed" -> ChangeType.CHANGED;
            case "Deprecated" -> ChangeType.DEPRECATED;
            case "Removed" -> ChangeType.REMOVED;
            case "Fixed" -> ChangeType.FIXED;
            case "Security" -> ChangeType.SECURITY;
            default -> null;
        };
    }
    
    /**
     * Table model for commits with selection and mapping.
     */
    private class CommitsTableModel extends AbstractTableModel {
        private final String[] columns = {"", "Hash", "Type", "Subject", "Author", "Date", "Map to"};
        private List<CommitInfo> commits = new ArrayList<>();
        private List<Boolean> selected = new ArrayList<>();
        private List<String> mapTo = new ArrayList<>();
        
        public void setCommits(List<CommitInfo> commits) {
            this.commits = commits;
            this.selected = new ArrayList<>();
            this.mapTo = new ArrayList<>();
            
            for (CommitInfo commit : commits) {
                // Auto-select conventional commits
                selected.add(commit.isConventionalCommit());
                mapTo.add("Auto");
            }
            
            fireTableDataChanged();
        }
        
        public void setAllSelected(boolean value) {
            for (int i = 0; i < selected.size(); i++) {
                selected.set(i, value);
            }
            fireTableDataChanged();
        }
        
        public void selectConventional() {
            for (int i = 0; i < commits.size(); i++) {
                selected.set(i, commits.get(i).isConventionalCommit());
            }
            fireTableDataChanged();
        }
        
        public int getSelectedCount() {
            return (int) selected.stream().filter(b -> b).count();
        }
        
        public List<CommitInfo> getSelectedCommits() {
            List<CommitInfo> result = new ArrayList<>();
            for (int i = 0; i < commits.size(); i++) {
                if (selected.get(i)) {
                    result.add(commits.get(i));
                }
            }
            return result;
        }
        
        public String getMapTo(int index) {
            return mapTo.get(index);
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
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) return Boolean.class;
            return String.class;
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0 || columnIndex == 6;
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex >= commits.size()) return "";
            CommitInfo commit = commits.get(rowIndex);
            
            return switch (columnIndex) {
                case 0 -> selected.get(rowIndex);
                case 1 -> commit.getShortHash();
                case 2 -> commit.getConventionalType() != null ? commit.getConventionalType() : "-";
                case 3 -> truncate(commit.getSubject(), 60);
                case 4 -> commit.getAuthor();
                case 5 -> commit.getDateTime().format(DATE_FORMAT);
                case 6 -> mapTo.get(rowIndex);
                default -> "";
            };
        }
        
        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                selected.set(rowIndex, (Boolean) value);
                updateStatus();
            } else if (columnIndex == 6) {
                mapTo.set(rowIndex, (String) value);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }
        
        private String truncate(String text, int maxLength) {
            if (text == null) return "";
            if (text.length() <= maxLength) return text;
            return text.substring(0, maxLength - 3) + "...";
        }
    }
}
