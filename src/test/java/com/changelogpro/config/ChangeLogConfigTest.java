package com.changelogpro.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ChangeLogConfig class.
 * Note: These tests focus on the configuration logic without IntelliJ dependencies.
 */
class ChangeLogConfigTest {

    // ========== GitProvider Detection Tests ==========

    @Test
    void testGitProviderDetectionGitHub() {
        GitProvider provider = GitProvider.detectFromUrl("https://github.com/user/repo");
        assertEquals(GitProvider.GITHUB, provider);
    }

    @Test
    void testGitProviderDetectionGitLab() {
        GitProvider provider = GitProvider.detectFromUrl("https://gitlab.com/group/project");
        assertEquals(GitProvider.GITLAB, provider);
    }

    @Test
    void testGitProviderDetectionBitbucket() {
        GitProvider provider = GitProvider.detectFromUrl("https://bitbucket.org/team/repo");
        assertEquals(GitProvider.BITBUCKET, provider);
    }

    @Test
    void testGitProviderDetectionAzureDevOps() {
        GitProvider provider = GitProvider.detectFromUrl("https://dev.azure.com/org/project/_git/repo");
        assertEquals(GitProvider.AZURE_DEVOPS, provider);
    }

    @Test
    void testGitProviderDetectionGitea() {
        GitProvider provider = GitProvider.detectFromUrl("https://gitea.example.com/user/repo");
        assertEquals(GitProvider.GITEA, provider);
    }

    @Test
    void testGitProviderDetectionCustom() {
        GitProvider provider = GitProvider.detectFromUrl("https://custom-git.example.com/repo");
        assertEquals(GitProvider.CUSTOM, provider);
    }

    // ========== IssueTracker Detection Tests ==========

    @Test
    void testIssueTrackerDetectionJira() {
        IssueTracker tracker = IssueTracker.detectFromUrl("https://company.atlassian.net");
        assertEquals(IssueTracker.JIRA, tracker);
    }

    @Test
    void testIssueTrackerDetectionYouTrack() {
        IssueTracker tracker = IssueTracker.detectFromUrl("https://company.youtrack.cloud");
        assertEquals(IssueTracker.YOUTRACK, tracker);
    }

    @Test
    void testIssueTrackerDetectionLinear() {
        IssueTracker tracker = IssueTracker.detectFromUrl("https://linear.app/team");
        assertEquals(IssueTracker.LINEAR, tracker);
    }

    @Test
    void testIssueTrackerDetectionGitHubIssues() {
        IssueTracker tracker = IssueTracker.detectFromUrl("https://github.com/user/repo");
        assertEquals(IssueTracker.GITHUB_ISSUES, tracker);
    }

    @Test
    void testIssueTrackerDetectionGitLabIssues() {
        IssueTracker tracker = IssueTracker.detectFromUrl("https://gitlab.com/group/project");
        assertEquals(IssueTracker.GITLAB_ISSUES, tracker);
    }

    // ========== GitHub Configuration Tests ==========

    @Test
    void testGitHubPrUrl() {
        String url = GitProvider.GITHUB.buildPrUrl("https://github.com/user/repo", "123");
        assertEquals("https://github.com/user/repo/pull/123", url);
    }

    @Test
    void testGitHubPrTerm() {
        assertEquals("Pull Request", GitProvider.GITHUB.getPrTermFull());
        assertEquals("PR", GitProvider.GITHUB.getPrTermShort());
    }

    // ========== GitLab Configuration Tests ==========

    @Test
    void testGitLabMrUrl() {
        String url = GitProvider.GITLAB.buildPrUrl("https://gitlab.com/group/project", "456");
        assertEquals("https://gitlab.com/group/project/-/merge_requests/456", url);
    }

    @Test
    void testGitLabMrTerm() {
        assertEquals("Merge Request", GitProvider.GITLAB.getPrTermFull());
        assertEquals("MR", GitProvider.GITLAB.getPrTermShort());
    }

    // ========== Bitbucket Configuration Tests ==========

    @Test
    void testBitbucketPrUrl() {
        String url = GitProvider.BITBUCKET.buildPrUrl("https://bitbucket.org/team/repo", "789");
        assertEquals("https://bitbucket.org/team/repo/pull-requests/789", url);
    }

    // ========== Azure DevOps Configuration Tests ==========

    @Test
    void testAzureDevOpsPrUrl() {
        String url = GitProvider.AZURE_DEVOPS.buildPrUrl("https://dev.azure.com/org/project/_git/repo", "100");
        assertEquals("https://dev.azure.com/org/project/_git/repo/pullrequest/100", url);
    }

    // ========== JIRA Configuration Tests ==========

    @Test
    void testJiraIssueUrl() {
        String url = IssueTracker.JIRA.buildIssueUrl("https://company.atlassian.net", "PROJ-123");
        assertEquals("https://company.atlassian.net/browse/PROJ-123", url);
    }

    @Test
    void testJiraIssueUrlWithTrailingSlash() {
        String url = IssueTracker.JIRA.buildIssueUrl("https://company.atlassian.net/", "PROJ-123");
        assertEquals("https://company.atlassian.net/browse/PROJ-123", url);
    }

    // ========== GitHub Issues Configuration Tests ==========

    @Test
    void testGitHubIssueUrl() {
        String url = IssueTracker.GITHUB_ISSUES.buildIssueUrl("https://github.com/user/repo", "456");
        assertEquals("https://github.com/user/repo/issues/456", url);
    }

    // ========== GitLab Issues Configuration Tests ==========

    @Test
    void testGitLabIssueUrl() {
        String url = IssueTracker.GITLAB_ISSUES.buildIssueUrl("https://gitlab.com/group/project", "789");
        assertEquals("https://gitlab.com/group/project/-/issues/789", url);
    }

    // ========== YouTrack Configuration Tests ==========

    @Test
    void testYouTrackIssueUrl() {
        String url = IssueTracker.YOUTRACK.buildIssueUrl("https://company.youtrack.cloud", "PROJ-100");
        assertEquals("https://company.youtrack.cloud/issue/PROJ-100", url);
    }

    // ========== Linear Configuration Tests ==========

    @Test
    void testLinearIssueUrl() {
        String url = IssueTracker.LINEAR.buildIssueUrl("https://linear.app/team", "TEAM-200");
        assertEquals("https://linear.app/team/issue/TEAM-200", url);
    }

    // ========== Azure Boards Configuration Tests ==========

    @Test
    void testAzureBoardsIssueUrl() {
        String url = IssueTracker.AZURE_BOARDS.buildIssueUrl("https://dev.azure.com/org/project", "300");
        assertEquals("https://dev.azure.com/org/project/_workitems/edit/300", url);
    }

    // ========== Redmine Configuration Tests ==========

    @Test
    void testRedmineIssueUrl() {
        String url = IssueTracker.REDMINE.buildIssueUrl("https://redmine.example.com", "400");
        assertEquals("https://redmine.example.com/issues/400", url);
    }

    // ========== Bitbucket Issues Configuration Tests ==========

    @Test
    void testBitbucketIssueUrl() {
        String url = IssueTracker.BITBUCKET_ISSUES.buildIssueUrl("https://bitbucket.org/team/repo", "500");
        assertEquals("https://bitbucket.org/team/repo/issues/500", url);
    }

    // ========== Mixed Configuration Tests ==========

    @Test
    void testGitHubWithJiraConfiguration() {
        // Common scenario: GitHub for code, JIRA for issues
        GitProvider gitProvider = GitProvider.GITHUB;
        IssueTracker issueTracker = IssueTracker.JIRA;

        String prUrl = gitProvider.buildPrUrl("https://github.com/myorg/myproject", "123");
        String issueUrl = issueTracker.buildIssueUrl("https://mycompany.atlassian.net", "PROJ-456");

        assertEquals("https://github.com/myorg/myproject/pull/123", prUrl);
        assertEquals("https://mycompany.atlassian.net/browse/PROJ-456", issueUrl);
    }

    @Test
    void testGitLabWithYouTrackConfiguration() {
        GitProvider gitProvider = GitProvider.GITLAB;
        IssueTracker issueTracker = IssueTracker.YOUTRACK;

        String mrUrl = gitProvider.buildPrUrl("https://gitlab.com/group/project", "789");
        String issueUrl = issueTracker.buildIssueUrl("https://mycompany.youtrack.cloud", "PROJ-100");

        assertEquals("https://gitlab.com/group/project/-/merge_requests/789", mrUrl);
        assertEquals("https://mycompany.youtrack.cloud/issue/PROJ-100", issueUrl);
    }

    // ========== Empty/Null URL Tests ==========

    @Test
    void testGitProviderDetectionEmptyUrl() {
        GitProvider provider = GitProvider.detectFromUrl("");
        assertEquals(GitProvider.CUSTOM, provider);
    }

    @Test
    void testGitProviderDetectionNullUrl() {
        GitProvider provider = GitProvider.detectFromUrl(null);
        assertEquals(GitProvider.CUSTOM, provider);
    }

    @Test
    void testIssueTrackerDetectionEmptyUrl() {
        IssueTracker tracker = IssueTracker.detectFromUrl("");
        assertEquals(IssueTracker.NONE, tracker);
    }

    @Test
    void testIssueTrackerDetectionNullUrl() {
        IssueTracker tracker = IssueTracker.detectFromUrl(null);
        assertEquals(IssueTracker.NONE, tracker);
    }

    @Test
    void testBuildPrUrlWithEmptyRepoUrl() {
        String url = GitProvider.GITHUB.buildPrUrl("", "123");
        assertEquals("", url);
    }

    @Test
    void testBuildIssueUrlWithEmptyBaseUrl() {
        String url = IssueTracker.JIRA.buildIssueUrl("", "PROJ-123");
        assertEquals("", url);
    }

    // ========== Uses Repo URL Tests ==========

    @Test
    void testGitHubIssuesUsesRepoUrl() {
        assertTrue(IssueTracker.GITHUB_ISSUES.usesRepoUrl());
    }

    @Test
    void testGitLabIssuesUsesRepoUrl() {
        assertTrue(IssueTracker.GITLAB_ISSUES.usesRepoUrl());
    }

    @Test
    void testBitbucketIssuesUsesRepoUrl() {
        assertTrue(IssueTracker.BITBUCKET_ISSUES.usesRepoUrl());
    }

    @Test
    void testJiraDoesNotUseRepoUrl() {
        assertFalse(IssueTracker.JIRA.usesRepoUrl());
    }

    @Test
    void testYouTrackDoesNotUseRepoUrl() {
        assertFalse(IssueTracker.YOUTRACK.usesRepoUrl());
    }

    @Test
    void testLinearDoesNotUseRepoUrl() {
        assertFalse(IssueTracker.LINEAR.usesRepoUrl());
    }

    // ========== All Providers Have Required Methods ==========

    @Test
    void testAllGitProvidersHavePrTerm() {
        for (GitProvider provider : GitProvider.values()) {
            assertThat(provider.getPrTermFull()).isNotNull().isNotEmpty();
            assertThat(provider.getPrTermShort()).isNotNull().isNotEmpty();
        }
    }

    @Test
    void testAllGitProvidersHaveDisplayName() {
        for (GitProvider provider : GitProvider.values()) {
            assertThat(provider.getDisplayName()).isNotNull().isNotEmpty();
        }
    }

    @Test
    void testAllIssueTrackersHaveDisplayName() {
        for (IssueTracker tracker : IssueTracker.values()) {
            assertThat(tracker.getDisplayName()).isNotNull().isNotEmpty();
        }
    }
}
