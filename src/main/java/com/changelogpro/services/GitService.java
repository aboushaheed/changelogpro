package com.changelogpro.services;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Service for Git operations.
 * Uses command-line Git to avoid EDT threading issues with IntelliJ Git API.
 */
public class GitService {
    
    private final Project project;
    private final LogService logService;
    
    public GitService(@NotNull Project project, @NotNull LogService logService) {
        this.project = project;
        this.logService = logService;
    }
    
    /**
     * Stage a file for commit.
     */
    public boolean stageFile(@NotNull String filePath) {
        logService.git("Staging file: " + filePath);
        String result = executeGitCommand("add", filePath);
        if (result != null) {
            logService.success("File staged successfully");
            return true;
        }
        return false;
    }
    
    /**
     * Stage all changes.
     */
    public boolean stageAll() {
        logService.git("Staging all changes");
        String result = executeGitCommand("add", "-A");
        if (result != null) {
            logService.success("All changes staged");
            return true;
        }
        return false;
    }
    
    /**
     * Commit staged changes.
     */
    public boolean commit(@NotNull String message) {
        logService.git("Committing: " + message);
        String result = executeGitCommand("commit", "-m", message);
        if (result != null) {
            logService.success("Changes committed");
            return true;
        }
        return false;
    }
    
    /**
     * Create a Git tag.
     */
    public boolean createTag(@NotNull String tagName, @NotNull String message) {
        logService.git("Creating tag: " + tagName);
        String result = executeGitCommand("tag", "-a", tagName, "-m", message);
        if (result != null) {
            logService.success("Tag created: " + tagName);
            return true;
        }
        return false;
    }
    
    /**
     * Check if a tag exists.
     */
    public boolean tagExists(@NotNull String tagName) {
        String result = executeGitCommand("tag", "-l", tagName);
        return result != null && result.trim().equals(tagName);
    }
    
    /**
     * Push to remote.
     */
    public boolean push() {
        logService.git("Pushing to remote...");
        String result = executeGitCommand("push", "origin", "HEAD");
        if (result != null) {
            logService.success("Pushed to remote");
            return true;
        }
        return false;
    }
    
    /**
     * Push to remote with tags.
     */
    public boolean pushWithTags() {
        logService.git("Pushing to remote with tags...");
        String result = executeGitCommand("push", "origin", "HEAD", "--tags");
        if (result != null) {
            logService.success("Pushed to remote with tags");
            return true;
        }
        return false;
    }
    
    /**
     * Pull from remote.
     */
    public boolean pull() {
        logService.git("Pulling from remote...");
        String result = executeGitCommand("pull");
        if (result != null) {
            logService.success("Pulled from remote");
            return true;
        }
        return false;
    }
    
    /**
     * Checkout a branch.
     */
    public boolean checkout(@NotNull String branch) {
        logService.git("Checking out: " + branch);
        String result = executeGitCommand("checkout", branch);
        if (result != null) {
            logService.success("Checked out: " + branch);
            return true;
        }
        return false;
    }
    
    /**
     * Merge a branch into current branch.
     */
    public boolean merge(@NotNull String branch) {
        logService.git("Merging: " + branch);
        String result = executeGitCommand("merge", branch);
        if (result != null) {
            logService.success("Merged: " + branch);
            return true;
        }
        return false;
    }
    
    /**
     * Get the remote URL of the repository.
     */
    @Nullable
    public String getRemoteUrl() {
        String url = executeGitCommand("remote", "get-url", "origin");
        if (url != null) {
            url = url.trim();
            // Convert SSH URL to HTTPS
            url = convertSshToHttps(url);
        }
        return url;
    }
    
    /**
     * Get the current branch name.
     */
    @Nullable
    public String getCurrentBranch() {
        String branch = executeGitCommand("rev-parse", "--abbrev-ref", "HEAD");
        return branch != null ? branch.trim() : null;
    }
    
    /**
     * Get the current user name from Git config.
     */
    @Nullable
    public String getUserName() {
        String name = executeGitCommand("config", "user.name");
        return name != null ? name.trim() : null;
    }
    
    /**
     * Get the current user email from Git config.
     */
    @Nullable
    public String getUserEmail() {
        String email = executeGitCommand("config", "user.email");
        return email != null ? email.trim() : null;
    }
    
    /**
     * Check if the current directory is a Git repository.
     */
    public boolean isGitRepository() {
        String result = executeGitCommand("rev-parse", "--is-inside-work-tree");
        return result != null && result.trim().equals("true");
    }
    
    /**
     * Convert SSH URL to HTTPS URL.
     */
    @NotNull
    private String convertSshToHttps(@NotNull String url) {
        // git@github.com:user/repo.git -> https://github.com/user/repo
        if (url.startsWith("git@")) {
            url = url.replace(":", "/")
                    .replace("git@", "https://")
                    .replaceAll("\\.git$", "");
        }
        // Remove .git suffix if present
        if (url.endsWith(".git")) {
            url = url.substring(0, url.length() - 4);
        }
        return url;
    }
    
    /**
     * Execute a Git command and return the output.
     */
    @Nullable
    private String executeGitCommand(String... args) {
        String basePath = project.getBasePath();
        if (basePath == null) {
            logService.error("Project base directory not found");
            return null;
        }
        
        try {
            String[] command = new String[args.length + 1];
            command[0] = "git";
            System.arraycopy(args, 0, command, 1, args.length);
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(basePath));
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                logService.error("Git command timed out");
                return null;
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String errorOutput = output.toString().trim();
                if (!errorOutput.isEmpty()) {
                    logService.error("Git error: " + errorOutput);
                }
                return null;
            }
            
            return output.toString();
            
        } catch (Exception e) {
            logService.error("Git command failed: " + e.getMessage());
            return null;
        }
    }
}
