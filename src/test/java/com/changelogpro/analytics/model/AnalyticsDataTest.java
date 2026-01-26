package com.changelogpro.analytics.model;

import com.changelogpro.config.ChangeType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AnalyticsData model.
 */
class AnalyticsDataTest {

    @Test
    void testBuilderCreatesValidData() {
        AnalyticsData data = AnalyticsData.builder()
                .projectName("TestProject")
                .currentVersion("1.0.0")
                .gitProvider("GitHub")
                .totalCommitCount(100)
                .build();

        assertEquals("TestProject", data.getProjectName());
        assertEquals("1.0.0", data.getCurrentVersion());
        assertEquals("GitHub", data.getGitProvider());
        assertEquals(100, data.getTotalCommitCount());
        assertNotNull(data.getGeneratedAt());
    }

    @Test
    void testReleaseCount() {
        ReleaseInfo unreleased = ReleaseInfo.builder()
                .version("Unreleased")
                .unreleased(true)
                .build();

        ReleaseInfo v1 = ReleaseInfo.builder()
                .version("1.0.0")
                .releaseDate(LocalDate.now())
                .build();

        ReleaseInfo v2 = ReleaseInfo.builder()
                .version("2.0.0")
                .releaseDate(LocalDate.now())
                .build();

        AnalyticsData data = AnalyticsData.builder()
                .addRelease(unreleased)
                .addRelease(v1)
                .addRelease(v2)
                .build();

        assertEquals(2, data.getReleaseCount());       // Excludes unreleased
        assertEquals(3, data.getReleases().size());    // Includes unreleased
    }

    @Test
    void testTotalEntryCount() {
        AnalyticsData data = AnalyticsData.builder()
                .addEntriesByType(ChangeType.ADDED, 10)
                .addEntriesByType(ChangeType.FIXED, 5)
                .addEntriesByType(ChangeType.CHANGED, 3)
                .build();

        assertEquals(18, data.getTotalEntryCount());
    }

    @Test
    void testAverageReleaseCycleDays() {
        ReleaseInfo r1 = ReleaseInfo.builder()
                .version("1.0.0")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .build();

        ReleaseInfo r2 = ReleaseInfo.builder()
                .version("1.1.0")
                .releaseDate(LocalDate.of(2024, 1, 31))
                .build();

        ReleaseInfo r3 = ReleaseInfo.builder()
                .version("1.2.0")
                .releaseDate(LocalDate.of(2024, 3, 1))
                .build();

        AnalyticsData data = AnalyticsData.builder()
                .addRelease(r1)
                .addRelease(r2)
                .addRelease(r3)
                .build();

        double avgCycle = data.getAverageReleaseCycleDays();
        // (30 + 30) / 2 = 30 days average
        assertEquals(30.0, avgCycle, 0.5);
    }

    @Test
    void testAverageReleaseCycleWithOnlyOneRelease() {
        ReleaseInfo r1 = ReleaseInfo.builder()
                .version("1.0.0")
                .releaseDate(LocalDate.now())
                .build();

        AnalyticsData data = AnalyticsData.builder()
                .addRelease(r1)
                .build();

        assertEquals(0.0, data.getAverageReleaseCycleDays(), 0.01);
    }

    @Test
    void testConventionalCommitPercentage() {
        AnalyticsData data = AnalyticsData.builder()
                .totalCommitCount(100)
                .conventionalCommitCount(75)
                .build();

        assertEquals(75.0, data.getConventionalCommitPercentage(), 0.01);
    }

    @Test
    void testConventionalCommitPercentageWithZeroCommits() {
        AnalyticsData data = AnalyticsData.builder()
                .totalCommitCount(0)
                .conventionalCommitCount(0)
                .build();

        assertEquals(0.0, data.getConventionalCommitPercentage(), 0.01);
    }

    @Test
    void testDocumentationCoverage() {
        AnalyticsData data = AnalyticsData.builder()
                .totalCommitCount(100)
                .undocumentedCommitCount(20)
                .build();

        assertEquals(80.0, data.getDocumentationCoverage(), 0.01);
    }

    @Test
    void testDocumentationCoverageWithZeroCommits() {
        AnalyticsData data = AnalyticsData.builder()
                .totalCommitCount(0)
                .undocumentedCommitCount(0)
                .build();

        assertEquals(100.0, data.getDocumentationCoverage(), 0.01);
    }

    @Test
    void testActiveContributorCount() {
        ContributorStats c1 = new ContributorStats("User1", "user1@example.com");
        ContributorStats c2 = new ContributorStats("User2", "user2@example.com");
        ContributorStats c3 = new ContributorStats("User3", "user3@example.com");

        AnalyticsData data = AnalyticsData.builder()
                .addContributor(c1)
                .addContributor(c2)
                .addContributor(c3)
                .build();

        assertEquals(3, data.getActiveContributorCount());
    }

    @Test
    void testBusFactor() {
        ContributorStats c1 = new ContributorStats("User1", null);
        c1.addCommit(LocalDate.now(), "feat");
        c1.addCommit(LocalDate.now(), "fix");
        c1.addCommit(LocalDate.now(), "feat");
        c1.addCommit(LocalDate.now(), "fix");
        c1.addCommit(LocalDate.now(), "feat");
        c1.addCommit(LocalDate.now(), "fix");
        c1.addCommit(LocalDate.now(), "feat");
        c1.addCommit(LocalDate.now(), "fix"); // 8 commits

        ContributorStats c2 = new ContributorStats("User2", null);
        c2.addCommit(LocalDate.now(), "fix");
        c2.addCommit(LocalDate.now(), "fix"); // 2 commits

        AnalyticsData data = AnalyticsData.builder()
                .totalCommitCount(10)
                .addContributor(c1)
                .addContributor(c2)
                .build();

        // User1 has 8 commits = 80% of total => bus factor 1
        assertEquals(1, data.getBusFactor());
    }

    @Test
    void testBusFactorWithEvenDistribution() {
        ContributorStats c1 = new ContributorStats("User1", null);
        ContributorStats c2 = new ContributorStats("User2", null);
        ContributorStats c3 = new ContributorStats("User3", null);
        ContributorStats c4 = new ContributorStats("User4", null);
        ContributorStats c5 = new ContributorStats("User5", null);

        // Each contributor has 20 commits
        for (int i = 0; i < 20; i++) {
            c1.addCommit(LocalDate.now(), "feat");
            c2.addCommit(LocalDate.now(), "feat");
            c3.addCommit(LocalDate.now(), "feat");
            c4.addCommit(LocalDate.now(), "feat");
            c5.addCommit(LocalDate.now(), "feat");
        }

        AnalyticsData data = AnalyticsData.builder()
                .totalCommitCount(100)
                .addContributor(c1)
                .addContributor(c2)
                .addContributor(c3)
                .addContributor(c4)
                .addContributor(c5)
                .build();

        // Need 4 contributors to reach 80%
        assertEquals(4, data.getBusFactor());
    }

    @Test
    void testBreakingChangeCount() {
        ReleaseInfo r1 = ReleaseInfo.builder()
                .version("1.0.0")
                .addBreakingChange("BC 1")
                .addBreakingChange("BC 2")
                .build();

        ReleaseInfo r2 = ReleaseInfo.builder()
                .version("2.0.0")
                .addBreakingChange("BC 3")
                .build();

        ReleaseInfo r3 = ReleaseInfo.builder()
                .version("2.1.0")
                .build();

        AnalyticsData data = AnalyticsData.builder()
                .addRelease(r1)
                .addRelease(r2)
                .addRelease(r3)
                .build();

        assertEquals(3, data.getBreakingChangeCount());
    }

    @Test
    void testLatestRelease() {
        ReleaseInfo unreleased = ReleaseInfo.builder()
                .version("Unreleased")
                .unreleased(true)
                .build();

        ReleaseInfo r1 = ReleaseInfo.builder()
                .version("1.0.0")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .build();

        ReleaseInfo r2 = ReleaseInfo.builder()
                .version("2.0.0")
                .releaseDate(LocalDate.of(2024, 6, 1))
                .build();

        AnalyticsData data = AnalyticsData.builder()
                .addRelease(unreleased)
                .addRelease(r1)
                .addRelease(r2)
                .build();

        ReleaseInfo latest = data.getLatestRelease();
        assertNotNull(latest);
        assertEquals("2.0.0", latest.getVersion());
    }

    @Test
    void testLatestReleaseWithNoReleases() {
        AnalyticsData data = AnalyticsData.builder().build();
        assertNull(data.getLatestRelease());
    }

    @Test
    void testChangeTypeDistribution() {
        AnalyticsData data = AnalyticsData.builder()
                .addEntriesByType(ChangeType.ADDED, 50)
                .addEntriesByType(ChangeType.FIXED, 30)
                .addEntriesByType(ChangeType.CHANGED, 20)
                .build();

        Map<ChangeType, Double> distribution = data.getChangeTypeDistribution();

        assertEquals(50.0, distribution.get(ChangeType.ADDED), 0.01);
        assertEquals(30.0, distribution.get(ChangeType.FIXED), 0.01);
        assertEquals(20.0, distribution.get(ChangeType.CHANGED), 0.01);
        assertEquals(0.0, distribution.get(ChangeType.REMOVED), 0.01);
    }

    @Test
    void testChangeTypeDistributionWithNoEntries() {
        AnalyticsData data = AnalyticsData.builder().build();

        Map<ChangeType, Double> distribution = data.getChangeTypeDistribution();

        for (ChangeType type : ChangeType.values()) {
            assertEquals(0.0, distribution.get(type), 0.01);
        }
    }

    @Test
    void testRecentCommits() {
        CommitInfo c1 = CommitInfo.builder()
                .hash("abc123")
                .author("User")
                .dateTime(LocalDateTime.now())
                .subject("Commit 1")
                .build();

        CommitInfo c2 = CommitInfo.builder()
                .hash("def456")
                .author("User")
                .dateTime(LocalDateTime.now())
                .subject("Commit 2")
                .build();

        AnalyticsData data = AnalyticsData.builder()
                .addRecentCommit(c1)
                .addRecentCommit(c2)
                .build();

        assertEquals(2, data.getRecentCommits().size());
    }

    @Test
    void testCommitsByMonth() {
        AnalyticsData data = AnalyticsData.builder()
                .addCommitsByMonth("2024-01", 10)
                .addCommitsByMonth("2024-02", 15)
                .addCommitsByMonth("2024-03", 20)
                .build();

        Map<String, Integer> byMonth = data.getCommitsByMonth();

        assertEquals(Integer.valueOf(10), byMonth.get("2024-01"));
        assertEquals(Integer.valueOf(15), byMonth.get("2024-02"));
        assertEquals(Integer.valueOf(20), byMonth.get("2024-03"));
    }

    @Test
    void testReleasesByMonth() {
        AnalyticsData data = AnalyticsData.builder()
                .addReleasesByMonth("2024-01", 1)
                .addReleasesByMonth("2024-03", 2)
                .build();

        Map<String, Integer> byMonth = data.getReleasesByMonth();

        assertEquals(Integer.valueOf(1), byMonth.get("2024-01"));
        assertEquals(Integer.valueOf(2), byMonth.get("2024-03"));
    }

    @Test
    void testHealthScore() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Test", "Description", 80, 100)
                .build();

        AnalyticsData data = AnalyticsData.builder()
                .healthScore(score)
                .build();

        assertNotNull(data.getHealthScore());
        assertEquals(80, data.getHealthScore().getTotalScore());
    }

    @Test
    void testToString() {
        ReleaseInfo r1 = ReleaseInfo.builder().version("1.0.0").build();
        ContributorStats c1 = new ContributorStats("User", null);

        AnalyticsData data = AnalyticsData.builder()
                .totalCommitCount(50)
                .addRelease(r1)
                .addContributor(c1)
                .build();

        String str = data.toString();
        assertTrue(str.contains("releases=1"));
        assertTrue(str.contains("commits=50"));
        assertTrue(str.contains("contributors=1"));
    }

    @Test
    void testReleasesAreSorted() {
        ReleaseInfo r1 = ReleaseInfo.builder()
                .version("1.0.0")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .build();

        ReleaseInfo r2 = ReleaseInfo.builder()
                .version("2.0.0")
                .releaseDate(LocalDate.of(2024, 6, 1))
                .build();

        ReleaseInfo unreleased = ReleaseInfo.builder()
                .version("Unreleased")
                .unreleased(true)
                .build();

        // Add in wrong order
        AnalyticsData data = AnalyticsData.builder()
                .addRelease(r1)
                .addRelease(r2)
                .addRelease(unreleased)
                .build();

        List<ReleaseInfo> releases = data.getReleases();

        // Should be sorted: Unreleased first, then newest to oldest
        assertTrue(releases.get(0).isUnreleased());
        assertEquals("2.0.0", releases.get(1).getVersion());
        assertEquals("1.0.0", releases.get(2).getVersion());
    }

    @Test
    void testContributorsAreSorted() {
        ContributorStats c1 = new ContributorStats("LessActive", null);
        c1.addCommit(LocalDate.now(), "feat");

        ContributorStats c2 = new ContributorStats("MoreActive", null);
        c2.addCommit(LocalDate.now(), "feat");
        c2.addCommit(LocalDate.now(), "feat");
        c2.addCommit(LocalDate.now(), "feat");

        // Add in wrong order
        AnalyticsData data = AnalyticsData.builder()
                .addContributor(c1)
                .addContributor(c2)
                .build();

        List<ContributorStats> contributors = data.getContributors();

        // Should be sorted by activity (most active first)
        assertEquals("MoreActive", contributors.get(0).getName());
        assertEquals("LessActive", contributors.get(1).getName());
    }

    @Test
    void testImmutableCollections() {
        ReleaseInfo r1 = ReleaseInfo.builder().version("1.0.0").build();

        AnalyticsData data = AnalyticsData.builder()
                .addRelease(r1)
                .build();

        assertThatThrownBy(() ->
                data.getReleases().add(ReleaseInfo.builder().version("2.0.0").build())
        ).isInstanceOf(UnsupportedOperationException.class);
    }
}
