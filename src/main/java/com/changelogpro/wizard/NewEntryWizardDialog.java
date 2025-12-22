package com.changelogpro.wizard;

import com.changelogpro.config.*;
import com.changelogpro.services.ChangeLogProjectService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Multi-step wizard dialog for creating new changelog entries.
 */
public class NewEntryWizardDialog extends DialogWrapper {
    
    private static final int STEP_TYPE = 0;
    private static final int STEP_DETAILS = 1;
    private static final int STEP_CONFIRM = 2;
    
    private final Project project;
    private final ChangeLogProjectService service;
    private final ChangeLogConfig config;
    private final ChangeEntry entry;
    
    private int currentStep = STEP_TYPE;
    
    // UI Components
    private JPanel mainPanel;
    private JPanel contentPanel;
    private JPanel stepIndicatorPanel;
    private JButton backButton;
    private JButton nextButton;
    
    // Step 1: Type selection
    private JPanel typePanel;
    private ChangeType selectedType = ChangeType.ADDED;
    
    // Step 2: Details
    private JBTextArea descriptionField;
    private JBTextField issueField;
    private JBTextField prField;
    
    // Step 3: Confirmation
    private JBTextArea previewArea;
    
    public NewEntryWizardDialog(@NotNull Project project) {
        super(project, true);
        this.project = project;
        this.service = ChangeLogProjectService.getInstance(project);
        this.config = service.getConfig();
        this.entry = new ChangeEntry(ChangeType.ADDED);
        
        setTitle("ChangeLog Pro - New Entry");
        setSize(550, 450);
        
        initButtons();
        init();
    }
    
    private void initButtons() {
        backButton = new JButton("← Back");
        backButton.setEnabled(false);
        backButton.addActionListener(e -> goBack());
        
        nextButton = new JButton("Next →");
        nextButton.addActionListener(e -> goNext());
    }
    
    @Override
    protected @Nullable JComponent createCenterPanel() {
        mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setPreferredSize(new Dimension(530, 400));
        mainPanel.setBorder(JBUI.Borders.empty(10));
        
        // Step indicator
        stepIndicatorPanel = createStepIndicator();
        mainPanel.add(stepIndicatorPanel, BorderLayout.NORTH);
        
        // Content panel
        contentPanel = new JPanel(new CardLayout());
        contentPanel.add(createTypeSelectionPanel(), "type");
        contentPanel.add(createDetailsPanel(), "details");
        contentPanel.add(createConfirmationPanel(), "confirm");
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        showStep(currentStep);
        
        return mainPanel;
    }
    
    @Override
    protected JComponent createSouthPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10, 0, 0, 0));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.add(backButton);
        buttonPanel.add(nextButton);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> doCancelAction());
        buttonPanel.add(cancelButton);
        
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createStepIndicator() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        panel.setBorder(JBUI.Borders.empty(0, 0, 10, 0));
        
        String[] steps = {"1. Type", "2. Details", "3. Confirm"};
        for (int i = 0; i < steps.length; i++) {
            JLabel label = new JLabel(steps[i]);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            label.setName("step_" + i);
            panel.add(label);
            
            if (i < steps.length - 1) {
                panel.add(new JLabel("→"));
            }
        }
        
        return panel;
    }
    
    private JPanel createTypeSelectionPanel() {
        typePanel = new JPanel(new BorderLayout(0, 15));
        typePanel.setBorder(JBUI.Borders.empty(10));
        
        JLabel titleLabel = new JLabel("Select Change Type");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        typePanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel typesGrid = new JPanel(new GridLayout(3, 2, 10, 10));
        
        for (ChangeType type : ChangeType.values()) {
            JPanel typeCard = createTypeCard(type);
            typesGrid.add(typeCard);
        }
        
        typePanel.add(typesGrid, BorderLayout.CENTER);
        
        return typePanel;
    }
    
    private JPanel createTypeCard(ChangeType type) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(JBColor.border(), 1),
            JBUI.Borders.empty(10)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setName("type_" + type.name());
        
        JLabel iconLabel = new JLabel(type.getIcon());
        JLabel nameLabel = new JLabel(type.getDisplayName());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
        
        JLabel descLabel = new JLabel("<html><small>" + type.getDescription() + "</small></html>");
        descLabel.setForeground(JBColor.GRAY);
        
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(nameLabel, BorderLayout.NORTH);
        textPanel.add(descLabel, BorderLayout.CENTER);
        
        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        
        // Selection handling
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedType = type;
                entry.setType(type);
                updateTypeSelection();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                if (selectedType != type) {
                    card.setBackground(JBColor.background().brighter());
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (selectedType != type) {
                    card.setBackground(JBColor.background());
                }
            }
        });
        
        return card;
    }
    
    private void updateTypeSelection() {
        for (Component comp : ((JPanel) typePanel.getComponent(1)).getComponents()) {
            if (comp instanceof JPanel) {
                JPanel card = (JPanel) comp;
                String name = card.getName();
                if (name != null && name.startsWith("type_")) {
                    String typeName = name.substring(5);
                    if (typeName.equals(selectedType.name())) {
                        card.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new JBColor(new Color(0x4CAF50), new Color(0x81C784)), 2),
                            JBUI.Borders.empty(9)
                        ));
                        card.setBackground(new JBColor(new Color(0xE8F5E9), new Color(0x1B5E20)));
                    } else {
                        card.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(JBColor.border(), 1),
                            JBUI.Borders.empty(10)
                        ));
                        card.setBackground(JBColor.background());
                    }
                }
            }
        }
    }
    
    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBorder(JBUI.Borders.empty(10));
        
        JLabel titleLabel = new JLabel("Enter Details");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Description
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(new JBLabel("Description *"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        descriptionField = new JBTextArea(4, 40);
        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);
        JBScrollPane descScroll = new JBScrollPane(descriptionField);
        formPanel.add(descScroll, gbc);
        
        // Issue ID
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        String issueLabel = config.getIssueTracker().getTermName() + " ID";
        String issueExample = config.getIssueTracker().getExampleFormat();
        formPanel.add(new JBLabel(issueLabel + " (e.g., " + issueExample + ")"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 1;
        issueField = new JBTextField();
        formPanel.add(issueField, gbc);
        
        // PR/MR Number
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.weightx = 0;
        String prLabel = config.getGitProvider().getPrTermFull() + " Number";
        formPanel.add(new JBLabel(prLabel), gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.weightx = 1;
        prField = new JBTextField();
        formPanel.add(prField, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createConfirmationPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBorder(JBUI.Borders.empty(10));
        
        JLabel titleLabel = new JLabel("Confirm Entry");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        previewArea = new JBTextArea();
        previewArea.setEditable(false);
        previewArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JBScrollPane scrollPane = new JBScrollPane(previewArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JLabel infoLabel = new JLabel("<html><small>The entry will be saved to: .changes/unreleased/" + 
            selectedType.getDirectoryName() + "/</small></html>");
        infoLabel.setForeground(JBColor.GRAY);
        panel.add(infoLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void showStep(int step) {
        currentStep = step;
        
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        switch (step) {
            case STEP_TYPE:
                cl.show(contentPanel, "type");
                updateTypeSelection();
                break;
            case STEP_DETAILS:
                cl.show(contentPanel, "details");
                break;
            case STEP_CONFIRM:
                cl.show(contentPanel, "confirm");
                updatePreview();
                break;
        }
        
        updateButtons();
        updateStepIndicator();
    }
    
    private void updateButtons() {
        if (backButton != null) {
            backButton.setEnabled(currentStep > STEP_TYPE);
        }
        if (nextButton != null) {
            if (currentStep == STEP_CONFIRM) {
                nextButton.setText("Create Entry");
            } else {
                nextButton.setText("Next →");
            }
        }
    }
    
    private void updateStepIndicator() {
        if (stepIndicatorPanel == null) return;
        
        for (Component comp : stepIndicatorPanel.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                String name = label.getName();
                if (name != null && name.startsWith("step_")) {
                    int stepNum = Integer.parseInt(name.substring(5));
                    if (stepNum == currentStep) {
                        label.setForeground(new JBColor(new Color(0x2196F3), new Color(0x64B5F6)));
                    } else if (stepNum < currentStep) {
                        label.setForeground(new JBColor(new Color(0x4CAF50), new Color(0x81C784)));
                    } else {
                        label.setForeground(JBColor.GRAY);
                    }
                }
            }
        }
    }
    
    private void updatePreview() {
        entry.setDescription(descriptionField.getText().trim());
        entry.setIssueId(issueField.getText().trim());
        entry.setPrNumber(prField.getText().trim());
        
        StringBuilder preview = new StringBuilder();
        preview.append("=== YAML Content ===\n\n");
        preview.append(entry.toYaml(config));
        preview.append("\n\n=== Markdown Preview ===\n\n");
        preview.append(entry.toMarkdown(config));
        
        previewArea.setText(preview.toString());
        previewArea.setCaretPosition(0);
    }
    
    private void goBack() {
        if (currentStep > STEP_TYPE) {
            showStep(currentStep - 1);
        }
    }
    
    private void goNext() {
        ValidationInfo validation = validateCurrentStep();
        if (validation != null) {
            setErrorText(validation.message);
            return;
        }
        setErrorText(null);
        
        if (currentStep < STEP_CONFIRM) {
            showStep(currentStep + 1);
        } else {
            // Create the entry
            createEntry();
        }
    }
    
    @Nullable
    private ValidationInfo validateCurrentStep() {
        switch (currentStep) {
            case STEP_TYPE:
                if (selectedType == null) {
                    return new ValidationInfo("Please select a change type");
                }
                break;
            case STEP_DETAILS:
                if (descriptionField.getText().trim().isEmpty()) {
                    return new ValidationInfo("Description is required", descriptionField);
                }
                break;
        }
        return null;
    }
    
    private void createEntry() {
        entry.setDescription(descriptionField.getText().trim());
        entry.setIssueId(issueField.getText().trim());
        entry.setPrNumber(prField.getText().trim());
        
        boolean success = service.getFileService().createChangeEntry(entry);
        
        if (success) {
            close(OK_EXIT_CODE);
        } else {
            setErrorText("Failed to create changelog entry. Check the log for details.");
        }
    }
    
    /**
     * Get the created entry (after dialog closes).
     */
    @Nullable
    public ChangeEntry getEntry() {
        return entry;
    }
}
