package com.changelogpro.actions;

import com.changelogpro.services.ChangeLogProjectService;
import com.changelogpro.wizard.NewEntryWizardDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

/**
 * Action to create a new changelog entry via keyboard shortcut.
 */
public class NewEntryAction extends AnAction {
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        
        ChangeLogProjectService service = ChangeLogProjectService.getInstance(project);
        
        if (!service.isInitialized()) {
            Messages.showWarningDialog(
                project,
                "Please initialize ChangeLog Pro first.\n\nGo to the ChangeLog Pro panel and click 'Initialize Project'.",
                "Project Not Initialized"
            );
            return;
        }
        
        NewEntryWizardDialog dialog = new NewEntryWizardDialog(project);
        dialog.show();
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }
}
