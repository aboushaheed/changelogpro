package com.changelogpro.config;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Configuration for ChangeLog Pro plugin.
 * Stores project-specific settings for changelog management.
 */
public class ChangeLogConfig {
    
    private static final String CONFIG_FILE_NAME = "changelog-pro.properties";
    private static final String CHANGES_DIR = ".changes";
    private static final String UNRELEASED_DIR = "unreleased";
    
    // Configuration keys
    private static final String KEY_REPO_URL = "repo.url";
    private static final String KEY_GIT_PROVIDER = "git.provider";
    private static final String KEY_ISSUE_TRACKER = "issue.tracker";
    private static final String KEY_ISSUE_TRACKER_URL = "issue.tracker.url";
    private static final String KEY_AUTO_STAGE = "git.auto.stage";
    private static final String KEY_CHANGELOG_FILE = "changelog.file";
    private static final String KEY_INITIALIZED = "initialized";
    
    // Configuration values
    private String repoUrl = "";
    private GitProvider gitProvider = GitProvider.GITHUB;
    private IssueTracker issueTracker = IssueTracker.NONE;
    private String issueTrackerUrl = "";
    private boolean autoStage = true;
    private String changelogFile = "CHANGELOG.md";
    private boolean initialized = false;
    
    private final Project project;
    
    public ChangeLogConfig(@NotNull Project project) {
        this.project = project;
        load();
    }
    
    /**
     * Load configuration from file.
     */
    public void load() {
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir == null) return;
        
        File configFile = new File(baseDir.getPath(), CONFIG_FILE_NAME);
        if (!configFile.exists()) return;
        
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(configFile)) {
            props.load(is);
            
            repoUrl = props.getProperty(KEY_REPO_URL, "");
            gitProvider = parseGitProvider(props.getProperty(KEY_GIT_PROVIDER, "GITHUB"));
            issueTracker = parseIssueTracker(props.getProperty(KEY_ISSUE_TRACKER, "NONE"));
            issueTrackerUrl = props.getProperty(KEY_ISSUE_TRACKER_URL, "");
            autoStage = Boolean.parseBoolean(props.getProperty(KEY_AUTO_STAGE, "true"));
            changelogFile = props.getProperty(KEY_CHANGELOG_FILE, "CHANGELOG.md");
            initialized = Boolean.parseBoolean(props.getProperty(KEY_INITIALIZED, "false"));
            
        } catch (IOException e) {
            // Use defaults
        }
    }
    
    /**
     * Save configuration to file.
     */
    public void save() {
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir == null) return;
        
        Properties props = new Properties();
        props.setProperty(KEY_REPO_URL, repoUrl);
        props.setProperty(KEY_GIT_PROVIDER, gitProvider.name());
        props.setProperty(KEY_ISSUE_TRACKER, issueTracker.name());
        props.setProperty(KEY_ISSUE_TRACKER_URL, issueTrackerUrl);
        props.setProperty(KEY_AUTO_STAGE, String.valueOf(autoStage));
        props.setProperty(KEY_CHANGELOG_FILE, changelogFile);
        props.setProperty(KEY_INITIALIZED, String.valueOf(initialized));
        
        File configFile = new File(baseDir.getPath(), CONFIG_FILE_NAME);
        try (OutputStream os = new FileOutputStream(configFile)) {
            props.store(os, "ChangeLog Pro Configuration");
        } catch (IOException e) {
            // Handle error
        }
    }
    
    /**
     * Get the changes directory path.
     */
    @Nullable
    public String getChangesDirectory() {
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir == null) return null;
        return baseDir.getPath() + File.separator + CHANGES_DIR;
    }
    
    /**
     * Get the unreleased changes directory path.
     */
    @Nullable
    public String getUnreleasedDirectory() {
        String changesDir = getChangesDirectory();
        if (changesDir == null) return null;
        return changesDir + File.separator + UNRELEASED_DIR;
    }
    
    /**
     * Get the directory for a specific change type.
     */
    @Nullable
    public String getChangeTypeDirectory(ChangeType type) {
        String unreleasedDir = getUnreleasedDirectory();
        if (unreleasedDir == null) return null;
        return unreleasedDir + File.separator + type.getDirectoryName();
    }
    
    /**
     * Check if the project is initialized for ChangeLog Pro.
     */
    public boolean isProjectInitialized() {
        if (!initialized) return false;
        
        String changesDir = getChangesDirectory();
        if (changesDir == null) return false;
        
        return new File(changesDir).exists();
    }
    
    /**
     * Create the directory structure for changelog management.
     */
    public boolean createDirectoryStructure() {
        String unreleasedDir = getUnreleasedDirectory();
        if (unreleasedDir == null) return false;
        
        // Create directories for each change type
        for (ChangeType type : ChangeType.values()) {
            File typeDir = new File(unreleasedDir, type.getDirectoryName());
            if (!typeDir.exists() && !typeDir.mkdirs()) {
                return false;
            }
            
            // Create .gitkeep file
            File gitkeep = new File(typeDir, ".gitkeep");
            try {
                if (!gitkeep.exists()) {
                    gitkeep.createNewFile();
                }
            } catch (IOException e) {
                // Ignore
            }
        }
        
        return true;
    }
    
    private GitProvider parseGitProvider(String value) {
        try {
            return GitProvider.valueOf(value);
        } catch (IllegalArgumentException e) {
            return GitProvider.GITHUB;
        }
    }
    
    private IssueTracker parseIssueTracker(String value) {
        try {
            return IssueTracker.valueOf(value);
        } catch (IllegalArgumentException e) {
            return IssueTracker.NONE;
        }
    }
    
    // Getters and Setters
    
    public String getRepoUrl() {
        return repoUrl;
    }
    
    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
        this.gitProvider = GitProvider.detectFromUrl(repoUrl);
    }
    
    public GitProvider getGitProvider() {
        return gitProvider;
    }
    
    public void setGitProvider(GitProvider gitProvider) {
        this.gitProvider = gitProvider;
    }
    
    public IssueTracker getIssueTracker() {
        return issueTracker;
    }
    
    public void setIssueTracker(IssueTracker issueTracker) {
        this.issueTracker = issueTracker;
    }
    
    public String getIssueTrackerUrl() {
        return issueTrackerUrl;
    }
    
    public void setIssueTrackerUrl(String issueTrackerUrl) {
        this.issueTrackerUrl = issueTrackerUrl;
        if (issueTracker == IssueTracker.NONE) {
            this.issueTracker = IssueTracker.detectFromUrl(issueTrackerUrl);
        }
    }
    
    public boolean isAutoStage() {
        return autoStage;
    }
    
    public void setAutoStage(boolean autoStage) {
        this.autoStage = autoStage;
    }
    
    public String getChangelogFile() {
        return changelogFile;
    }
    
    public void setChangelogFile(String changelogFile) {
        this.changelogFile = changelogFile;
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
    
    public Project getProject() {
        return project;
    }
}
