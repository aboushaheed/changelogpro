package com.changelogpro.config;

/**
 * Supported Git hosting providers.
 * Each provider has specific URL patterns for PRs/MRs and issues.
 */
public enum GitProvider {
    GITHUB("GitHub", "Pull Request", "PR", "/pull/", "/issues/", "/commit/"),
    GITLAB("GitLab", "Merge Request", "MR", "/-/merge_requests/", "/-/issues/", "/-/commit/"),
    BITBUCKET("Bitbucket", "Pull Request", "PR", "/pull-requests/", "/issues/", "/commits/"),
    AZURE_DEVOPS("Azure DevOps", "Pull Request", "PR", "/pullrequest/", "/workitems/edit/", "/commit/"),
    GITEA("Gitea", "Pull Request", "PR", "/pulls/", "/issues/", "/commit/"),
    GOGS("Gogs", "Pull Request", "PR", "/pulls/", "/issues/", "/commit/"),
    CUSTOM("Custom", "Pull Request", "PR", "/pull/", "/issues/", "/commit/");

    private final String displayName;
    private final String prTermFull;
    private final String prTermShort;
    private final String prUrlPath;
    private final String issueUrlPath;
    private final String commitUrlPath;

    GitProvider(String displayName, String prTermFull, String prTermShort, 
                String prUrlPath, String issueUrlPath, String commitUrlPath) {
        this.displayName = displayName;
        this.prTermFull = prTermFull;
        this.prTermShort = prTermShort;
        this.prUrlPath = prUrlPath;
        this.issueUrlPath = issueUrlPath;
        this.commitUrlPath = commitUrlPath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrTermFull() {
        return prTermFull;
    }

    public String getPrTermShort() {
        return prTermShort;
    }

    public String getPrUrlPath() {
        return prUrlPath;
    }

    public String getIssueUrlPath() {
        return issueUrlPath;
    }
    
    public String getCommitUrlPath() {
        return commitUrlPath;
    }

    /**
     * Build the full PR/MR URL.
     */
    public String buildPrUrl(String repoUrl, String prNumber) {
        if (repoUrl == null || repoUrl.isEmpty() || prNumber == null || prNumber.isEmpty()) {
            return "";
        }
        String baseUrl = repoUrl.endsWith("/") ? repoUrl.substring(0, repoUrl.length() - 1) : repoUrl;
        return baseUrl + prUrlPath + prNumber;
    }

    /**
     * Build the full issue URL (for Git-based issue tracking).
     */
    public String buildIssueUrl(String repoUrl, String issueNumber) {
        if (repoUrl == null || repoUrl.isEmpty() || issueNumber == null || issueNumber.isEmpty()) {
            return "";
        }
        String baseUrl = repoUrl.endsWith("/") ? repoUrl.substring(0, repoUrl.length() - 1) : repoUrl;
        return baseUrl + issueUrlPath + issueNumber;
    }
    
    /**
     * Build the full commit URL.
     */
    public String buildCommitUrl(String repoUrl, String commitHash) {
        if (repoUrl == null || repoUrl.isEmpty() || commitHash == null || commitHash.isEmpty()) {
            return "";
        }
        String baseUrl = repoUrl.endsWith("/") ? repoUrl.substring(0, repoUrl.length() - 1) : repoUrl;
        return baseUrl + commitUrlPath + commitHash;
    }

    /**
     * Detect the Git provider from a repository URL.
     */
    public static GitProvider detectFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return CUSTOM;
        }
        
        String lowerUrl = url.toLowerCase();
        
        if (lowerUrl.contains("github.com") || lowerUrl.contains("github.")) {
            return GITHUB;
        } else if (lowerUrl.contains("gitlab.com") || lowerUrl.contains("gitlab.")) {
            return GITLAB;
        } else if (lowerUrl.contains("bitbucket.org") || lowerUrl.contains("bitbucket.")) {
            return BITBUCKET;
        } else if (lowerUrl.contains("dev.azure.com") || lowerUrl.contains("visualstudio.com")) {
            return AZURE_DEVOPS;
        } else if (lowerUrl.contains("gitea.") || lowerUrl.contains("/gitea/")) {
            return GITEA;
        } else if (lowerUrl.contains("gogs.") || lowerUrl.contains("/gogs/")) {
            return GOGS;
        }
        
        return CUSTOM;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
