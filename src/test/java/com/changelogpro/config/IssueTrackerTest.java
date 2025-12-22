package com.changelogpro.config;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for IssueTracker enum.
 */
public class IssueTrackerTest {

    // ========== JIRA Tests ==========
    
    @Test
    public void testJiraDisplayName() {
        assertEquals("JIRA", IssueTracker.JIRA.getDisplayName());
    }

    @Test
    public void testJiraTermName() {
        assertEquals("Issue", IssueTracker.JIRA.getTermName());
    }

    @Test
    public void testJiraUrlPath() {
        assertEquals("/browse/", IssueTracker.JIRA.getUrlPath());
    }

    @Test
    public void testJiraExampleFormat() {
        assertEquals("PROJECT-123", IssueTracker.JIRA.getExampleFormat());
    }

    @Test
    public void testJiraBuildIssueUrl() {
        String url = IssueTracker.JIRA.buildIssueUrl("https://mycompany.atlassian.net", "PROJ-123");
        assertEquals("https://mycompany.atlassian.net/browse/PROJ-123", url);
    }

    @Test
    public void testJiraBuildIssueUrlWithTrailingSlash() {
        String url = IssueTracker.JIRA.buildIssueUrl("https://mycompany.atlassian.net/", "PROJ-123");
        assertEquals("https://mycompany.atlassian.net/browse/PROJ-123", url);
    }

    // ========== GitHub Issues Tests ==========
    
    @Test
    public void testGitHubIssuesDisplayName() {
        assertEquals("GitHub Issues", IssueTracker.GITHUB_ISSUES.getDisplayName());
    }

    @Test
    public void testGitHubIssuesBuildUrl() {
        String url = IssueTracker.GITHUB_ISSUES.buildIssueUrl("https://github.com/user/repo", "42");
        assertEquals("https://github.com/user/repo/issues/42", url);
    }

    @Test
    public void testGitHubIssuesBuildUrlWithHash() {
        String url = IssueTracker.GITHUB_ISSUES.buildIssueUrl("https://github.com/user/repo", "#42");
        assertEquals("https://github.com/user/repo/issues/42", url);
    }

    // ========== GitLab Issues Tests ==========
    
    @Test
    public void testGitLabIssuesDisplayName() {
        assertEquals("GitLab Issues", IssueTracker.GITLAB_ISSUES.getDisplayName());
    }

    @Test
    public void testGitLabIssuesBuildUrl() {
        String url = IssueTracker.GITLAB_ISSUES.buildIssueUrl("https://gitlab.com/group/project", "42");
        assertEquals("https://gitlab.com/group/project/-/issues/42", url);
    }

    @Test
    public void testGitLabIssuesBuildUrlWithHash() {
        String url = IssueTracker.GITLAB_ISSUES.buildIssueUrl("https://gitlab.com/group/project", "#42");
        assertEquals("https://gitlab.com/group/project/-/issues/42", url);
    }

    // ========== YouTrack Tests ==========
    
    @Test
    public void testYouTrackDisplayName() {
        assertEquals("YouTrack", IssueTracker.YOUTRACK.getDisplayName());
    }

    @Test
    public void testYouTrackBuildUrl() {
        String url = IssueTracker.YOUTRACK.buildIssueUrl("https://mycompany.youtrack.cloud", "PROJ-123");
        assertEquals("https://mycompany.youtrack.cloud/issue/PROJ-123", url);
    }

    // ========== Linear Tests ==========
    
    @Test
    public void testLinearDisplayName() {
        assertEquals("Linear", IssueTracker.LINEAR.getDisplayName());
    }

    @Test
    public void testLinearBuildUrl() {
        String url = IssueTracker.LINEAR.buildIssueUrl("https://linear.app/myteam", "ABC-123");
        assertEquals("https://linear.app/myteam/issue/ABC-123", url);
    }

    // ========== Azure Boards Tests ==========
    
    @Test
    public void testAzureBoardsDisplayName() {
        assertEquals("Azure Boards", IssueTracker.AZURE_BOARDS.getDisplayName());
    }

    @Test
    public void testAzureBoardsBuildUrl() {
        String url = IssueTracker.AZURE_BOARDS.buildIssueUrl("https://dev.azure.com/myorg/myproject", "123");
        assertEquals("https://dev.azure.com/myorg/myproject/_workitems/edit/123", url);
    }

    // ========== Bitbucket Issues Tests ==========
    
    @Test
    public void testBitbucketIssuesDisplayName() {
        assertEquals("Bitbucket Issues", IssueTracker.BITBUCKET_ISSUES.getDisplayName());
    }

    @Test
    public void testBitbucketIssuesBuildUrl() {
        String url = IssueTracker.BITBUCKET_ISSUES.buildIssueUrl("https://bitbucket.org/team/repo", "42");
        assertEquals("https://bitbucket.org/team/repo/issues/42", url);
    }

    // ========== Redmine Tests ==========
    
    @Test
    public void testRedmineDisplayName() {
        assertEquals("Redmine", IssueTracker.REDMINE.getDisplayName());
    }

    @Test
    public void testRedmineBuildUrl() {
        String url = IssueTracker.REDMINE.buildIssueUrl("https://redmine.mycompany.com", "123");
        assertEquals("https://redmine.mycompany.com/issues/123", url);
    }

    // ========== Trello Tests ==========
    
    @Test
    public void testTrelloDisplayName() {
        assertEquals("Trello", IssueTracker.TRELLO.getDisplayName());
    }

    // ========== Asana Tests ==========
    
    @Test
    public void testAsanaDisplayName() {
        assertEquals("Asana", IssueTracker.ASANA.getDisplayName());
    }

    // ========== None Tests ==========
    
    @Test
    public void testNoneDisplayName() {
        assertEquals("None", IssueTracker.NONE.getDisplayName());
    }

    @Test
    public void testNoneBuildUrlReturnsEmpty() {
        String url = IssueTracker.NONE.buildIssueUrl("https://example.com", "123");
        assertEquals("", url);
    }

    // ========== Edge Cases ==========
    
    @Test
    public void testBuildIssueUrlWithNullBaseUrl() {
        String url = IssueTracker.JIRA.buildIssueUrl(null, "PROJ-123");
        assertEquals("", url);
    }

    @Test
    public void testBuildIssueUrlWithEmptyBaseUrl() {
        String url = IssueTracker.JIRA.buildIssueUrl("", "PROJ-123");
        assertEquals("", url);
    }

    @Test
    public void testBuildIssueUrlWithNullIssueId() {
        String url = IssueTracker.JIRA.buildIssueUrl("https://mycompany.atlassian.net", null);
        assertEquals("", url);
    }

    @Test
    public void testBuildIssueUrlWithEmptyIssueId() {
        String url = IssueTracker.JIRA.buildIssueUrl("https://mycompany.atlassian.net", "");
        assertEquals("", url);
    }

    // ========== Auto-Detection Tests ==========
    
    @Test
    public void testDetectFromUrlJiraAtlassian() {
        assertEquals(IssueTracker.JIRA, IssueTracker.detectFromUrl("https://mycompany.atlassian.net"));
    }

    @Test
    public void testDetectFromUrlJiraSelfHosted() {
        assertEquals(IssueTracker.JIRA, IssueTracker.detectFromUrl("https://jira.mycompany.com"));
    }

    @Test
    public void testDetectFromUrlGitHub() {
        assertEquals(IssueTracker.GITHUB_ISSUES, IssueTracker.detectFromUrl("https://github.com/user/repo"));
    }

    @Test
    public void testDetectFromUrlGitLab() {
        assertEquals(IssueTracker.GITLAB_ISSUES, IssueTracker.detectFromUrl("https://gitlab.com/group/project"));
    }

    @Test
    public void testDetectFromUrlGitLabSelfHosted() {
        assertEquals(IssueTracker.GITLAB_ISSUES, IssueTracker.detectFromUrl("https://gitlab.mycompany.com"));
    }

    @Test
    public void testDetectFromUrlYouTrack() {
        assertEquals(IssueTracker.YOUTRACK, IssueTracker.detectFromUrl("https://mycompany.youtrack.cloud"));
    }

    @Test
    public void testDetectFromUrlLinear() {
        assertEquals(IssueTracker.LINEAR, IssueTracker.detectFromUrl("https://linear.app/myteam"));
    }

    @Test
    public void testDetectFromUrlAzureDevOps() {
        assertEquals(IssueTracker.AZURE_BOARDS, IssueTracker.detectFromUrl("https://dev.azure.com/myorg"));
    }

    @Test
    public void testDetectFromUrlVisualStudio() {
        assertEquals(IssueTracker.AZURE_BOARDS, IssueTracker.detectFromUrl("https://myorg.visualstudio.com"));
    }

    @Test
    public void testDetectFromUrlBitbucket() {
        assertEquals(IssueTracker.BITBUCKET_ISSUES, IssueTracker.detectFromUrl("https://bitbucket.org/team/repo"));
    }

    @Test
    public void testDetectFromUrlRedmine() {
        assertEquals(IssueTracker.REDMINE, IssueTracker.detectFromUrl("https://redmine.mycompany.com"));
    }

    @Test
    public void testDetectFromUrlTrello() {
        assertEquals(IssueTracker.TRELLO, IssueTracker.detectFromUrl("https://trello.com/b/abc123"));
    }

    @Test
    public void testDetectFromUrlAsana() {
        assertEquals(IssueTracker.ASANA, IssueTracker.detectFromUrl("https://app.asana.com/0/123"));
    }

    @Test
    public void testDetectFromUrlUnknown() {
        assertEquals(IssueTracker.NONE, IssueTracker.detectFromUrl("https://unknown.com"));
    }

    @Test
    public void testDetectFromUrlNull() {
        assertEquals(IssueTracker.NONE, IssueTracker.detectFromUrl(null));
    }

    @Test
    public void testDetectFromUrlEmpty() {
        assertEquals(IssueTracker.NONE, IssueTracker.detectFromUrl(""));
    }

    // ========== Uses Repo URL Tests ==========
    
    @Test
    public void testUsesRepoUrlGitHubIssues() {
        assertTrue(IssueTracker.GITHUB_ISSUES.usesRepoUrl());
    }

    @Test
    public void testUsesRepoUrlGitLabIssues() {
        assertTrue(IssueTracker.GITLAB_ISSUES.usesRepoUrl());
    }

    @Test
    public void testUsesRepoUrlBitbucketIssues() {
        assertTrue(IssueTracker.BITBUCKET_ISSUES.usesRepoUrl());
    }

    @Test
    public void testUsesRepoUrlJira() {
        assertFalse(IssueTracker.JIRA.usesRepoUrl());
    }

    @Test
    public void testUsesRepoUrlYouTrack() {
        assertFalse(IssueTracker.YOUTRACK.usesRepoUrl());
    }

    // ========== All Trackers Exist Tests ==========
    
    @Test
    public void testAllTrackersHaveDisplayName() {
        for (IssueTracker tracker : IssueTracker.values()) {
            assertThat(tracker.getDisplayName()).isNotNull().isNotEmpty();
        }
    }

    @Test
    public void testAllTrackersHaveTermName() {
        for (IssueTracker tracker : IssueTracker.values()) {
            assertThat(tracker.getTermName()).isNotNull().isNotEmpty();
        }
    }

    @Test
    public void testAllTrackersHaveExampleFormat() {
        for (IssueTracker tracker : IssueTracker.values()) {
            assertThat(tracker.getExampleFormat()).isNotNull().isNotEmpty();
        }
    }

    // ========== ToString Tests ==========
    
    @Test
    public void testToString() {
        assertEquals("JIRA", IssueTracker.JIRA.toString());
        assertEquals("GitHub Issues", IssueTracker.GITHUB_ISSUES.toString());
    }
}
