package com.changelogpro.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GitProvider enum.
 */
class GitProviderTest {

    // ========== Display Name Tests ==========

    @Test
    void testGitHubDisplayName() {
        assertEquals("GitHub", GitProvider.GITHUB.getDisplayName());
    }

    @Test
    void testGitLabDisplayName() {
        assertEquals("GitLab", GitProvider.GITLAB.getDisplayName());
    }

    @Test
    void testBitbucketDisplayName() {
        assertEquals("Bitbucket", GitProvider.BITBUCKET.getDisplayName());
    }

    @Test
    void testAzureDevOpsDisplayName() {
        assertEquals("Azure DevOps", GitProvider.AZURE_DEVOPS.getDisplayName());
    }

    // ========== PR Term Tests ==========

    @Test
    void testGitHubPrTermFull() {
        assertEquals("Pull Request", GitProvider.GITHUB.getPrTermFull());
    }

    @Test
    void testGitLabPrTermFull() {
        assertEquals("Merge Request", GitProvider.GITLAB.getPrTermFull());
    }

    @Test
    void testGitHubPrTermShort() {
        assertEquals("PR", GitProvider.GITHUB.getPrTermShort());
    }

    @Test
    void testGitLabPrTermShort() {
        assertEquals("MR", GitProvider.GITLAB.getPrTermShort());
    }

    // ========== PR URL Building Tests ==========

    @Test
    void testBuildPrUrlGitHub() {
        String url = GitProvider.GITHUB.buildPrUrl("https://github.com/user/repo", "123");
        assertEquals("https://github.com/user/repo/pull/123", url);
    }

    @Test
    void testBuildPrUrlGitLab() {
        String url = GitProvider.GITLAB.buildPrUrl("https://gitlab.com/group/project", "456");
        assertEquals("https://gitlab.com/group/project/-/merge_requests/456", url);
    }

    @Test
    void testBuildPrUrlBitbucket() {
        String url = GitProvider.BITBUCKET.buildPrUrl("https://bitbucket.org/team/repo", "789");
        assertEquals("https://bitbucket.org/team/repo/pull-requests/789", url);
    }

    @Test
    void testBuildPrUrlAzureDevOps() {
        String url = GitProvider.AZURE_DEVOPS.buildPrUrl("https://dev.azure.com/org/project/_git/repo", "101");
        assertEquals("https://dev.azure.com/org/project/_git/repo/pullrequest/101", url);
    }

    @Test
    void testBuildPrUrlWithTrailingSlash() {
        String url = GitProvider.GITHUB.buildPrUrl("https://github.com/user/repo/", "123");
        assertEquals("https://github.com/user/repo/pull/123", url);
    }

    @Test
    void testBuildPrUrlWithNullRepoUrl() {
        String url = GitProvider.GITHUB.buildPrUrl(null, "123");
        assertEquals("", url);
    }

    @Test
    void testBuildPrUrlWithEmptyRepoUrl() {
        String url = GitProvider.GITHUB.buildPrUrl("", "123");
        assertEquals("", url);
    }

    @Test
    void testBuildPrUrlWithNullPrNumber() {
        String url = GitProvider.GITHUB.buildPrUrl("https://github.com/user/repo", null);
        assertEquals("", url);
    }

    @Test
    void testBuildPrUrlWithEmptyPrNumber() {
        String url = GitProvider.GITHUB.buildPrUrl("https://github.com/user/repo", "");
        assertEquals("", url);
    }

    // ========== Issue URL Building Tests ==========

    @Test
    void testBuildIssueUrlGitHub() {
        String url = GitProvider.GITHUB.buildIssueUrl("https://github.com/user/repo", "42");
        assertEquals("https://github.com/user/repo/issues/42", url);
    }

    @Test
    void testBuildIssueUrlGitLab() {
        String url = GitProvider.GITLAB.buildIssueUrl("https://gitlab.com/group/project", "42");
        assertEquals("https://gitlab.com/group/project/-/issues/42", url);
    }

    // ========== Auto-Detection Tests ==========

    @Test
    void testDetectFromUrlGitHub() {
        assertEquals(GitProvider.GITHUB, GitProvider.detectFromUrl("https://github.com/user/repo"));
    }

    @Test
    void testDetectFromUrlGitHubEnterprise() {
        assertEquals(GitProvider.GITHUB, GitProvider.detectFromUrl("https://github.mycompany.com/user/repo"));
    }

    @Test
    void testDetectFromUrlGitLab() {
        assertEquals(GitProvider.GITLAB, GitProvider.detectFromUrl("https://gitlab.com/group/project"));
    }

    @Test
    void testDetectFromUrlGitLabSelfHosted() {
        assertEquals(GitProvider.GITLAB, GitProvider.detectFromUrl("https://gitlab.mycompany.com/group/project"));
    }

    @Test
    void testDetectFromUrlBitbucket() {
        assertEquals(GitProvider.BITBUCKET, GitProvider.detectFromUrl("https://bitbucket.org/team/repo"));
    }

    @Test
    void testDetectFromUrlAzureDevOps() {
        assertEquals(GitProvider.AZURE_DEVOPS, GitProvider.detectFromUrl("https://dev.azure.com/org/project"));
    }

    @Test
    void testDetectFromUrlVisualStudio() {
        assertEquals(GitProvider.AZURE_DEVOPS, GitProvider.detectFromUrl("https://myorg.visualstudio.com/project"));
    }

    @Test
    void testDetectFromUrlGitea() {
        assertEquals(GitProvider.GITEA, GitProvider.detectFromUrl("https://gitea.myserver.com/user/repo"));
    }

    @Test
    void testDetectFromUrlGogs() {
        assertEquals(GitProvider.GOGS, GitProvider.detectFromUrl("https://gogs.myserver.com/user/repo"));
    }

    @Test
    void testDetectFromUrlUnknown() {
        assertEquals(GitProvider.CUSTOM, GitProvider.detectFromUrl("https://myserver.com/repo"));
    }

    @Test
    void testDetectFromUrlNull() {
        assertEquals(GitProvider.CUSTOM, GitProvider.detectFromUrl(null));
    }

    @Test
    void testDetectFromUrlEmpty() {
        assertEquals(GitProvider.CUSTOM, GitProvider.detectFromUrl(""));
    }

    @Test
    void testDetectFromUrlCaseInsensitive() {
        assertEquals(GitProvider.GITHUB, GitProvider.detectFromUrl("https://GITHUB.COM/user/repo"));
    }

    // ========== All Providers Exist Tests ==========

    @Test
    void testAllProvidersHaveDisplayName() {
        for (GitProvider provider : GitProvider.values()) {
            assertThat(provider.getDisplayName()).isNotNull().isNotEmpty();
        }
    }

    @Test
    void testAllProvidersHavePrTerms() {
        for (GitProvider provider : GitProvider.values()) {
            assertThat(provider.getPrTermFull()).isNotNull().isNotEmpty();
            assertThat(provider.getPrTermShort()).isNotNull().isNotEmpty();
        }
    }

    @Test
    void testAllProvidersHaveUrlPaths() {
        for (GitProvider provider : GitProvider.values()) {
            assertThat(provider.getPrUrlPath()).isNotNull();
            assertThat(provider.getIssueUrlPath()).isNotNull();
        }
    }

    // ========== ToString Tests ==========

    @Test
    void testToString() {
        assertEquals("GitHub", GitProvider.GITHUB.toString());
        assertEquals("GitLab", GitProvider.GITLAB.toString());
    }
}
