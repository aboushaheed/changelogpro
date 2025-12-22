package com.changelogpro.services;

import com.changelogpro.config.ChangeEntry;
import com.changelogpro.config.ChangeLogConfig;
import com.changelogpro.config.ChangeType;
import com.changelogpro.config.GitProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for generating CHANGELOG.md from fragment files.
 */
public class ChangelogGenerator {
    
    private final Project project;
    private final ChangeLogProjectService projectService;
    private final LogService logger;
    
    public ChangelogGenerator(@NotNull Project project) {
        this.project = project;
        this.projectService = ChangeLogProjectService.getInstance(project);
        this.logger = projectService.getLogService();
    }
    
    /**
     * Generate CHANGELOG.md content from unreleased fragments.
     */
    public String generateChangelog(@Nullable String version, boolean includeAuthor) {
        StringBuilder sb = new StringBuilder();
        ChangeLogConfig config = projectService.getConfig();
        
        // Header
        sb.append("# Changelog\n\n");
        sb.append("All notable changes to this project will be documented in this file.\n\n");
        sb.append("The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),\n");
        sb.append("and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).\n\n");
        
        // Read existing changelog to append
        String existingContent = readExistingChangelog();
        String existingEntries = extractExistingEntries(existingContent);
        
        // Generate new section
        String newSection = generateVersionSection(version, includeAuthor);
        
        if (!newSection.isEmpty()) {
            sb.append(newSection);
            sb.append("\n");
        }
        
        // Append existing entries
        if (!existingEntries.isEmpty()) {
            sb.append(existingEntries);
        }
        
        return sb.toString();
    }
    
    /**
     * Generate a version section from unreleased fragments.
     */
    private String generateVersionSection(@Nullable String version, boolean includeAuthor) {
        Map<ChangeType, List<ChangeEntry>> entriesByType = readUnreleasedEntries();
        
        if (entriesByType.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        
        // Version header
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        if (version != null && !version.isEmpty()) {
            sb.append("## [").append(version).append("] - ").append(date).append("\n\n");
        } else {
            sb.append("## [Unreleased]\n\n");
        }
        
        // Add entries by type in order
        ChangeType[] orderedTypes = {
            ChangeType.ADDED,
            ChangeType.CHANGED,
            ChangeType.DEPRECATED,
            ChangeType.REMOVED,
            ChangeType.FIXED,
            ChangeType.SECURITY
        };
        
        for (ChangeType type : orderedTypes) {
            List<ChangeEntry> entries = entriesByType.get(type);
            if (entries != null && !entries.isEmpty()) {
                sb.append("### ").append(type.getDisplayName()).append("\n\n");
                
                for (ChangeEntry entry : entries) {
                    sb.append(formatEntry(entry, includeAuthor));
                    sb.append("\n");
                }
                sb.append("\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Format a single changelog entry.
     */
    private String formatEntry(ChangeEntry entry, boolean includeAuthor) {
        StringBuilder sb = new StringBuilder();
        sb.append("- ");
        sb.append(entry.getDescription());
        
        ChangeLogConfig config = projectService.getConfig();
        
        // Add PR/MR link
        if (entry.getPrNumber() != null && !entry.getPrNumber().isEmpty()) {
            String prUrl = buildPrUrl(entry.getPrNumber());
            String prTerm = config.getGitProvider().getPrTermShort();
            sb.append(" [").append(prTerm).append("#").append(entry.getPrNumber()).append("]");
            if (prUrl != null) {
                sb.append("(").append(prUrl).append(")");
            }
        }
        
        // Add issue link
        if (entry.getIssueId() != null && !entry.getIssueId().isEmpty()) {
            String issueUrl = buildIssueUrl(entry.getIssueId());
            sb.append(" closes [").append(entry.getIssueId()).append("]");
            if (issueUrl != null) {
                sb.append("(").append(issueUrl).append(")");
            }
        }
        
        // Add author
        if (includeAuthor && entry.getAuthor() != null && !entry.getAuthor().isEmpty()) {
            sb.append(" by @").append(entry.getAuthor());
        }
        
        return sb.toString();
    }
    
    /**
     * Build PR/MR URL based on provider.
     */
    @Nullable
    private String buildPrUrl(String prNumber) {
        ChangeLogConfig config = projectService.getConfig();
        String repoUrl = config.getRepoUrl();
        
        if (repoUrl == null || repoUrl.isEmpty()) {
            return null;
        }
        
        // Remove trailing slash
        repoUrl = repoUrl.replaceAll("/$", "");
        
        GitProvider provider = config.getGitProvider();
        switch (provider) {
            case GITHUB:
            case BITBUCKET:
            case GITEA:
            case GOGS:
                return repoUrl + "/pull/" + prNumber;
            case GITLAB:
                return repoUrl + "/-/merge_requests/" + prNumber;
            case AZURE_DEVOPS:
                return repoUrl + "/pullrequest/" + prNumber;
            default:
                return repoUrl + "/pull/" + prNumber;
        }
    }
    
    /**
     * Build issue URL based on tracker.
     */
    @Nullable
    private String buildIssueUrl(String issueId) {
        ChangeLogConfig config = projectService.getConfig();
        String trackerUrl = config.getIssueTrackerUrl();
        
        if (trackerUrl == null || trackerUrl.isEmpty()) {
            return null;
        }
        
        // Remove trailing slash
        trackerUrl = trackerUrl.replaceAll("/$", "");
        
        switch (config.getIssueTracker()) {
            case JIRA:
                return trackerUrl + "/browse/" + issueId;
            case GITHUB_ISSUES:
            case GITLAB_ISSUES:
                return trackerUrl + "/issues/" + issueId.replace("#", "");
            case YOUTRACK:
                return trackerUrl + "/issue/" + issueId;
            case LINEAR:
                return trackerUrl + "/issue/" + issueId;
            case AZURE_BOARDS:
                return trackerUrl + "/_workitems/edit/" + issueId;
            case REDMINE:
                return trackerUrl + "/issues/" + issueId;
            default:
                return trackerUrl + "/" + issueId;
        }
    }
    
    /**
     * Read all unreleased entries from fragment files.
     */
    public Map<ChangeType, List<ChangeEntry>> readUnreleasedEntries() {
        Map<ChangeType, List<ChangeEntry>> result = new LinkedHashMap<>();
        
        String basePath = project.getBasePath();
        if (basePath == null) {
            return result;
        }
        
        Path unreleasedPath = Paths.get(basePath, ".changes", "unreleased");
        if (!Files.exists(unreleasedPath)) {
            return result;
        }
        
        for (ChangeType type : ChangeType.values()) {
            Path typePath = unreleasedPath.resolve(type.getDirectoryName());
            if (Files.exists(typePath) && Files.isDirectory(typePath)) {
                List<ChangeEntry> entries = new ArrayList<>();
                
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(typePath, "*.yaml")) {
                    for (Path file : stream) {
                        ChangeEntry entry = parseYamlFile(file, type);
                        if (entry != null) {
                            entries.add(entry);
                        }
                    }
                } catch (IOException e) {
                    logger.error("Failed to read entries from " + typePath + ": " + e.getMessage());
                }
                
                // Also check for .yml files
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(typePath, "*.yml")) {
                    for (Path file : stream) {
                        ChangeEntry entry = parseYamlFile(file, type);
                        if (entry != null) {
                            entries.add(entry);
                        }
                    }
                } catch (IOException e) {
                    // Ignore
                }
                
                if (!entries.isEmpty()) {
                    // Sort by timestamp (newest first)
                    entries.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    result.put(type, entries);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Parse a YAML fragment file into a ChangeEntry.
     */
    @Nullable
    private ChangeEntry parseYamlFile(Path file, ChangeType type) {
        try {
            String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            
            String description = extractYamlValue(content, "description");
            String issueId = extractYamlValue(content, "issue");
            String prNumber = extractYamlValue(content, "pr");
            String author = extractYamlValue(content, "author");
            String timestampStr = extractYamlValue(content, "timestamp");
            
            if (description == null || description.isEmpty()) {
                return null;
            }
            
            ChangeEntry entry = new ChangeEntry();
            entry.setType(type);
            entry.setDescription(description);
            entry.setIssueId(issueId);
            entry.setPrNumber(prNumber);
            entry.setAuthor(author);
            entry.setFilePath(file.toString());
            
            if (timestampStr != null && !timestampStr.isEmpty()) {
                try {
                    entry.setTimestamp(Long.parseLong(timestampStr));
                } catch (NumberFormatException e) {
                    entry.setTimestamp(System.currentTimeMillis());
                }
            }
            
            return entry;
        } catch (IOException e) {
            logger.error("Failed to parse YAML file " + file + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Extract a value from simple YAML content.
     */
    @Nullable
    private String extractYamlValue(String content, String key) {
        Pattern pattern = Pattern.compile("^" + key + ":\\s*(.+)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String value = matcher.group(1).trim();
            // Remove quotes if present
            if ((value.startsWith("\"") && value.endsWith("\"")) ||
                (value.startsWith("'") && value.endsWith("'"))) {
                value = value.substring(1, value.length() - 1);
            }
            return value;
        }
        return null;
    }
    
    /**
     * Read existing CHANGELOG.md content.
     */
    @Nullable
    private String readExistingChangelog() {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return null;
        }
        
        Path changelogPath = Paths.get(basePath, "CHANGELOG.md");
        if (!Files.exists(changelogPath)) {
            return null;
        }
        
        try {
            return new String(Files.readAllBytes(changelogPath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Extract existing version entries from CHANGELOG.md (everything after the header).
     */
    private String extractExistingEntries(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        // Find the first version section (## [x.x.x] or ## [Unreleased])
        Pattern pattern = Pattern.compile("^## \\[", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            return content.substring(matcher.start());
        }
        
        return "";
    }
    
    /**
     * Write CHANGELOG.md to the project root.
     */
    public boolean writeChangelog(String content) {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return false;
        }
        
        Path changelogPath = Paths.get(basePath, "CHANGELOG.md");
        
        try {
            Files.write(changelogPath, content.getBytes(StandardCharsets.UTF_8));
            logger.success("CHANGELOG.md generated successfully");
            return true;
        } catch (IOException e) {
            logger.error("Failed to write CHANGELOG.md: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Archive unreleased fragments to a versioned directory.
     */
    public boolean archiveFragments(String version) {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return false;
        }
        
        Path unreleasedPath = Paths.get(basePath, ".changes", "unreleased");
        Path archivePath = Paths.get(basePath, ".changes", "v" + version);
        
        if (!Files.exists(unreleasedPath)) {
            return true; // Nothing to archive
        }
        
        try {
            // Create archive directory
            Files.createDirectories(archivePath);
            
            // Move all type directories
            for (ChangeType type : ChangeType.values()) {
                Path sourceDir = unreleasedPath.resolve(type.getDirectoryName());
                Path targetDir = archivePath.resolve(type.getDirectoryName());
                
                if (Files.exists(sourceDir) && Files.isDirectory(sourceDir)) {
                    // Move files
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
                        for (Path file : stream) {
                            if (Files.isRegularFile(file)) {
                                Files.createDirectories(targetDir);
                                Files.move(file, targetDir.resolve(file.getFileName()), 
                                    StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    }
                }
            }
            
            logger.success("Fragments archived to .changes/v" + version);
            return true;
        } catch (IOException e) {
            logger.error("Failed to archive fragments: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete unreleased fragments after generating changelog.
     */
    public boolean deleteUnreleasedFragments() {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return false;
        }
        
        Path unreleasedPath = Paths.get(basePath, ".changes", "unreleased");
        
        if (!Files.exists(unreleasedPath)) {
            return true;
        }
        
        try {
            for (ChangeType type : ChangeType.values()) {
                Path typeDir = unreleasedPath.resolve(type.getDirectoryName());
                if (Files.exists(typeDir) && Files.isDirectory(typeDir)) {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(typeDir)) {
                        for (Path file : stream) {
                            if (Files.isRegularFile(file)) {
                                Files.delete(file);
                            }
                        }
                    }
                }
            }
            
            logger.success("Unreleased fragments deleted");
            return true;
        } catch (IOException e) {
            logger.error("Failed to delete fragments: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Count total unreleased entries.
     */
    public int countUnreleasedEntries() {
        Map<ChangeType, List<ChangeEntry>> entries = readUnreleasedEntries();
        int count = 0;
        for (List<ChangeEntry> list : entries.values()) {
            count += list.size();
        }
        return count;
    }
    
    /**
     * Preview the changelog content without writing.
     */
    public String previewChangelog(@Nullable String version, boolean includeAuthor) {
        return generateChangelog(version, includeAuthor);
    }
}
