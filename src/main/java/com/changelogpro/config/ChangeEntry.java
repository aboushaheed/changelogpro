package com.changelogpro.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Represents a single changelog entry.
 */
public class ChangeEntry {
    
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private String id;
    private LocalDateTime createdAt;
    private long timestamp;
    private ChangeType type;
    private String description;
    private String issueId;
    private String prNumber;
    private String author;
    private String filePath;
    
    /**
     * Create a new changelog entry with default values.
     */
    public ChangeEntry() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.createdAt = LocalDateTime.now();
        this.timestamp = System.currentTimeMillis();
        this.type = ChangeType.ADDED;
        this.description = "";
        this.issueId = "";
        this.prNumber = "";
        this.author = System.getProperty("user.name", "");
        this.filePath = "";
    }
    
    /**
     * Create a new changelog entry with a specific type.
     */
    public ChangeEntry(@NotNull ChangeType type) {
        this();
        this.type = type;
    }
    
    /**
     * Create a new changelog entry with all fields.
     */
    public ChangeEntry(@NotNull ChangeType type, @Nullable String description, 
                       @Nullable String issueId, @Nullable String prNumber, 
                       @Nullable String author) {
        this();
        this.type = type;
        this.description = description != null ? description : "";
        this.issueId = issueId != null ? issueId : "";
        this.prNumber = prNumber != null ? prNumber : "";
        this.author = author != null ? author : "";
    }
    
    /**
     * Generate the filename for this entry.
     */
    public String generateFileName() {
        String timestampStr = createdAt != null ? createdAt.format(FILE_DATE_FORMAT) : String.valueOf(timestamp);
        String sanitizedDesc = sanitizeForFileName(description);
        return timestampStr + "_" + id + "_" + sanitizedDesc + ".yaml";
    }
    
    /**
     * Generate YAML content for this entry.
     */
    public String toYaml(@NotNull ChangeLogConfig config) {
        StringBuilder yaml = new StringBuilder();
        
        yaml.append("# ChangeLog Pro Entry\n");
        if (createdAt != null) {
            yaml.append("# Created: ").append(createdAt.format(DISPLAY_DATE_FORMAT)).append("\n");
        }
        yaml.append("\n");
        
        yaml.append("type: ").append(type.name().toLowerCase()).append("\n");
        yaml.append("description: \"").append(escapeYaml(description)).append("\"\n");
        
        if (issueId != null && !issueId.isEmpty()) {
            yaml.append("issue: \"").append(issueId).append("\"\n");
            
            // Add issue URL if tracker is configured
            String issueUrl = buildIssueUrl(config);
            if (!issueUrl.isEmpty()) {
                yaml.append("issue_url: \"").append(issueUrl).append("\"\n");
            }
        }
        
        if (prNumber != null && !prNumber.isEmpty()) {
            yaml.append("pr: \"").append(prNumber).append("\"\n");
            
            // Add PR URL
            String prUrl = config.getGitProvider().buildPrUrl(config.getRepoUrl(), prNumber);
            if (!prUrl.isEmpty()) {
                yaml.append("pr_url: \"").append(prUrl).append("\"\n");
            }
        }
        
        if (author != null && !author.isEmpty()) {
            yaml.append("author: \"").append(author).append("\"\n");
        }
        
        yaml.append("timestamp: ").append(timestamp).append("\n");
        
        return yaml.toString();
    }
    
    /**
     * Generate Markdown line for CHANGELOG.md.
     */
    public String toMarkdown(@NotNull ChangeLogConfig config) {
        StringBuilder md = new StringBuilder();
        md.append("- ").append(description);
        
        // Add PR/MR link
        if (prNumber != null && !prNumber.isEmpty()) {
            String prUrl = config.getGitProvider().buildPrUrl(config.getRepoUrl(), prNumber);
            String prTerm = config.getGitProvider().getPrTermShort();
            if (!prUrl.isEmpty()) {
                md.append(" [").append(prTerm).append("#").append(prNumber).append("](").append(prUrl).append(")");
            } else {
                md.append(" (").append(prTerm).append("#").append(prNumber).append(")");
            }
        }
        
        // Add issue link
        if (issueId != null && !issueId.isEmpty()) {
            String issueUrl = buildIssueUrl(config);
            if (!issueUrl.isEmpty()) {
                md.append(" closes [").append(issueId).append("](").append(issueUrl).append(")");
            } else {
                md.append(" closes ").append(issueId);
            }
        }
        
        return md.toString();
    }
    
    /**
     * Build the issue URL based on configuration.
     */
    private String buildIssueUrl(@NotNull ChangeLogConfig config) {
        IssueTracker tracker = config.getIssueTracker();
        
        if (tracker == IssueTracker.NONE) {
            return "";
        }
        
        String baseUrl = tracker.usesRepoUrl() ? config.getRepoUrl() : config.getIssueTrackerUrl();
        return tracker.buildIssueUrl(baseUrl, issueId);
    }
    
    /**
     * Sanitize a string for use in a filename.
     */
    private String sanitizeForFileName(String input) {
        if (input == null || input.isEmpty()) {
            return "entry";
        }
        
        // Take first 30 chars, replace non-alphanumeric with underscore
        String sanitized = input.substring(0, Math.min(input.length(), 30))
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        
        return sanitized.isEmpty() ? "entry" : sanitized;
    }
    
    /**
     * Escape special characters for YAML.
     */
    private String escapeYaml(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n");
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getFormattedDate() {
        if (createdAt != null) {
            return createdAt.format(DISPLAY_DATE_FORMAT);
        }
        return "";
    }
    
    public ChangeType getType() {
        return type;
    }
    
    public void setType(ChangeType type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getIssueId() {
        return issueId;
    }
    
    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }
    
    public String getPrNumber() {
        return prNumber;
    }
    
    public void setPrNumber(String prNumber) {
        this.prNumber = prNumber;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
