package com.changelogpro.analytics.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a Git commit with metadata for analytics purposes.
 * Provides information about commit hash, author, date, message, and conventional commit parsing.
 */
public class CommitInfo {
    
    private final String hash;
    private final String shortHash;
    private final String author;
    private final String authorEmail;
    private final LocalDateTime dateTime;
    private final String subject;
    private final String body;
    private final boolean isMergeCommit;
    
    // Conventional commit fields (parsed from subject)
    private final String conventionalType;
    private final String conventionalScope;
    private final boolean isBreakingChange;
    private final String issueReference;
    
    private CommitInfo(Builder builder) {
        this.hash = builder.hash;
        this.shortHash = builder.hash != null && builder.hash.length() >= 7 
            ? builder.hash.substring(0, 7) : builder.hash;
        this.author = builder.author;
        this.authorEmail = builder.authorEmail;
        this.dateTime = builder.dateTime;
        this.subject = builder.subject;
        this.body = builder.body;
        this.isMergeCommit = builder.isMergeCommit;
        this.conventionalType = builder.conventionalType;
        this.conventionalScope = builder.conventionalScope;
        this.isBreakingChange = builder.isBreakingChange;
        this.issueReference = builder.issueReference;
    }
    
    // Getters
    
    @NotNull
    public String getHash() {
        return hash;
    }
    
    @NotNull
    public String getShortHash() {
        return shortHash;
    }
    
    @NotNull
    public String getAuthor() {
        return author;
    }
    
    @Nullable
    public String getAuthorEmail() {
        return authorEmail;
    }
    
    @NotNull
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    
    @NotNull
    public String getSubject() {
        return subject;
    }
    
    @Nullable
    public String getBody() {
        return body;
    }
    
    public boolean isMergeCommit() {
        return isMergeCommit;
    }
    
    /**
     * Returns the conventional commit type (feat, fix, chore, etc.) if detected.
     */
    @Nullable
    public String getConventionalType() {
        return conventionalType;
    }
    
    /**
     * Returns the conventional commit scope if detected.
     */
    @Nullable
    public String getConventionalScope() {
        return conventionalScope;
    }
    
    /**
     * Returns true if this commit is marked as a breaking change.
     */
    public boolean isBreakingChange() {
        return isBreakingChange;
    }
    
    /**
     * Returns true if this commit follows Conventional Commits format.
     */
    public boolean isConventionalCommit() {
        return conventionalType != null;
    }
    
    /**
     * Returns the issue reference (e.g., #123 or JIRA-456) if found.
     */
    @Nullable
    public String getIssueReference() {
        return issueReference;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommitInfo that = (CommitInfo) o;
        return Objects.equals(hash, that.hash);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s - %s", shortHash, author, subject);
    }
    
    /**
     * Builder for CommitInfo.
     */
    public static class Builder {
        private String hash;
        private String author;
        private String authorEmail;
        private LocalDateTime dateTime;
        private String subject;
        private String body;
        private boolean isMergeCommit;
        private String conventionalType;
        private String conventionalScope;
        private boolean isBreakingChange;
        private String issueReference;
        
        public Builder hash(String hash) {
            this.hash = hash;
            return this;
        }
        
        public Builder author(String author) {
            this.author = author;
            return this;
        }
        
        public Builder authorEmail(String authorEmail) {
            this.authorEmail = authorEmail;
            return this;
        }
        
        public Builder dateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
            return this;
        }
        
        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }
        
        public Builder body(String body) {
            this.body = body;
            return this;
        }
        
        public Builder mergeCommit(boolean isMergeCommit) {
            this.isMergeCommit = isMergeCommit;
            return this;
        }
        
        public Builder conventionalType(String type) {
            this.conventionalType = type;
            return this;
        }
        
        public Builder conventionalScope(String scope) {
            this.conventionalScope = scope;
            return this;
        }
        
        public Builder breakingChange(boolean isBreaking) {
            this.isBreakingChange = isBreaking;
            return this;
        }
        
        public Builder issueReference(String issueRef) {
            this.issueReference = issueRef;
            return this;
        }
        
        public CommitInfo build() {
            Objects.requireNonNull(hash, "hash is required");
            Objects.requireNonNull(author, "author is required");
            Objects.requireNonNull(dateTime, "dateTime is required");
            Objects.requireNonNull(subject, "subject is required");
            return new CommitInfo(this);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
