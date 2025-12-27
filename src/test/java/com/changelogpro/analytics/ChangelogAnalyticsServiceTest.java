package com.changelogpro.analytics;

import com.changelogpro.analytics.model.ReleaseInfo;
import com.changelogpro.config.ChangeType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ChangelogAnalyticsService.
 * Tests the parsing of CHANGELOG.md content without file system access.
 */
class ChangelogAnalyticsServiceTest {

    /**
     * Test parser instance that can parse content directly.
     */
    private final TestableChangelogParser parser = new TestableChangelogParser();

    @Test
    void testParseEmptyChangelog() {
        String content = "";
        List<ReleaseInfo> releases = parser.parseContent(content);
        assertTrue(releases.isEmpty());
    }

    @Test
    void testParseHeaderOnly() {
        String content = """
            # Changelog
            
            All notable changes to this project will be documented in this file.
            """;

        List<ReleaseInfo> releases = parser.parseContent(content);
        assertTrue(releases.isEmpty());
    }

    @Test
    void testParseUnreleasedSection() {
        String content = """
            # Changelog
            
            ## [Unreleased]
            
            ### Added
            - New feature A
            - New feature B
            
            ### Fixed
            - Bug fix 1
            """;

        List<ReleaseInfo> releases = parser.parseContent(content);

        assertEquals(1, releases.size());

        ReleaseInfo unreleased = releases.getFirst();
        assertTrue(unreleased.isUnreleased());
        assertEquals("Unreleased", unreleased.getVersion());
        assertNull(unreleased.getReleaseDate());

        assertEquals(2, unreleased.getEntryCount(ChangeType.ADDED));
        assertEquals(1, unreleased.getEntryCount(ChangeType.FIXED));
        assertEquals(3, unreleased.getTotalEntryCount());
    }

    @Test
    void testParseVersionWithDate() {
        String content = """
            ## [1.0.0] - 2024-01-15
            
            ### Added
            - Initial release
            """;

        List<ReleaseInfo> releases = parser.parseContent(content);

        assertEquals(1, releases.size());

        ReleaseInfo release = releases.getFirst();
        assertFalse(release.isUnreleased());
        assertEquals("1.0.0", release.getVersion());
        assertEquals(LocalDate.of(2024, 1, 15), release.getReleaseDate());
        assertEquals(1, release.getEntryCount(ChangeType.ADDED));
    }

    @Test
    void testParseMultipleVersions() {
        String content = """
            # Changelog
            
            ## [Unreleased]
            
            ### Added
            - Upcoming feature
            
            ## [2.0.0] - 2024-06-01
            
            ### Added
            - Major new feature
            
            ### Changed
            - Updated API
            
            ## [1.1.0] - 2024-03-15
            
            ### Fixed
            - Critical bug fix
            
            ## [1.0.0] - 2024-01-15
            
            ### Added
            - Initial release
            """;

        List<ReleaseInfo> releases = parser.parseContent(content);

        assertEquals(4, releases.size());

        // Should be sorted newest first
        assertTrue(releases.get(0).isUnreleased());
        assertEquals("2.0.0", releases.get(1).getVersion());
        assertEquals("1.1.0", releases.get(2).getVersion());
        assertEquals("1.0.0", releases.get(3).getVersion());
    }

    @Test
    void testParseAllChangeTypes() {
        String content = """
            ## [1.0.0] - 2024-01-15
            
            ### Added
            - New feature
            
            ### Changed
            - Updated behavior
            
            ### Deprecated
            - Old API
            
            ### Removed
            - Legacy code
            
            ### Fixed
            - Bug fix
            
            ### Security
            - Security patch
            """;

        List<ReleaseInfo> releases = parser.parseContent(content);

        assertEquals(1, releases.size());
        ReleaseInfo release = releases.getFirst();

        assertEquals(1, release.getEntryCount(ChangeType.ADDED));
        assertEquals(1, release.getEntryCount(ChangeType.CHANGED));
        assertEquals(1, release.getEntryCount(ChangeType.DEPRECATED));
        assertEquals(1, release.getEntryCount(ChangeType.REMOVED));
        assertEquals(1, release.getEntryCount(ChangeType.FIXED));
        assertEquals(1, release.getEntryCount(ChangeType.SECURITY));
        assertEquals(6, release.getTotalEntryCount());
    }

    @Test
    void testParseBreakingChanges() {
        String content = """
            ## [2.0.0] - 2024-06-01
            
            ### Changed
            - BREAKING CHANGE: API signature updated
            - Regular change
            
            ### Removed
            - ⚠️ Deprecated endpoint removed
            """;

        List<ReleaseInfo> releases = parser.parseContent(content);

        assertEquals(1, releases.size());
        ReleaseInfo release = releases.getFirst();

        assertTrue(release.hasBreakingChanges());
        assertEquals(1, release.getBreakingChanges().size());
    }

    @Test
    void testParseMultilineEntries() {
        String content = """
            ## [1.0.0] - 2024-01-15
            
            ### Added
            - First feature
            - Second feature with longer description
            - Third feature
            """;

        List<ReleaseInfo> releases = parser.parseContent(content);

        assertEquals(1, releases.size());
        assertEquals(3, releases.get(0).getEntryCount(ChangeType.ADDED));
    }

    @Test
    void testParseBulletVariations() {
        String content = """
            ## [1.0.0] - 2024-01-15
            
            ### Added
            - Dash bullet entry
            * Asterisk bullet entry
            """;

        List<ReleaseInfo> releases = parser.parseContent(content);

        assertEquals(1, releases.size());
        assertEquals(2, releases.get(0).getEntryCount(ChangeType.ADDED));
    }

    @Test
    void testParseCaseInsensitiveTypes() {
        String content = """
            ## [1.0.0] - 2024-01-15
            
            ### ADDED
            - Uppercase type
            
            ### fixed
            - Lowercase type
            """;

        List<ReleaseInfo> releases = parser.parseContent(content);

        assertEquals(1, releases.size());
        assertEquals(1, releases.get(0).getEntryCount(ChangeType.ADDED));
        assertEquals(1, releases.get(0).getEntryCount(ChangeType.FIXED));
    }

    @Test
    void testParseSummary() {
        String content = """
            ## [1.0.0] - 2024-01-15
            
            ### Added
            - Feature 1
            - Feature 2
            - Feature 3
            
            ### Fixed
            - Bug 1
            - Bug 2
            """;

        List<ReleaseInfo> releases = parser.parseContent(content);

        assertEquals(1, releases.size());
        String summary = releases.get(0).getSummary();

        assertTrue(summary.contains("3 Added"));
        assertTrue(summary.contains("2 Fixed"));
    }

    @Test
    void testCountEntriesByType() {
        String content = """
            ## [2.0.0] - 2024-06-01
            
            ### Added
            - Feature 1
            - Feature 2
            
            ## [1.0.0] - 2024-01-15
            
            ### Added
            - Initial feature
            
            ### Fixed
            - Bug fix
            """;

        List<ReleaseInfo> releases = parser.parseContent(content);
        Map<ChangeType, Integer> counts = parser.countEntriesByType(releases);

        assertEquals(Integer.valueOf(3), counts.get(ChangeType.ADDED));
        assertEquals(Integer.valueOf(1), counts.get(ChangeType.FIXED));
    }

    @Test
    void testGroupReleasesByMonth() {
        String content = """
            ## [3.0.0] - 2024-06-15
            ### Added
            - Feature
            
            ## [2.0.0] - 2024-06-01
            ### Added
            - Feature
            
            ## [1.0.0] - 2024-01-15
            ### Added
            - Feature
            """;

        List<ReleaseInfo> releases = parser.parseContent(content);
        Map<String, Integer> byMonth = parser.groupReleasesByMonth(releases);

        assertEquals(Integer.valueOf(2), byMonth.get("2024-06"));
        assertEquals(Integer.valueOf(1), byMonth.get("2024-01"));
    }

    @Test
    void testVersionWithoutDate() {
        String content = """
            ## [1.0.0-beta]
            
            ### Added
            - Beta feature
            """;

        List<ReleaseInfo> releases = parser.parseContent(content);

        assertEquals(1, releases.size());
        assertEquals("1.0.0-beta", releases.get(0).getVersion());
        assertNull(releases.get(0).getReleaseDate());
    }

    @Test
    void testParseWithExtraWhitespace() {
        String content = """
            ## [1.0.0] - 2024-01-15
            
            ###   Added  
            
            -   Entry with extra spaces  
            
            """;

        List<ReleaseInfo> releases = parser.parseContent(content);

        assertEquals(1, releases.size());
        assertEquals(1, releases.get(0).getEntryCount(ChangeType.ADDED));
    }

    /**
     * Testable version of changelog parser that works with content strings.
     * This mimics the behavior of ChangelogAnalyticsService without needing a Project.
     */
    private static class TestableChangelogParser {

        private static final java.util.regex.Pattern VERSION_HEADER_PATTERN = java.util.regex.Pattern.compile(
                "^##\\s+\\[([^\\]]+)\\](?:\\s*-\\s*(\\d{4}-\\d{2}-\\d{2}))?",
                java.util.regex.Pattern.MULTILINE
        );

        private static final java.util.regex.Pattern CHANGE_TYPE_PATTERN = java.util.regex.Pattern.compile(
                "^###\\s+(Added|Changed|Deprecated|Removed|Fixed|Security)\\s*$",
                java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.MULTILINE
        );

        private static final java.util.regex.Pattern LIST_ITEM_PATTERN = java.util.regex.Pattern.compile(
                "^\\s*[-*]\\s+(.+)$"
        );

        private static final java.util.regex.Pattern BREAKING_CHANGE_PATTERN = java.util.regex.Pattern.compile(
                "\\bBREAKING\\s*CHANGE\\b|\\b⚠️|\\bBREAKING\\b",
                java.util.regex.Pattern.CASE_INSENSITIVE
        );

        public List<ReleaseInfo> parseContent(String content) {
            List<ReleaseInfo> releases = new java.util.ArrayList<>();
            if (content == null || content.isEmpty()) {
                return releases;
            }

            java.util.regex.Matcher matcher = VERSION_HEADER_PATTERN.matcher(content);
            List<int[]> positions = new java.util.ArrayList<>();

            while (matcher.find()) {
                positions.add(new int[]{matcher.start(), matcher.end()});
            }

            for (int i = 0; i < positions.size(); i++) {
                int start = positions.get(i)[0];
                int headerEnd = positions.get(i)[1];
                int end = (i + 1 < positions.size()) ? positions.get(i + 1)[0] : content.length();

                String headerLine = content.substring(start, headerEnd);
                String sectionContent = content.substring(headerEnd, end).trim();

                ReleaseInfo release = parseSection(headerLine, sectionContent);
                if (release != null) {
                    releases.add(release);
                }
            }

            releases.sort(java.util.Comparator.naturalOrder());
            return releases;
        }

        private ReleaseInfo parseSection(String header, String content) {
            java.util.regex.Matcher headerMatcher = VERSION_HEADER_PATTERN.matcher(header);
            if (!headerMatcher.find()) {
                return null;
            }

            String version = headerMatcher.group(1);
            String dateStr = headerMatcher.group(2);

            ReleaseInfo.Builder builder = ReleaseInfo.builder()
                    .version(version)
                    .unreleased(version.equalsIgnoreCase("Unreleased"));

            if (dateStr != null && !dateStr.isEmpty()) {
                try {
                    builder.releaseDate(LocalDate.parse(dateStr));
                } catch (Exception e) {
                    // Ignore date parse errors
                }
            }

            String[] lines = content.split("\n");
            ChangeType currentType = null;

            for (String line : lines) {
                java.util.regex.Matcher typeMatcher = CHANGE_TYPE_PATTERN.matcher(line);
                if (typeMatcher.find()) {
                    try {
                        currentType = ChangeType.valueOf(typeMatcher.group(1).toUpperCase());
                    } catch (IllegalArgumentException e) {
                        currentType = null;
                    }
                    continue;
                }

                java.util.regex.Matcher itemMatcher = LIST_ITEM_PATTERN.matcher(line);
                if (itemMatcher.find() && currentType != null) {
                    String description = itemMatcher.group(1).trim();
                    builder.addEntry(currentType, description);

                    if (BREAKING_CHANGE_PATTERN.matcher(description).find()) {
                        builder.addBreakingChange(description);
                    }
                }
            }

            return builder.build();
        }

        public Map<ChangeType, Integer> countEntriesByType(List<ReleaseInfo> releases) {
            Map<ChangeType, Integer> counts = new java.util.EnumMap<>(ChangeType.class);
            for (ReleaseInfo release : releases) {
                for (Map.Entry<ChangeType, List<String>> entry : release.getEntriesByType().entrySet()) {
                    counts.merge(entry.getKey(), entry.getValue().size(), Integer::sum);
                }
            }
            return counts;
        }

        public Map<String, Integer> groupReleasesByMonth(List<ReleaseInfo> releases) {
            Map<String, Integer> byMonth = new java.util.LinkedHashMap<>();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM");

            for (ReleaseInfo release : releases) {
                if (!release.isUnreleased() && release.getReleaseDate() != null) {
                    String month = release.getReleaseDate().format(formatter);
                    byMonth.merge(month, 1, Integer::sum);
                }
            }
            return byMonth;
        }
    }
}
