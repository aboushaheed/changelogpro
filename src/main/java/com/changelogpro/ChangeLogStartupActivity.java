package com.changelogpro;

import com.changelogpro.services.ChangeLogProjectService;
import com.changelogpro.wizard.InitializeWizardDialog;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

/**
 * Startup activity that shows a welcome notification when the plugin is first installed.
 */
public class ChangeLogStartupActivity implements StartupActivity.DumbAware {
    
    private static final String PLUGIN_VERSION = "1.0.0";
    private static final String LAST_VERSION_KEY = "com.changelogpro.lastVersion";
    
    @Override
    public void runActivity(@NotNull Project project) {
        PropertiesComponent properties = PropertiesComponent.getInstance();
        String lastVersion = properties.getValue(LAST_VERSION_KEY, "");
        
        if (lastVersion.isEmpty()) {
            // First installation
            showWelcomeNotification(project);
        } else if (!lastVersion.equals(PLUGIN_VERSION)) {
            // Plugin updated
            showUpdateNotification(project);
        }
        
        // Save current version
        properties.setValue(LAST_VERSION_KEY, PLUGIN_VERSION);
    }
    
    private void showWelcomeNotification(@NotNull Project project) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("ChangeLog Pro")
            .createNotification(
                "ChangeLog Pro Installed",
                "<html>Welcome to ChangeLog Pro v" + PLUGIN_VERSION + "!<br><br>" +
                "Create professional changelogs following the Keep a Changelog standard.<br><br>" +
                "<b>Getting started:</b><br>" +
                "Open the ChangeLog Pro panel from the right sidebar and click 'Initialize Project'.</html>",
                NotificationType.INFORMATION
            )
            .addAction(NotificationAction.createSimple("Open ChangeLog Pro", () -> {
                openToolWindow(project);
            }))
            .addAction(NotificationAction.createSimple("Initialize Project", () -> {
                new InitializeWizardDialog(project).show();
            }))
            .notify(project);
    }
    
    private void showUpdateNotification(@NotNull Project project) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("ChangeLog Pro")
            .createNotification(
                "ChangeLog Pro Updated",
                "ChangeLog Pro has been updated to v" + PLUGIN_VERSION + ".",
                NotificationType.INFORMATION
            )
            .addAction(NotificationAction.createSimple("Open ChangeLog Pro", () -> {
                openToolWindow(project);
            }))
            .notify(project);
    }
    
    private void openToolWindow(@NotNull Project project) {
        ToolWindowManager manager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = manager.getToolWindow("ChangeLog Pro");
        if (toolWindow != null) {
            toolWindow.show();
        }
    }
}
