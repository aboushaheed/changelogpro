package com.changelogpro.services;

import com.changelogpro.config.ChangeEntry;
import com.changelogpro.config.ChangeLogConfig;
import com.changelogpro.config.ChangeType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for file operations related to changelog management.
 */
public class FileService {
    
    private final Project project;
    private final ChangeLogConfig config;
    private final GitService gitService;
    private final LogService logService;
    
    public FileService(@NotNull Project project, @NotNull ChangeLogConfig config,
                       @NotNull GitService gitService, @NotNull LogService logService) {
        this.project = project;
        this.config = config;
        this.gitService = gitService;
        this.logService = logService;
    }
    
    /**
     * Create a new changelog entry file.
     */
    public boolean createChangeEntry(@NotNull ChangeEntry entry) {
        String typeDir = config.getChangeTypeDirectory(entry.getType());
        if (typeDir == null) {
            logService.error("Could not determine directory for change type: " + entry.getType());
            return false;
        }
        
        // Ensure directory exists
        File dir = new File(typeDir);
        if (!dir.exists() && !dir.mkdirs()) {
            logService.error("Could not create directory: " + typeDir);
            return false;
        }
        
        // Create the file
        String fileName = entry.generateFileName();
        File entryFile = new File(dir, fileName);
        
        try {
            String content = entry.toYaml(config);
            Files.write(entryFile.toPath(), content.getBytes(StandardCharsets.UTF_8));
            logService.success("Created changelog entry: " + fileName);
            
            // Refresh VFS
            refreshVfs(entryFile);
            
            // Auto-stage if enabled
            if (config.isAutoStage()) {
                gitService.stageFile(entryFile.getAbsolutePath());
            }
            
            return true;
            
        } catch (IOException e) {
            logService.error("Failed to create changelog entry: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all unreleased changelog entries.
     */
    @NotNull
    public List<File> getUnreleasedEntries() {
        List<File> entries = new ArrayList<>();
        String unreleasedDir = config.getUnreleasedDirectory();
        
        if (unreleasedDir == null) {
            return entries;
        }
        
        File baseDir = new File(unreleasedDir);
        if (!baseDir.exists()) {
            return entries;
        }
        
        // Iterate through each change type directory
        for (ChangeType type : ChangeType.values()) {
            File typeDir = new File(baseDir, type.getDirectoryName());
            if (typeDir.exists() && typeDir.isDirectory()) {
                File[] files = typeDir.listFiles((dir, name) -> name.endsWith(".yaml") || name.endsWith(".yml"));
                if (files != null) {
                    for (File file : files) {
                        entries.add(file);
                    }
                }
            }
        }
        
        return entries;
    }
    
    /**
     * Count unreleased entries by type.
     */
    public int countUnreleasedEntries(@NotNull ChangeType type) {
        String typeDir = config.getChangeTypeDirectory(type);
        if (typeDir == null) {
            return 0;
        }
        
        File dir = new File(typeDir);
        if (!dir.exists() || !dir.isDirectory()) {
            return 0;
        }
        
        File[] files = dir.listFiles((d, name) -> name.endsWith(".yaml") || name.endsWith(".yml"));
        return files != null ? files.length : 0;
    }
    
    /**
     * Count total unreleased entries.
     */
    public int countTotalUnreleasedEntries() {
        int total = 0;
        for (ChangeType type : ChangeType.values()) {
            total += countUnreleasedEntries(type);
        }
        return total;
    }
    
    /**
     * Initialize the project directory structure.
     */
    public boolean initializeProject() {
        logService.info("Initializing ChangeLog Pro for project...");
        
        // Create directory structure
        if (!config.createDirectoryStructure()) {
            logService.error("Failed to create directory structure");
            return false;
        }
        
        logService.success("Directory structure created");
        
        // Create initial CHANGELOG.md if it doesn't exist
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir != null) {
            File changelogFile = new File(baseDir.getPath(), config.getChangelogFile());
            if (!changelogFile.exists()) {
                try {
                    String initialContent = createInitialChangelog();
                    Files.write(changelogFile.toPath(), initialContent.getBytes(StandardCharsets.UTF_8));
                    logService.success("Created " + config.getChangelogFile());
                    refreshVfs(changelogFile);
                } catch (IOException e) {
                    logService.warn("Could not create CHANGELOG.md: " + e.getMessage());
                }
            }
        }
        
        // Save configuration
        config.setInitialized(true);
        config.save();
        logService.success("Configuration saved");
        
        // Stage files if auto-stage is enabled
        if (config.isAutoStage()) {
            String changesDir = config.getChangesDirectory();
            if (changesDir != null) {
                gitService.stageFile(changesDir);
            }
        }
        
        logService.success("ChangeLog Pro initialized successfully!");
        return true;
    }
    
    /**
     * Create initial CHANGELOG.md content.
     */
    @NotNull
    private String createInitialChangelog() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Changelog\n\n");
        sb.append("All notable changes to this project will be documented in this file.\n\n");
        sb.append("The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),\n");
        sb.append("and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).\n\n");
        sb.append("## [Unreleased]\n\n");
        return sb.toString();
    }
    
    /**
     * Refresh the virtual file system for a file.
     */
    private void refreshVfs(@NotNull File file) {
        ApplicationManager.getApplication().invokeLater(() -> {
            VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
            if (vf != null) {
                vf.refresh(false, false);
            }
        });
    }
}
