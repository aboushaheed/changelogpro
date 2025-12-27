package com.changelogpro.analytics.model;

import com.changelogpro.config.ChangeType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ContributorStats model.
 */
class ContributorStatsTest {

    // ========== Constructor Tests ==========

    @Test
    void testCreateContributor() {
        ContributorStats stats = new ContributorStats("John Doe", "john@example.com");

        assertThat(stats.getName()).isEqualTo("John Doe");
        assertThat(stats.getEmail()).isEqualTo("john@example.com");
        assertThat(stats.getCommitCount()).isEqualTo(0);
        assertThat(stats.getChangelogEntryCount()).isEqualTo(0);
    }

    @Test
    void testCreateContributorWithoutEmail() {
        ContributorStats stats = new ContributorStats("John Doe", null);

        assertThat(stats.getName()).isEqualTo("John Doe");
        assertThat(stats.getEmail()).isNull();
    }

    // ========== Add Commit Tests ==========

    @Test
    void testAddSingleCommit() {
        ContributorStats stats = new ContributorStats("John", "john@example.com");
        LocalDate today = LocalDate.now();

        stats.addCommit(today, "feat");

        assertThat(stats.getCommitCount()).isEqualTo(1);
        assertThat(stats.getFirstContribution()).isEqualTo(today);
        assertThat(stats.getLastContribution()).isEqualTo(today);
    }

    @Test
    void testAddMultipleCommits() {
        ContributorStats stats = new ContributorStats("John", "john@example.com");
        LocalDate day1 = LocalDate.of(2024, 1, 1);
        LocalDate day2 = LocalDate.of(2024, 1, 15);
        LocalDate day3 = LocalDate.of(2024, 2, 1);

        stats.addCommit(day1, "feat");
        stats.addCommit(day2, "fix");
        stats.addCommit(day3, "feat");

        assertThat(stats.getCommitCount()).isEqualTo(3);
        assertThat(stats.getFirstContribution()).isEqualTo(day1);
        assertThat(stats.getLastContribution()).isEqualTo(day3);
    }

    @Test
    void testCommitsOnSameDay() {
        ContributorStats stats = new ContributorStats("John", "john@example.com");
        LocalDate today = LocalDate.now();

        stats.addCommit(today, "feat");
        stats.addCommit(today, "fix");
        stats.addCommit(today, "feat");

        assertThat(stats.getCommitCount()).isEqualTo(3);
        assertThat(stats.getActiveDays()).isEqualTo(1);
        assertThat(stats.getActivityByDate().get(today)).isEqualTo(3);
    }

    @Test
    void testAddCommitWithoutConventionalType() {
        ContributorStats stats = new ContributorStats("John", "john@example.com");

        stats.addCommit(LocalDate.now(), null);

        assertThat(stats.getCommitCount()).isEqualTo(1);
        assertThat(stats.getConventionalTypeCount()).isEmpty();
    }

    // ========== Conventional Type Tracking ==========

    @Test
    void testConventionalTypeCount() {
        ContributorStats stats = new ContributorStats("John", "john@example.com");
        LocalDate today = LocalDate.now();

        stats.addCommit(today, "feat");
        stats.addCommit(today, "feat");
        stats.addCommit(today, "fix");
        stats.addCommit(today, "chore");

        assertThat(stats.getConventionalTypeCount().get("feat")).isEqualTo(2);
        assertThat(stats.getConventionalTypeCount().get("fix")).isEqualTo(1);
        assertThat(stats.getConventionalTypeCount().get("chore")).isEqualTo(1);
    }

    @Test
    void testPrimaryConventionalType() {
        ContributorStats stats = new ContributorStats("John", "john@example.com");
        LocalDate today = LocalDate.now();

        stats.addCommit(today, "fix");
        stats.addCommit(today, "feat");
        stats.addCommit(today, "feat");
        stats.addCommit(today, "feat");
        stats.addCommit(today, "fix");

        assertThat(stats.getPrimaryConventionalType()).isEqualTo("feat");
    }

    @Test
    void testPrimaryConventionalTypeWhenEmpty() {
        ContributorStats stats = new ContributorStats("John", "john@example.com");

        assertThat(stats.getPrimaryConventionalType()).isNull();
    }

    @Test
    void testConventionalCommitPercentage() {
        ContributorStats stats = new ContributorStats("John", "john@example.com");
        LocalDate today = LocalDate.now();

        stats.addCommit(today, "feat");
        stats.addCommit(today, "fix");
        stats.addCommit(today, null); // Non-conventional
        stats.addCommit(today, null); // Non-conventional

        assertThat(stats.getConventionalCommitPercentage()).isEqualTo(50.0);
    }

    @Test
    void testConventionalCommitPercentageNoCommits() {
        ContributorStats stats = new ContributorStats("John", "john@example.com");

        assertThat(stats.getConventionalCommitPercentage()).isEqualTo(0.0);
    }

    // ========== Changelog Entry Tests ==========

    @Test
    void testAddChangelogEntry() {
        ContributorStats stats = new ContributorStats("John", "john@example.com");

        stats.addChangelogEntry(ChangeType.ADDED);
        stats.addChangelogEntry(ChangeType.ADDED);
        stats.addChangelogEntry(ChangeType.FIXED);

        assertThat(stats.getChangelogEntryCount()).isEqualTo(3);
        assertThat(stats.getEntryCount(ChangeType.ADDED)).isEqualTo(2);
        assertThat(stats.getEntryCount(ChangeType.FIXED)).isEqualTo(1);
    }

    @Test
    void testGetEntryCountForMissingType() {
        ContributorStats stats = new ContributorStats("John", "john@example.com");
        stats.addChangelogEntry(ChangeType.ADDED);

        assertThat(stats.getEntryCount(ChangeType.SECURITY)).isEqualTo(0);
    }

    // ========== Activity Tests ==========

    @Test
    void testActiveDays() {
        ContributorStats stats = new ContributorStats("John", "john@example.com");

        stats.addCommit(LocalDate.of(2024, 1, 1), "feat");
        stats.addCommit(LocalDate.of(2024, 1, 1), "fix");
        stats.addCommit(LocalDate.of(2024, 1, 2), "chore");
        stats.addCommit(LocalDate.of(2024, 1, 5), "feat");

        assertThat(stats.getActiveDays()).isEqualTo(3);
    }

    @Test
    void testWeeklyPattern() {
        ContributorStats stats = new ContributorStats("John", "john@example.com");

        // Monday = 2024-01-01, Tuesday = 2024-01-02, etc.
        stats.addCommit(LocalDate.of(2024, 1, 1), "feat"); // Monday
        stats.addCommit(LocalDate.of(2024, 1, 1), "fix");  // Monday
        stats.addCommit(LocalDate.of(2024, 1, 3), "feat"); // Wednesday
        stats.addCommit(LocalDate.of(2024, 1, 5), "fix");  // Friday

        int[] pattern = stats.getWeeklyPattern();

        assertThat(pattern).hasSize(7);
        assertThat(pattern[0]).isEqualTo(2); // Monday
        assertThat(pattern[1]).isEqualTo(0); // Tuesday
        assertThat(pattern[2]).isEqualTo(1); // Wednesday
        assertThat(pattern[3]).isEqualTo(0); // Thursday
        assertThat(pattern[4]).isEqualTo(1); // Friday
        assertThat(pattern[5]).isEqualTo(0); // Saturday
        assertThat(pattern[6]).isEqualTo(0); // Sunday
    }

    // ========== First/Last Contribution Tracking ==========

    @Test
    void testFirstLastContributionWithOutOfOrderCommits() {
        ContributorStats stats = new ContributorStats("John", "john@example.com");

        // Add commits out of chronological order
        stats.addCommit(LocalDate.of(2024, 6, 15), "feat");
        stats.addCommit(LocalDate.of(2024, 1, 1), "fix");        // Earlier
        stats.addCommit(LocalDate.of(2024, 12, 31), "chore");    // Later
        stats.addCommit(LocalDate.of(2024, 3, 15), "feat");

        assertThat(stats.getFirstContribution()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(stats.getLastContribution()).isEqualTo(LocalDate.of(2024, 12, 31));
    }

    // ========== Comparison Tests ==========

    @Test
    void testCompareByTotalContribution() {
        ContributorStats active = new ContributorStats("Active", "active@example.com");
        active.addCommit(LocalDate.now(), "feat");
        active.addCommit(LocalDate.now(), "fix");
        active.addCommit(LocalDate.now(), "feat");
        active.addChangelogEntry(ChangeType.ADDED);
        active.addChangelogEntry(ChangeType.FIXED);

        ContributorStats lessActive = new ContributorStats("Less Active", "less@example.com");
        lessActive.addCommit(LocalDate.now(), "fix");

        // More active should come first (negative comparison)
        assertThat(active.compareTo(lessActive)).isLessThan(0);
        assertThat(lessActive.compareTo(active)).isGreaterThan(0);
    }

    @Test
    void testSortContributors() {
        ContributorStats c1 = new ContributorStats("User 1", null);
        c1.addCommit(LocalDate.now(), "feat");

        ContributorStats c2 = new ContributorStats("User 2", null);
        c2.addCommit(LocalDate.now(), "feat");
        c2.addCommit(LocalDate.now(), "fix");
        c2.addCommit(LocalDate.now(), "feat");

        ContributorStats c3 = new ContributorStats("User 3", null);
        c3.addCommit(LocalDate.now(), "fix");
        c3.addCommit(LocalDate.now(), "fix");

        List<ContributorStats> contributors = Arrays.asList(c1, c2, c3);
        Collections.sort(contributors);

        // Should be sorted by total contribution (most first)
        assertThat(contributors.get(0).getName()).isEqualTo("User 2"); // 3 commits
        assertThat(contributors.get(1).getName()).isEqualTo("User 3"); // 2 commits
        assertThat(contributors.get(2).getName()).isEqualTo("User 1"); // 1 commit
    }

    // ========== Equality Tests ==========

    @Test
    void testEqualsWithSameNameAndEmail() {
        ContributorStats c1 = new ContributorStats("John", "john@example.com");
        c1.addCommit(LocalDate.now(), "feat");

        ContributorStats c2 = new ContributorStats("John", "john@example.com");
        // No commits added

        assertThat(c1).isEqualTo(c2);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }

    @Test
    void testNotEqualsWithDifferentName() {
        ContributorStats c1 = new ContributorStats("John", "john@example.com");
        ContributorStats c2 = new ContributorStats("Jane", "john@example.com");

        assertThat(c1).isNotEqualTo(c2);
    }

    @Test
    void testNotEqualsWithDifferentEmail() {
        ContributorStats c1 = new ContributorStats("John", "john@example.com");
        ContributorStats c2 = new ContributorStats("John", "johnny@example.com");

        assertThat(c1).isNotEqualTo(c2);
    }

    // ========== ToString Tests ==========

    @Test
    void testToString() {
        ContributorStats stats = new ContributorStats("John Doe", "john@example.com");
        stats.addCommit(LocalDate.now(), "feat");
        stats.addCommit(LocalDate.now(), "fix");
        stats.addChangelogEntry(ChangeType.ADDED);

        String str = stats.toString();
        assertThat(str).contains("John Doe");
        assertThat(str).contains("2 commits");
        assertThat(str).contains("1 entries");
    }

    // ========== Immutability Tests ==========

    @Test
    void testEntriesByTypeIsImmutable() {
        ContributorStats stats = new ContributorStats("John", "john@example.com");
        stats.addChangelogEntry(ChangeType.ADDED);

        assertThatThrownBy(() ->
                stats.getEntriesByType().put(ChangeType.FIXED, 5)
        ).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testActivityByDateIsImmutable() {
        ContributorStats stats = new ContributorStats("John", "john@example.com");
        stats.addCommit(LocalDate.now(), "feat");

        assertThatThrownBy(() ->
                stats.getActivityByDate().put(LocalDate.now().plusDays(1), 10)
        ).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testConventionalTypeCountIsImmutable() {
        ContributorStats stats = new ContributorStats("John", "john@example.com");
        stats.addCommit(LocalDate.now(), "feat");

        assertThatThrownBy(() ->
                stats.getConventionalTypeCount().put("fix", 10)
        ).isInstanceOf(UnsupportedOperationException.class);
    }
}
