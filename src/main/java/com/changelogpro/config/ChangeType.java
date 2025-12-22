package com.changelogpro.config;

import com.intellij.icons.AllIcons;
import javax.swing.*;

/**
 * Types of changelog entries following the Keep a Changelog standard.
 * @see <a href="https://keepachangelog.com">Keep a Changelog</a>
 */
public enum ChangeType {
    ADDED("Added", "New features", AllIcons.General.Add, "#4CAF50"),
    CHANGED("Changed", "Changes in existing functionality", AllIcons.Actions.Edit, "#2196F3"),
    DEPRECATED("Deprecated", "Soon-to-be removed features", AllIcons.General.Warning, "#FF9800"),
    REMOVED("Removed", "Now removed features", AllIcons.Actions.Cancel, "#F44336"),
    FIXED("Fixed", "Bug fixes", AllIcons.Actions.QuickfixBulb, "#9C27B0"),
    SECURITY("Security", "Vulnerability fixes", AllIcons.Nodes.SecurityRole, "#E91E63");

    private final String displayName;
    private final String description;
    private final Icon icon;
    private final String color;

    ChangeType(String displayName, String description, Icon icon, String color) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Icon getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }

    /**
     * Get the directory name for storing change fragments.
     */
    public String getDirectoryName() {
        return name().toLowerCase();
    }

    /**
     * Get the Markdown header for this change type.
     */
    public String getMarkdownHeader() {
        return "### " + displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
