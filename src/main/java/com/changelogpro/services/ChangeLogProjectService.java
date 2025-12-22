package com.changelogpro.services;

import com.changelogpro.config.ChangeLogConfig;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Project-level service for ChangeLog Pro.
 * Manages configuration and provides access to plugin services.
 */
@Service(Service.Level.PROJECT)
public final class ChangeLogProjectService {
    
    private final Project project;
    private final ChangeLogConfig config;
    private final GitService gitService;
    private final FileService fileService;
    private final LogService logService;
    
    public ChangeLogProjectService(@NotNull Project project) {
        this.project = project;
        this.config = new ChangeLogConfig(project);
        this.logService = new LogService(project);
        this.gitService = new GitService(project, logService);
        this.fileService = new FileService(project, config, gitService, logService);
    }
    
    /**
     * Get the project instance.
     */
    @NotNull
    public Project getProject() {
        return project;
    }
    
    /**
     * Get the configuration.
     */
    @NotNull
    public ChangeLogConfig getConfig() {
        return config;
    }
    
    /**
     * Get the Git service.
     */
    @NotNull
    public GitService getGitService() {
        return gitService;
    }
    
    /**
     * Get the file service.
     */
    @NotNull
    public FileService getFileService() {
        return fileService;
    }
    
    /**
     * Get the log service.
     */
    @NotNull
    public LogService getLogService() {
        return logService;
    }
    
    /**
     * Check if the project is initialized.
     */
    public boolean isInitialized() {
        return config.isProjectInitialized();
    }
    
    /**
     * Reload configuration from disk.
     */
    public void reloadConfig() {
        config.load();
    }
    
    /**
     * Save configuration to disk.
     */
    public void saveConfig() {
        config.save();
    }
    
    /**
     * Get the service instance for a project.
     */
    @NotNull
    public static ChangeLogProjectService getInstance(@NotNull Project project) {
        return project.getService(ChangeLogProjectService.class);
    }
}
