package com.changelogpro.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for IssueTracker enum.
 */
class IssueTrackerTest {

    // ========== JIRA Tests ==========

    @Test
    void testJiraDisplayName() {
        assertEquals("JIRA", IssueTracker.JIRA.getDisplayName());
    }

    @Test
    void testJiraTermName() {
        assertEquals("Issue", IssueTracker.JIRA.getTermName());
    }

    @Test
    void testJiraUrlPath() {
        assertEquals("/browse/", IssueTracker.JIRA.getUrlPath());
    }

    @Test
    void testJiraExampleFormat() {
        assertEquals("PROJECT-123", IssueTracker.JIRA.getExampleFormat());
    }

    @Test
    void testJiraBuildIssueUrl() {
        String url = IssueTracker.JIRA.buildIssueUrl("https://mycompany.atlassian.net", "PROJ-123");
        assertEquals("https://mycompany.atlassian.net/browse/PROJ-123", url);
    }

    @Test
    void testJiraBuildIssueUrlWithTrailingSlash() {
        String url = IssueTracker.JIRA.buildIssueUrl("https://mycompany.atlassian.net/", "PROJ-123");
        assertEquals("https://mycompany.atlassian.net/browse/PROJ-123", url);
    }

    // ========== GitHub Issues Tests ==========

    @Test
    void testGitHubIssuesDisplayName() {
        assertEquals("GitHub Issues", IssueTracker.GITHUB_ISSUES.getDisplayName());
    }

    @Test
    void testGitHubIssuesBuildUrl() {
        String url = IssueTracker.GITHUB_ISSUES.buildIssueUrl("https://github.com/user/repo", "42");
        assertEquals("https://github.com/user/repo/issues/42", url);
    }

    @Test
    void testGitHubIssuesBuildUrlWithHash() {
        String url = IssueTracker.GITHUB_ISSUES.buildIssueUrl("https://github.com/user/repo", "#42");
        assertEquals("https://github.com/user/repo/issues/42", url);
    }

    // ========== GitLab Issues Tests ==========

    @Test
    void testGitLabIssuesDisplayName() {
        assertEquals("GitLab Issues", IssueTracker.GITLAB_ISSUES.getDisplayName());
    }

    @Test
    void testGitLabIssuesBuildUrl() {
        String url = IssueTracker.GITLAB_ISSUES.buildIssueUrl("https://gitlab.com/group/project", "42");
        assertEquals("https://gitlab.com/group/project/-/issues/42", url);
    }

    @Test
    void testGitLabIssuesBuildUrlWithHash() {
        String url = IssueTracker.GITLAB_ISSUES.buildIssueUrl("https://gitlab.com/group/project", "#42");
        assertEquals("https://gitlab.com/group/project/-/issues/42", url);
    }

    // ========== YouTrack Tests ==========

    @Test
    void testYouTrackDisplayName() {
        assertEquals("YouTrack", IssueTracker.YOUTRACK.getDisplayName());
    }

    @Test
    void testYouTrackBuildUrl() {
        String url = IssueTracker.YOUTRACK.buildIssueUrl("https://mycompany.youtrack.cloud", "PROJ-123");
        assertEquals("https://mycompany.youtrack.cloud/issue/PROJ-123", url);
    }

    // ========== Linear Tests ==========

    @Test
    void testLinearDisplayName() {
        assertEquals("Linear", IssueTracker.LINEAR.getDisplayName());
    }

    @Test
    void testLinearBuildUrl() {
        String url = IssueTracker.LINEAR.buildIssueUrl("https://linear.app/myteam", "ABC-123");
        assertEquals("https://linear.app/myteam/issue/ABC-123", url);
    }

    // ========== Azure Boards Tests ==========

    @Test
    void testAzureBoardsDisplayName() {
        assertEquals("Azure Boards", IssueTracker.AZURE_BOARDS.getDisplayName());
    }

    @Test
    void testAzureBoardsBuildUrl() {
        String url = IssueTracker.AZURE_BOARDS.buildIssueUrl("https://dev.azure.com/myorg/myproject", "123");
        assertEquals("https://dev.azure.com/myorg/myproject/_workitems/edit/123", url);
    }

    // ========== Bitbucket Issues Tests ==========

    @Test
    void testBitbucketIssuesDisplayName() {
        assertEquals("Bitbucket Issues", IssueTracker.BITBUCKET_ISSUES.getDisplayName());
    }

    @Test
    void testBitbucketIssuesBuildUrl() {
        String url = IssueTracker.BITBUCKET_ISSUES.buildIssueUrl("https://bitbucket.org/team/repo", "42");
        assertEquals("https://bitbucket.org/team/repo/issues/42", url);
    }

    // ========== Redmine Tests ==========

    @Test
    void testRedmineDisplayName() {
        assertEquals("Redmine", IssueTracker.REDMINE.getDisplayName());
    }

    @Test
    void testRedmineBuildUrl() {
        String url = IssueTracker.REDMINE.buildIssueUrl("https://redmine.mycompany.com", "123");
        assertEquals("https://redmine.mycompany.com/issues/123", url);
    }

    // ========== Trello Tests ==========

    @Test
    void testTrelloDisplayName() {
        assertEquals("Trello", IssueTracker.TRELLO.getDisplayName());
    }

    // ========== Asana Tests ==========

    @Test
    void testAsanaDisplayName() {
        assertEquals("Asana", IssueTracker.ASANA.getDisplayName());
    }

    // ========== None Tests ==========

    @Test
    void testNoneDisplayName() {
        assertEquals("None", IssueTracker.NONE.getDisplayName());
    }

    @Test
    void testNoneBuildUrlReturnsEmpty() {
        String url = IssueTracker.NONE.buildIssueUrl("https://example.com", "123");
        assertEquals("", url);
    }

    // ========== Edge Cases ==========

    @Test
    void testBuildIssueUrlWithNullBaseUrl() {
        String url = IssueTracker.JIRA.buildIssueUrl(null, "PROJ-123");
        assertEquals("", url);
    }

    @Test
    void testBuildIssueUrlWithEmptyBaseUrl() {
        String url = IssueTracker.JIRA.buildIssueUrl("", "PROJ-123");
        assertEquals("", url);
    }

    @Test
    void testBuildIssueUrlWithNullIssueId() {
        String url = IssueTracker.JIRA.buildIssueUrl("https://mycompany.atlassian.net", null);
        assertEquals("", url);
    }

    @Test
    void testBuildIssueUrlWithEmptyIssueId() {
        String url = IssueTracker.JIRA.buildIssueUrl("https://mycompany.atlassian.net", "");
        assertEquals("", url);
    }

    // ========== Auto-Detection Tests ==========

    @Test
    void testDetectFromUrlJiraAtlassian() {
        assertEquals(IssueTracker.JIRA, IssueTracker.detectFromUrl("https://mycompany.atlassian.net"));
    }

    @Test
    void testDetectFromUrlJiraSelfHosted() {
        assertEquals(IssueTracker.JIRA, IssueTracker.detectFromUrl("https://jira.mycompany.com"));
    }

    @Test
    void testDetectFromUrlGitHub() {
        assertEquals(IssueTracker.GITHUB_ISSUES, IssueTracker.detectFromUrl("https://github.com/user/repo"));
    }

    @Test
    void testDetectFromUrlGitLab() {
        assertEquals(IssueTracker.GITLAB_ISSUES, IssueTracker.detectFromUrl("https://gitlab.com/group/project"));
    }

    @Test
    void testDetectFromUrlGitLabSelfHosted() {
        assertEquals(IssueTracker.GITLAB_ISSUES, IssueTracker.detectFromUrl("https://gitlab.mycompany.com"));
    }

    @Test
    void testDetectFromUrlYouTrack() {
        assertEquals(IssueTracker.YOUTRACK, IssueTracker.detectFromUrl("https://mycompany.youtrack.cloud"));
    }

    @Test
    void testDetectFromUrlLinear() {
        assertEquals(IssueTracker.LINEAR, IssueTracker.detectFromUrl("https://linear.app/myteam"));
    }

    @Test
    void testDetectFromUrlAzureDevOps() {
        assertEquals(IssueTracker.AZURE_BOARDS, IssueTracker.detectFromUrl("https://dev.azure.com/myorg"));
    }

    @Test
    void testDetectFromUrlVisualStudio() {
        assertEquals(IssueTracker.AZURE_BOARDS, IssueTracker.detectFromUrl("https://myorg.visualstudio.com"));
    }

    @Test
    void testDetectFromUrlBitbucket() {
        assertEquals(IssueTracker.BITBUCKET_ISSUES, IssueTracker.detectFromUrl("https://bitbucket.org/team/repo"));
    }

    @Test
    void testDetectFromUrlRedmine() {
        assertEquals(IssueTracker.REDMINE, IssueTracker.detectFromUrl("https://redmine.mycompany.com"));
    }

    @Test
    void testDetectFromUrlTrello() {
        assertEquals(IssueTracker.TRELLO, IssueTracker.detectFromUrl("https://trello.com/b/abc123"));
    }

    @Test
    void testDetectFromUrlAsana() {
        assertEquals(IssueTracker.ASANA, IssueTracker.detectFromUrl("https://app.asana.com/0/123"));
    }

    @Test
    void testDetectFromUrlUnknown() {
        assertEquals(IssueTracker.NONE, IssueTracker.detectFromUrl("https://unknown.com"));
    }

    @Test
    void testDetectFromUrlNull() {
        assertEquals(IssueTracker.NONE, IssueTracker.detectFromUrl(null));
    }

    @Test
    void testDetectFromUrlEmpty() {
        assertEquals(IssueTracker.NONE, IssueTracker.detectFromUrl(""));
    }

    // ========== Uses Repo URL Tests ==========

    @Test
    void testUsesRepoUrlGitHubIssues() {
        assertTrue(IssueTracker.GITHUB_ISSUES.usesRepoUrl());
    }

    @Test
    void testUsesRepoUrlGitLabIssues() {
        assertTrue(IssueTracker.GITLAB_ISSUES.usesRepoUrl());
    }

    @Test
    void testUsesRepoUrlBitbucketIssues() {
        assertTrue(IssueTracker.BITBUCKET_ISSUES.usesRepoUrl());
    }

    @Test
    void testUsesRepoUrlJira() {
        assertFalse(IssueTracker.JIRA.usesRepoUrl());
    }

    @Test
    void testUsesRepoUrlYouTrack() {
        assertFalse(IssueTracker.YOUTRACK.usesRepoUrl());
    }

    // ========== All Trackers Exist Tests ==========

    @Test
    void testAllTrackersHaveDisplayName() {
        for (IssueTracker tracker : IssueTracker.values()) {
            assertThat(tracker.getDisplayName()).isNotNull().isNotEmpty();
        }
    }

    @Test
    void testAllTrackersHaveTermName() {
        for (IssueTracker tracker : IssueTracker.values()) {
            assertThat(tracker.getTermName()).isNotNull().isNotEmpty();
        }
    }

    @Test
    void testAllTrackersHaveExampleFormat() {
        for (IssueTracker tracker : IssueTracker.values()) {
            assertThat(tracker.getExampleFormat()).isNotNull().isNotEmpty();
        }
    }

    // ========== ToString Tests ==========

    @Test
    void testToString() {
        assertEquals("JIRA", IssueTracker.JIRA.toString());
        assertEquals("GitHub Issues", IssueTracker.GITHUB_ISSUES.toString());
    }
}
