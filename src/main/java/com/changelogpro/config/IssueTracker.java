package com.changelogpro.config;

/**
 * Supported issue tracking systems.
 */
public enum IssueTracker {
    JIRA("JIRA", "Issue", "/browse/", "PROJECT-123"),
    GITHUB_ISSUES("GitHub Issues", "Issue", "/issues/", "#123"),
    GITLAB_ISSUES("GitLab Issues", "Issue", "/-/issues/", "#123"),
    YOUTRACK("YouTrack", "Issue", "/issue/", "PROJECT-123"),
    LINEAR("Linear", "Issue", "/issue/", "ABC-123"),
    AZURE_BOARDS("Azure Boards", "Work Item", "/_workitems/edit/", "123"),
    BITBUCKET_ISSUES("Bitbucket Issues", "Issue", "/issues/", "#123"),
    REDMINE("Redmine", "Issue", "/issues/", "#123"),
    TRELLO("Trello", "Card", "/c/", "cardId"),
    ASANA("Asana", "Task", "/0/", "taskId"),
    NONE("None", "Reference", "", "REF-123");

    private final String displayName;
    private final String termName;
    private final String urlPath;
    private final String exampleFormat;

    IssueTracker(String displayName, String termName, String urlPath, String exampleFormat) {
        this.displayName = displayName;
        this.termName = termName;
        this.urlPath = urlPath;
        this.exampleFormat = exampleFormat;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTermName() {
        return termName;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public String getExampleFormat() {
        return exampleFormat;
    }

    /**
     * Build the full issue URL.
     */
    public String buildIssueUrl(String baseUrl, String issueId) {
        if (this == NONE || baseUrl == null || baseUrl.isEmpty() || issueId == null || issueId.isEmpty()) {
            return "";
        }
        
        String url = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        
        // Handle special cases
        if (this == GITHUB_ISSUES || this == GITLAB_ISSUES || this == BITBUCKET_ISSUES) {
            // These use the repo URL directly
            return url + urlPath + issueId.replace("#", "");
        }
        
        return url + urlPath + issueId;
    }

    /**
     * Detect issue tracker from URL.
     */
    public static IssueTracker detectFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return NONE;
        }
        
        String lowerUrl = url.toLowerCase();
        
        if (lowerUrl.contains("atlassian.net") || lowerUrl.contains("jira.")) {
            return JIRA;
        } else if (lowerUrl.contains("github.com")) {
            return GITHUB_ISSUES;
        } else if (lowerUrl.contains("gitlab.com") || lowerUrl.contains("gitlab.")) {
            return GITLAB_ISSUES;
        } else if (lowerUrl.contains("youtrack.")) {
            return YOUTRACK;
        } else if (lowerUrl.contains("linear.app")) {
            return LINEAR;
        } else if (lowerUrl.contains("dev.azure.com") || lowerUrl.contains("visualstudio.com")) {
            return AZURE_BOARDS;
        } else if (lowerUrl.contains("bitbucket.")) {
            return BITBUCKET_ISSUES;
        } else if (lowerUrl.contains("redmine.")) {
            return REDMINE;
        } else if (lowerUrl.contains("trello.com")) {
            return TRELLO;
        } else if (lowerUrl.contains("asana.com")) {
            return ASANA;
        }
        
        return NONE;
    }

    /**
     * Check if this tracker uses the Git repo URL for issues.
     */
    public boolean usesRepoUrl() {
        return this == GITHUB_ISSUES || this == GITLAB_ISSUES || this == BITBUCKET_ISSUES;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
