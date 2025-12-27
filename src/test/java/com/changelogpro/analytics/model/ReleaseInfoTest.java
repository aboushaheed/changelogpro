package com.changelogpro.analytics.model;

import com.changelogpro.config.ChangeType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for ReleaseInfo model.
 */
class ReleaseInfoTest {

    // ========== Builder Tests ==========

    @Test
    void testBuildBasicRelease() {
        LocalDate date = LocalDate.of(2024, 6, 15);

        ReleaseInfo release = ReleaseInfo.builder()
                .version("1.0.0")
                .releaseDate(date)
                .tagName("v1.0.0")
                .build();

        assertThat(release.getVersion()).isEqualTo("1.0.0");
        assertThat(release.getReleaseDate()).isEqualTo(date);
        assertThat(release.getTagName()).isEqualTo("v1.0.0");
        assertThat(release.isUnreleased()).isFalse();
    }

    @Test
    void testBuildUnreleasedVersion() {
        ReleaseInfo release = ReleaseInfo.builder()
                .version("Unreleased")
                .unreleased(true)
                .build();

        assertThat(release.getVersion()).isEqualTo("Unreleased");
        assertThat(release.isUnreleased()).isTrue();
        assertThat(release.getReleaseDate()).isNull();
    }

    // ========== Entries Tests ==========

    @Test
    void testAddSingleEntry() {
        ReleaseInfo release = ReleaseInfo.builder()
                .version("1.0.0")
                .addEntry(ChangeType.ADDED, "New feature")
                .build();

        assertThat(release.getEntryCount(ChangeType.ADDED)).isEqualTo(1);
        assertThat(release.getEntriesByType().get(ChangeType.ADDED))
                .containsExactly("New feature");
    }

    @Test
    void testAddMultipleEntries() {
        ReleaseInfo release = ReleaseInfo.builder()
                .version("1.0.0")
                .addEntry(ChangeType.ADDED, "Feature 1")
                .addEntry(ChangeType.ADDED, "Feature 2")
                .addEntry(ChangeType.FIXED, "Bug fix")
                .build();

        assertThat(release.getEntryCount(ChangeType.ADDED)).isEqualTo(2);
        assertThat(release.getEntryCount(ChangeType.FIXED)).isEqualTo(1);
        assertThat(release.getTotalEntryCount()).isEqualTo(3);
    }

    @Test
    void testAddEntriesList() {
        List<String> features = Arrays.asList("Feature 1", "Feature 2", "Feature 3");

        ReleaseInfo release = ReleaseInfo.builder()
                .version("1.0.0")
                .addEntries(ChangeType.ADDED, features)
                .build();

        assertThat(release.getEntryCount(ChangeType.ADDED)).isEqualTo(3);
        assertThat(release.getEntriesByType().get(ChangeType.ADDED))
                .containsExactlyElementsOf(features);
    }

    @Test
    void testGetEntryCountForMissingType() {
        ReleaseInfo release = ReleaseInfo.builder()
                .version("1.0.0")
                .addEntry(ChangeType.ADDED, "Feature")
                .build();

        assertThat(release.getEntryCount(ChangeType.SECURITY)).isEqualTo(0);
    }

    // ========== Breaking Changes Tests ==========

    @Test
    void testAddBreakingChange() {
        ReleaseInfo release = ReleaseInfo.builder()
                .version("2.0.0")
                .addBreakingChange("API endpoint renamed")
                .build();

        assertThat(release.hasBreakingChanges()).isTrue();
        assertThat(release.getBreakingChanges()).containsExactly("API endpoint renamed");
    }

    @Test
    void testMultipleBreakingChanges() {
        ReleaseInfo release = ReleaseInfo.builder()
                .version("2.0.0")
                .addBreakingChange("Breaking change 1")
                .addBreakingChange("Breaking change 2")
                .build();

        assertThat(release.hasBreakingChanges()).isTrue();
        assertThat(release.getBreakingChanges()).hasSize(2);
    }

    @Test
    void testNoBreakingChanges() {
        ReleaseInfo release = ReleaseInfo.builder()
                .version("1.0.1")
                .addEntry(ChangeType.FIXED, "Minor fix")
                .build();

        assertThat(release.hasBreakingChanges()).isFalse();
        assertThat(release.getBreakingChanges()).isEmpty();
    }

    // ========== Summary Tests ==========

    @Test
    void testGetSummary() {
        ReleaseInfo release = ReleaseInfo.builder()
                .version("1.0.0")
                .addEntry(ChangeType.ADDED, "Feature 1")
                .addEntry(ChangeType.ADDED, "Feature 2")
                .addEntry(ChangeType.FIXED, "Bug fix")
                .build();

        String summary = release.getSummary();
        assertThat(summary).contains("2 Added");
        assertThat(summary).contains("1 Fixed");
    }

    @Test
    void testGetSummaryEmpty() {
        ReleaseInfo release = ReleaseInfo.builder()
                .version("1.0.0")
                .build();

        assertThat(release.getSummary()).isEmpty();
    }

    // ========== Comparison Tests ==========

    @Test
    void testCompareByDate() {
        ReleaseInfo older = ReleaseInfo.builder()
                .version("1.0.0")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .build();

        ReleaseInfo newer = ReleaseInfo.builder()
                .version("2.0.0")
                .releaseDate(LocalDate.of(2024, 6, 1))
                .build();

        // Newer should come first (negative comparison)
        assertThat(newer.compareTo(older)).isLessThan(0);
        assertThat(older.compareTo(newer)).isGreaterThan(0);
    }

    @Test
    void testUnreleasedComesFirst() {
        ReleaseInfo unreleased = ReleaseInfo.builder()
                .version("Unreleased")
                .unreleased(true)
                .build();

        ReleaseInfo released = ReleaseInfo.builder()
                .version("2.0.0")
                .releaseDate(LocalDate.now())
                .build();

        assertThat(unreleased.compareTo(released)).isLessThan(0);
        assertThat(released.compareTo(unreleased)).isGreaterThan(0);
    }

    @Test
    void testSortReleases() {
        ReleaseInfo v1 = ReleaseInfo.builder()
                .version("1.0.0")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .build();

        ReleaseInfo v2 = ReleaseInfo.builder()
                .version("2.0.0")
                .releaseDate(LocalDate.of(2024, 6, 1))
                .build();

        ReleaseInfo unreleased = ReleaseInfo.builder()
                .version("Unreleased")
                .unreleased(true)
                .build();

        List<ReleaseInfo> releases = Arrays.asList(v1, unreleased, v2);
        Collections.sort(releases);

        assertThat(releases.get(0).getVersion()).isEqualTo("Unreleased");
        assertThat(releases.get(1).getVersion()).isEqualTo("2.0.0");
        assertThat(releases.get(2).getVersion()).isEqualTo("1.0.0");
    }

    // ========== Version Comparison Tests ==========

    @Test
    void testCompareVersionsWithoutDates() {
        ReleaseInfo v1 = ReleaseInfo.builder()
                .version("1.0.0")
                .build();

        ReleaseInfo v2 = ReleaseInfo.builder()
                .version("2.0.0")
                .build();

        // When no dates, falls back to version string comparison
        assertThat(v2.compareTo(v1)).isLessThan(0);
    }

    @Test
    void testCompareSemanticVersions() {
        ReleaseInfo v1_0_0 = ReleaseInfo.builder().version("1.0.0").build();
        ReleaseInfo v1_0_1 = ReleaseInfo.builder().version("1.0.1").build();
        ReleaseInfo v1_1_0 = ReleaseInfo.builder().version("1.1.0").build();
        ReleaseInfo v2_0_0 = ReleaseInfo.builder().version("2.0.0").build();

        List<ReleaseInfo> releases = Arrays.asList(v1_0_0, v2_0_0, v1_0_1, v1_1_0);
        Collections.sort(releases);

        // Should be sorted newest first
        assertThat(releases.get(0).getVersion()).isEqualTo("2.0.0");
        assertThat(releases.get(1).getVersion()).isEqualTo("1.1.0");
        assertThat(releases.get(2).getVersion()).isEqualTo("1.0.1");
        assertThat(releases.get(3).getVersion()).isEqualTo("1.0.0");
    }

    // ========== Equality Tests ==========

    @Test
    void testEqualsWithSameVersion() {
        ReleaseInfo r1 = ReleaseInfo.builder()
                .version("1.0.0")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .addEntry(ChangeType.ADDED, "Feature")
                .build();

        ReleaseInfo r2 = ReleaseInfo.builder()
                .version("1.0.0")
                .releaseDate(LocalDate.of(2024, 6, 1)) // Different date
                .addEntry(ChangeType.FIXED, "Fix") // Different entries
                .build();

        // Equality based on version only
        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    void testNotEqualsWithDifferentVersion() {
        ReleaseInfo r1 = ReleaseInfo.builder().version("1.0.0").build();
        ReleaseInfo r2 = ReleaseInfo.builder().version("2.0.0").build();

        assertThat(r1).isNotEqualTo(r2);
    }

    // ========== ToString Tests ==========

    @Test
    void testToString() {
        ReleaseInfo release = ReleaseInfo.builder()
                .version("1.0.0")
                .releaseDate(LocalDate.of(2024, 6, 15))
                .addEntry(ChangeType.ADDED, "Feature 1")
                .addEntry(ChangeType.ADDED, "Feature 2")
                .addEntry(ChangeType.FIXED, "Bug fix")
                .build();

        String str = release.toString();
        assertThat(str).contains("1.0.0");
        assertThat(str).contains("2024-06-15");
        assertThat(str).contains("3 entries");
    }

    // ========== Immutability Tests ==========

    @Test
    void testEntriesByTypeIsImmutable() {
        ReleaseInfo release = ReleaseInfo.builder()
                .version("1.0.0")
                .addEntry(ChangeType.ADDED, "Feature")
                .build();

        assertThatThrownBy(() ->
                release.getEntriesByType().put(ChangeType.FIXED, Arrays.asList("Fix"))
        ).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testBreakingChangesIsImmutable() {
        ReleaseInfo release = ReleaseInfo.builder()
                .version("1.0.0")
                .addBreakingChange("Breaking")
                .build();

        assertThatThrownBy(() ->
                release.getBreakingChanges().add("Another")
        ).isInstanceOf(UnsupportedOperationException.class);
    }

    // ========== Validation Tests ==========

    @Test
    void testBuildWithoutVersion() {
        assertThrows(NullPointerException.class, () ->
                ReleaseInfo.builder()
                        .releaseDate(LocalDate.now())
                        .build()
        );
    }

    // ========== All Change Types Tests ==========

    @Test
    void testAllChangeTypes() {
        ReleaseInfo.Builder builder = ReleaseInfo.builder().version("1.0.0");

        for (ChangeType type : ChangeType.values()) {
            builder.addEntry(type, "Entry for " + type.name());
        }

        ReleaseInfo release = builder.build();

        assertThat(release.getTotalEntryCount()).isEqualTo(ChangeType.values().length);

        for (ChangeType type : ChangeType.values()) {
            assertThat(release.getEntryCount(type)).isEqualTo(1);
        }
    }
}
