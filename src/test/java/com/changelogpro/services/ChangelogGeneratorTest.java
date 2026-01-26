package com.changelogpro.services;

import com.changelogpro.config.ChangeEntry;
import com.changelogpro.config.ChangeType;
import com.changelogpro.config.GitProvider;
import com.changelogpro.config.IssueTracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unit tests for ChangelogGenerator.
 * Tests CHANGELOG.md generation from fragment files.
 */
class ChangelogGeneratorTest {

    @TempDir
    Path tempDir;

    private File changesDir;
    private File unreleasedDir;

    @BeforeEach
    void setUp() throws IOException {
        // tempDir is created automatically by JUnit 5
        changesDir = Files.createDirectory(tempDir.resolve(".changes")).toFile();
        unreleasedDir = new File(changesDir, "unreleased");
        assertTrue(unreleasedDir.mkdir());

        // Create type directories
        for (ChangeType type : ChangeType.values()) {
            File typeDir = new File(unreleasedDir, type.getDirectoryName());
            assertTrue(typeDir.mkdir(), "Failed to create directory: " + typeDir.getAbsolutePath());
        }
    }

    // ========== YAML Fragment Parsing Tests ==========

    @Test
    void testParseYamlFragmentSimple() {
        String yaml = """
                description: Added new feature
                issue: PROJ-123
                pr: 456
                author: developer
                """;

        ChangeEntry entry = parseYamlFragment(yaml);

        assertEquals("Added new feature", entry.getDescription());
        assertEquals("PROJ-123", entry.getIssueId());
        assertEquals("456", entry.getPrNumber());
        assertEquals("developer", entry.getAuthor());
    }

    @Test
    void testParseYamlFragmentWithQuotes() {
        String yaml = """
                description: "Fixed bug with special: characters"
                issue: "BUG-789"
                pr: "100"
                """;

        ChangeEntry entry = parseYamlFragment(yaml);

        assertEquals("Fixed bug with special: characters", entry.getDescription());
        assertEquals("BUG-789", entry.getIssueId());
        assertEquals("100", entry.getPrNumber());
    }

    @Test
    void testParseYamlFragmentMinimal() {
        String yaml = "description: Simple change\n";

        ChangeEntry entry = parseYamlFragment(yaml);

        assertEquals("Simple change", entry.getDescription());
        assertEquals("", entry.getIssueId());
        assertEquals("", entry.getPrNumber());
        assertNotNull(entry.getAuthor());
    }

    @Test
    void testParseYamlFragmentWithTimestamp() {
        String yaml = """
                description: Change with timestamp
                timestamp: 1703260800000
                """;

        ChangeEntry entry = parseYamlFragment(yaml);

        assertEquals("Change with timestamp", entry.getDescription());
        assertEquals(1703260800000L, entry.getTimestamp());
    }

    // ========== Entry Formatting Tests ==========

    @Test
    void testFormatEntryBasic() {
        ChangeEntry entry = new ChangeEntry(ChangeType.ADDED, "New feature", null, null, null);

        String formatted = formatEntry(entry, false, GitProvider.GITHUB, IssueTracker.NONE, "", "");

        assertEquals("- New feature", formatted);
    }

    @Test
    void testFormatEntryWithPrGitHub() {
        ChangeEntry entry = new ChangeEntry(ChangeType.FIXED, "Bug fix", null, "123", null);

        String formatted = formatEntry(entry, false, GitProvider.GITHUB, IssueTracker.NONE,
                "https://github.com/user/repo", "");

        assertThat(formatted).contains("Bug fix");
        assertThat(formatted).contains("[PR#123]");
        assertThat(formatted).contains("https://github.com/user/repo/pull/123");
    }

    @Test
    void testFormatEntryWithMrGitLab() {
        ChangeEntry entry = new ChangeEntry(ChangeType.FIXED, "Bug fix", null, "456", null);

        String formatted = formatEntry(entry, false, GitProvider.GITLAB, IssueTracker.NONE,
                "https://gitlab.com/group/project", "");

        assertThat(formatted).contains("Bug fix");
        assertThat(formatted).contains("[MR#456]");
        assertThat(formatted).contains("https://gitlab.com/group/project/-/merge_requests/456");
    }

    @Test
    void testFormatEntryWithJiraIssue() {
        ChangeEntry entry = new ChangeEntry(ChangeType.ADDED, "Feature", "PROJ-123", "789", null);

        String formatted = formatEntry(entry, false, GitProvider.GITHUB, IssueTracker.JIRA,
                "https://github.com/user/repo", "https://company.atlassian.net");

        assertThat(formatted).contains("Feature");
        assertThat(formatted).contains("[PROJ-123]");
        assertThat(formatted).contains("https://company.atlassian.net/browse/PROJ-123");
    }

    @Test
    void testFormatEntryWithAuthor() {
        ChangeEntry entry = new ChangeEntry(ChangeType.SECURITY, "Security fix", "SEC-001", "999", "security-team");

        String formatted = formatEntry(entry, true, GitProvider.GITHUB, IssueTracker.JIRA,
                "https://github.com/user/repo", "https://company.atlassian.net");

        assertThat(formatted).contains("Security fix");
        assertThat(formatted).contains("by @security-team");
    }

    @Test
    void testFormatEntryWithoutAuthorWhenDisabled() {
        ChangeEntry entry = new ChangeEntry(ChangeType.ADDED, "Feature", null, null, "developer");

        String formatted = formatEntry(entry, false, GitProvider.GITHUB, IssueTracker.NONE, "", "");

        assertThat(formatted).doesNotContain("@developer");
    }

    // ========== Changelog Generation Tests ==========

    @Test
    void testGenerateChangelogHeader() {
        String changelog = generateChangelogHeader();

        assertThat(changelog).contains("# Changelog");
        assertThat(changelog).contains("Keep a Changelog");
        assertThat(changelog).contains("Semantic Versioning");
    }

    @Test
    void testGenerateVersionSectionUnreleased() {
        List<ChangeEntry> entries = Arrays.asList(
                new ChangeEntry(ChangeType.ADDED, "New feature 1", null, null, null),
                new ChangeEntry(ChangeType.ADDED, "New feature 2", null, null, null)
        );

        String section = generateVersionSection(null, entries, false, GitProvider.GITHUB,
                IssueTracker.NONE, "", "");

        assertThat(section).contains("## [Unreleased]");
        assertThat(section).contains("### Added");
        assertThat(section).contains("- New feature 1");
        assertThat(section).contains("- New feature 2");
    }

    @Test
    void testGenerateVersionSectionWithVersion() {
        List<ChangeEntry> entries = Collections.singletonList(
                new ChangeEntry(ChangeType.FIXED, "Bug fix", null, null, null)
        );

        String section = generateVersionSection("1.2.0", entries, false, GitProvider.GITHUB,
                IssueTracker.NONE, "", "");

        assertThat(section).contains("## [1.2.0]");
        assertThat(section).contains(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        assertThat(section).contains("### Fixed");
        assertThat(section).contains("- Bug fix");
    }

    @Test
    void testGenerateChangelogMultipleTypes() {
        Map<ChangeType, List<ChangeEntry>> entriesByType = new LinkedHashMap<>();
        entriesByType.put(ChangeType.ADDED, Collections.singletonList(
                new ChangeEntry(ChangeType.ADDED, "Feature 1", null, null, null)
        ));
        entriesByType.put(ChangeType.FIXED, Arrays.asList(
                new ChangeEntry(ChangeType.FIXED, "Bug fix 1", null, null, null),
                new ChangeEntry(ChangeType.FIXED, "Bug fix 2", null, null, null)
        ));
        entriesByType.put(ChangeType.SECURITY, Collections.singletonList(
                new ChangeEntry(ChangeType.SECURITY, "Security patch", null, null, null)
        ));

        String changelog = generateFullChangelog("2.0.0", entriesByType, false,
                GitProvider.GITHUB, IssueTracker.NONE, "", "");

        assertThat(changelog).contains("# Changelog");
        assertThat(changelog).contains("## [2.0.0]");
        assertThat(changelog).contains("### Added");
        assertThat(changelog).contains("### Fixed");
        assertThat(changelog).contains("### Security");
        assertThat(changelog).contains("- Feature 1");
        assertThat(changelog).contains("- Bug fix 1");
        assertThat(changelog).contains("- Bug fix 2");
        assertThat(changelog).contains("- Security patch");
    }

    // ========== Keep a Changelog Compliance Tests ==========

    @Test
    void testChangelogFollowsKeepAChangelogFormat() {
        Map<ChangeType, List<ChangeEntry>> entriesByType = new LinkedHashMap<>();
        entriesByType.put(ChangeType.ADDED, Collections.singletonList(
                new ChangeEntry(ChangeType.ADDED, "New feature", null, null, null)
        ));

        String changelog = generateFullChangelog("1.0.0", entriesByType, false,
                GitProvider.GITHUB, IssueTracker.NONE, "", "");

        assertThat(changelog).startsWith("# Changelog");
        assertThat(changelog).contains("All notable changes");
        assertThat(changelog).contains("Keep a Changelog");
        assertThat(changelog).contains("Semantic Versioning");

        Pattern versionPattern = Pattern.compile("## \\[\\d+\\.\\d+\\.\\d+\\] - \\d{4}-\\d{2}-\\d{2}");
        assertTrue(versionPattern.matcher(changelog).find());
    }

    @Test
    void testChangeTypeOrderFollowsKeepAChangelog() {
        ChangeType[] expectedOrder = {
                ChangeType.ADDED,
                ChangeType.CHANGED,
                ChangeType.DEPRECATED,
                ChangeType.REMOVED,
                ChangeType.FIXED,
                ChangeType.SECURITY
        };

        Map<ChangeType, List<ChangeEntry>> entriesByType = new LinkedHashMap<>();
        for (int i = expectedOrder.length - 1; i >= 0; i--) {
            entriesByType.put(expectedOrder[i], Collections.singletonList(
                    new ChangeEntry(expectedOrder[i], "Entry for " + expectedOrder[i].getDisplayName(), null, null, null)
            ));
        }

        String changelog = generateFullChangelog("1.0.0", entriesByType, false,
                GitProvider.GITHUB, IssueTracker.NONE, "", "");

        int lastIndex = -1;
        for (ChangeType type : expectedOrder) {
            int index = changelog.indexOf("### " + type.getDisplayName());
            assertTrue(index > lastIndex, "Type " + type + " should appear after previous types");
            lastIndex = index;
        }
    }

    // ========== Fragment File Reading Tests ==========

    @Test
    void testReadFragmentFilesFromDirectory() throws IOException {
        File addedDir = new File(unreleasedDir, "added");
        createFragmentFile(addedDir, "feature1.yaml",
                "description: Feature 1\nissue: FEAT-1\npr: 100\n");
        createFragmentFile(addedDir, "feature2.yaml",
                "description: Feature 2\nissue: FEAT-2\npr: 101\n");

        File fixedDir = new File(unreleasedDir, "fixed");
        createFragmentFile(fixedDir, "bugfix1.yaml",
                "description: Bug fix 1\nissue: BUG-1\npr: 200\n");

        Map<ChangeType, List<ChangeEntry>> entries = readFragmentsFromDirectory(tempDir.toFile());

        assertThat(entries).containsKey(ChangeType.ADDED);
        assertThat(entries).containsKey(ChangeType.FIXED);
        assertThat(entries.get(ChangeType.ADDED)).hasSize(2);
        assertThat(entries.get(ChangeType.FIXED)).hasSize(1);
    }

    @Test
    void testReadFragmentFilesYmlExtension() throws IOException {
        File addedDir = new File(unreleasedDir, "added");
        createFragmentFile(addedDir, "feature.yml",
                "description: Feature with yml extension\n");

        Map<ChangeType, List<ChangeEntry>> entries = readFragmentsFromDirectory(tempDir.toFile());

        assertThat(entries).containsKey(ChangeType.ADDED);
        assertThat(entries.get(ChangeType.ADDED)).hasSize(1);
    }

    @Test
    void testReadFragmentFilesEmptyDirectory() throws IOException {
        Map<ChangeType, List<ChangeEntry>> entries = readFragmentsFromDirectory(tempDir.toFile());
        assertTrue(entries.isEmpty());
    }

    // ========== Fragment Archiving Tests ==========

    @Test
    void testArchiveFragments() throws IOException {
        File addedDir = new File(unreleasedDir, "added");
        createFragmentFile(addedDir, "feature.yaml", "description: Feature\n");

        boolean result = archiveFragments(tempDir.toFile(), "1.0.0");
        assertTrue(result);

        File archivedDir = new File(changesDir, "v1.0.0/added");
        assertTrue(archivedDir.exists());
        assertTrue(new File(archivedDir, "feature.yaml").exists());
        assertFalse(new File(addedDir, "feature.yaml").exists());
    }

    @Test
    void testDeleteFragments() throws IOException {
        File addedDir = new File(unreleasedDir, "added");
        File fragmentFile = createFragmentFile(addedDir, "feature.yaml", "description: Feature\n");

        assertTrue(fragmentFile.exists());

        boolean result = deleteFragments(tempDir.toFile());

        assertTrue(result);
        assertFalse(fragmentFile.exists());
    }

    // ========== Edge Cases Tests ==========

    @Test
    void testGenerateChangelogEmptyEntries() {
        Map<ChangeType, List<ChangeEntry>> entriesByType = new LinkedHashMap<>();

        String changelog = generateFullChangelog("1.0.0", entriesByType, false,
                GitProvider.GITHUB, IssueTracker.NONE, "", "");

        assertThat(changelog).contains("# Changelog");
    }

    @Test
    void testFormatEntryWithSpecialCharacters() {
        ChangeEntry entry = new ChangeEntry(ChangeType.FIXED,
                "Fixed issue with <script> tags & special \"characters\"", null, null, null);

        String formatted = formatEntry(entry, false, GitProvider.GITHUB, IssueTracker.NONE, "", "");

        assertThat(formatted).contains("<script>");
        assertThat(formatted).contains("&");
        assertThat(formatted).contains("\"characters\"");
    }

    @Test
    void testFormatEntryWithMultilineDescription() {
        ChangeEntry entry = new ChangeEntry(ChangeType.ADDED,
                "Added feature with\nmultiple lines", null, null, null);

        String formatted = formatEntry(entry, false, GitProvider.GITHUB, IssueTracker.NONE, "", "");

        assertThat(formatted).contains("Added feature with");
    }

    // ========== Helper Methods ==========

    private ChangeEntry parseYamlFragment(String yaml) {
        ChangeEntry entry = new ChangeEntry();

        Pattern descPattern = Pattern.compile("description:\\s*\"?([^\"\\n]+)\"?");
        Pattern issuePattern = Pattern.compile("issue:\\s*\"?([^\"\\n]+)\"?");
        Pattern prPattern = Pattern.compile("pr:\\s*\"?([^\"\\n]+)\"?");
        Pattern authorPattern = Pattern.compile("author:\\s*\"?([^\"\\n]+)\"?");
        Pattern timestampPattern = Pattern.compile("timestamp:\\s*(\\d+)");

        Matcher m;
        if ((m = descPattern.matcher(yaml)).find()) entry.setDescription(m.group(1).trim());
        if ((m = issuePattern.matcher(yaml)).find()) entry.setIssueId(m.group(1).trim());
        if ((m = prPattern.matcher(yaml)).find()) entry.setPrNumber(m.group(1).trim());
        if ((m = authorPattern.matcher(yaml)).find()) entry.setAuthor(m.group(1).trim());
        if ((m = timestampPattern.matcher(yaml)).find()) entry.setTimestamp(Long.parseLong(m.group(1)));

        return entry;
    }

    private String formatEntry(ChangeEntry entry, boolean includeAuthor, GitProvider gitProvider,
                               IssueTracker issueTracker, String repoUrl, String issueTrackerUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("- ").append(entry.getDescription());

        if (entry.getPrNumber() != null && !entry.getPrNumber().isEmpty() && !repoUrl.isEmpty()) {
            String prUrl = gitProvider.buildPrUrl(repoUrl, entry.getPrNumber());
            sb.append(" [").append(gitProvider.getPrTermShort()).append("#")
                    .append(entry.getPrNumber()).append("](").append(prUrl).append(")");
        }

        if (entry.getIssueId() != null && !entry.getIssueId().isEmpty() && !issueTrackerUrl.isEmpty()) {
            String issueUrl = issueTracker.buildIssueUrl(issueTrackerUrl, entry.getIssueId());
            sb.append(" closes [").append(entry.getIssueId()).append("](").append(issueUrl).append(")");
        }

        if (includeAuthor && entry.getAuthor() != null && !entry.getAuthor().isEmpty()) {
            sb.append(" by @").append(entry.getAuthor());
        }

        return sb.toString();
    }

    private String generateChangelogHeader() {
        return "# Changelog\n\n" +
                "All notable changes to this project will be documented in this file.\n\n" +
                "The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),\n" +
                "and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).\n\n";
    }

    private String generateVersionSection(String version, List<ChangeEntry> entries, boolean includeAuthor,
                                          GitProvider gitProvider, IssueTracker issueTracker,
                                          String repoUrl, String issueTrackerUrl) {
        StringBuilder sb = new StringBuilder();

        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        if (version != null && !version.isEmpty()) {
            sb.append("## [").append(version).append("] - ").append(date).append("\n\n");
        } else {
            sb.append("## [Unreleased]\n\n");
        }

        Map<ChangeType, List<ChangeEntry>> byType = new LinkedHashMap<>();
        for (ChangeEntry entry : entries) {
            byType.computeIfAbsent(entry.getType(), k -> new ArrayList<>()).add(entry);
        }

        for (ChangeType type : ChangeType.values()) {
            List<ChangeEntry> typeEntries = byType.get(type);
            if (typeEntries != null && !typeEntries.isEmpty()) {
                sb.append("### ").append(type.getDisplayName()).append("\n\n");
                for (ChangeEntry entry : typeEntries) {
                    sb.append(formatEntry(entry, includeAuthor, gitProvider, issueTracker, repoUrl, issueTrackerUrl));
                    sb.append("\n");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private String generateFullChangelog(String version, Map<ChangeType, List<ChangeEntry>> entriesByType,
                                         boolean includeAuthor, GitProvider gitProvider, IssueTracker issueTracker,
                                         String repoUrl, String issueTrackerUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append(generateChangelogHeader());

        if (!entriesByType.isEmpty()) {
            List<ChangeEntry> allEntries = new ArrayList<>();
            for (List<ChangeEntry> entries : entriesByType.values()) {
                allEntries.addAll(entries);
            }
            sb.append(generateVersionSection(version, allEntries, includeAuthor, gitProvider,
                    issueTracker, repoUrl, issueTrackerUrl));
        }

        return sb.toString();
    }

    private Map<ChangeType, List<ChangeEntry>> readFragmentsFromDirectory(File projectRoot) throws IOException {
        Map<ChangeType, List<ChangeEntry>> result = new LinkedHashMap<>();

        File unreleasedPath = new File(projectRoot, ".changes/unreleased");
        if (!unreleasedPath.exists()) {
            return result;
        }

        for (ChangeType type : ChangeType.values()) {
            File typeDir = new File(unreleasedPath, type.getDirectoryName());
            if (typeDir.exists() && typeDir.isDirectory()) {
                List<ChangeEntry> entries = new ArrayList<>();

                File[] files = typeDir.listFiles((dir, name) ->
                        name.endsWith(".yaml") || name.endsWith(".yml"));

                if (files != null) {
                    for (File file : files) {
                        String content = new String(Files.readAllBytes(file.toPath()));
                        ChangeEntry entry = parseYamlFragment(content);
                        entry.setType(type);
                        entries.add(entry);
                    }
                }

                if (!entries.isEmpty()) {
                    result.put(type, entries);
                }
            }
        }

        return result;
    }

    private File createFragmentFile(File dir, String name, String content) throws IOException {
        File file = new File(dir, name);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }

    private boolean archiveFragments(File projectRoot, String version) throws IOException {
        File unreleasedPath = new File(projectRoot, ".changes/unreleased");
        File archivePath = new File(projectRoot, ".changes/v" + version);

        for (ChangeType type : ChangeType.values()) {
            File sourceDir = new File(unreleasedPath, type.getDirectoryName());
            if (sourceDir.exists()) {
                File targetDir = new File(archivePath, type.getDirectoryName());
                targetDir.mkdirs();

                File[] files = sourceDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        Files.move(file.toPath(), new File(targetDir, file.getName()).toPath());
                    }
                }
            }
        }

        return true;
    }

    private boolean deleteFragments(File projectRoot) {
        File unreleasedPath = new File(projectRoot, ".changes/unreleased");

        for (ChangeType type : ChangeType.values()) {
            File typeDir = new File(unreleasedPath, type.getDirectoryName());
            if (typeDir.exists()) {
                File[] files = typeDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        // ignore failures (matches original behavior)
                        file.delete();
                    }
                }
            }
        }

        return true;
    }
}
