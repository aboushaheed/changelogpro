package com.changelogpro.analytics;

import com.changelogpro.analytics.model.ReleaseInfo;
import com.changelogpro.config.ChangeType;
import com.changelogpro.services.LogService;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for parsing and analyzing existing CHANGELOG.md files.
 * Extracts release information, entries by type, and breaking changes.
 * 
 * <p>Supports the standard Keep a Changelog format:
 * <pre>
 * ## [1.0.0] - 2024-01-15
 * ### Added
 * - New feature description
 * ### Fixed
 * - Bug fix description
 * </pre>
 */
public class ChangelogAnalyticsService {
    
    // Pattern for version headers: ## [1.0.0] - 2024-01-15 or ## [Unreleased]
    private static final Pattern VERSION_HEADER_PATTERN = Pattern.compile(
        "^##\\s+\\[([^\\]]+)\\](?:\\s*-\\s*(\\d{4}-\\d{2}-\\d{2}))?",
        Pattern.MULTILINE
    );
    
    // Pattern for change type headers: ### Added, ### Fixed, etc.
    private static final Pattern CHANGE_TYPE_PATTERN = Pattern.compile(
        "^###\\s+(Added|Changed|Deprecated|Removed|Fixed|Security)\\s*$",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );
    
    // Pattern for list items: - Description text
    private static final Pattern LIST_ITEM_PATTERN = Pattern.compile(
        "^\\s*[-*]\\s+(.+)$"
    );
    
    // Pattern for breaking change indicators
    private static final Pattern BREAKING_CHANGE_PATTERN = Pattern.compile(
        "\\bBREAKING\\s*CHANGE\\b|\\b⚠️|\\bBREAKING\\b",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    private final Project project;
    private final LogService logger;
    
    public ChangelogAnalyticsService(@NotNull Project project, @NotNull LogService logger) {
        this.project = project;
        this.logger = logger;
    }
    
    /**
     * Parses the CHANGELOG.md file and extracts all release information.
     * 
     * @return List of releases, sorted newest first
     */
    @NotNull
    public List<ReleaseInfo> parseChangelog() {
        String content = readChangelogFile();
        if (content == null || content.isEmpty()) {
            return Collections.emptyList();
        }
        
        return parseChangelogContent(content);
    }
    
    /**
     * Parses changelog content string.
     * Useful for testing without file system access.
     */
    @NotNull
    public List<ReleaseInfo> parseChangelogContent(@NotNull String content) {
        List<ReleaseInfo> releases = new ArrayList<>();
        
        // Find all version headers and their positions
        List<VersionSection> sections = findVersionSections(content);
        
        for (VersionSection section : sections) {
            ReleaseInfo release = parseVersionSection(section);
            if (release != null) {
                releases.add(release);
            }
        }
        
        // Sort by date (newest first)
        releases.sort(Comparator.naturalOrder());
        
        return releases;
    }
    
    /**
     * Counts total entries by type across all releases.
     */
    @NotNull
    public Map<ChangeType, Integer> countEntriesByType(@NotNull List<ReleaseInfo> releases) {
        Map<ChangeType, Integer> counts = new EnumMap<>(ChangeType.class);
        
        for (ReleaseInfo release : releases) {
            for (Map.Entry<ChangeType, List<String>> entry : release.getEntriesByType().entrySet()) {
                counts.merge(entry.getKey(), entry.getValue().size(), Integer::sum);
            }
        }
        
        return counts;
    }
    
    /**
     * Groups releases by month.
     */
    @NotNull
    public Map<String, Integer> groupReleasesByMonth(@NotNull List<ReleaseInfo> releases) {
        Map<String, Integer> byMonth = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        for (ReleaseInfo release : releases) {
            if (!release.isUnreleased() && release.getReleaseDate() != null) {
                String month = release.getReleaseDate().format(formatter);
                byMonth.merge(month, 1, Integer::sum);
            }
        }
        
        return byMonth;
    }
    
    /**
     * Finds all breaking changes across releases.
     */
    @NotNull
    public List<BreakingChangeInfo> findBreakingChanges(@NotNull List<ReleaseInfo> releases) {
        List<BreakingChangeInfo> breakingChanges = new ArrayList<>();
        
        for (ReleaseInfo release : releases) {
            for (String description : release.getBreakingChanges()) {
                breakingChanges.add(new BreakingChangeInfo(
                    release.getVersion(),
                    release.getReleaseDate(),
                    description
                ));
            }
        }
        
        return breakingChanges;
    }
    
    /**
     * Reads the CHANGELOG.md file from the project root.
     */
    @Nullable
    private String readChangelogFile() {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return null;
        }
        
        // Try common changelog file names
        String[] fileNames = {"CHANGELOG.md", "changelog.md", "HISTORY.md", "CHANGES.md"};
        
        for (String fileName : fileNames) {
            Path path = Paths.get(basePath, fileName);
            if (Files.exists(path)) {
                try {
                    return Files.readString(path, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    logger.error("Failed to read " + fileName + ": " + e.getMessage());
                }
            }
        }
        
        return null;
    }
    
    /**
     * Finds all version sections in the content.
     */
    @NotNull
    private List<VersionSection> findVersionSections(@NotNull String content) {
        List<VersionSection> sections = new ArrayList<>();
        Matcher matcher = VERSION_HEADER_PATTERN.matcher(content);
        
        List<int[]> positions = new ArrayList<>();
        while (matcher.find()) {
            positions.add(new int[]{matcher.start(), matcher.end()});
        }
        
        // Extract content for each section
        for (int i = 0; i < positions.size(); i++) {
            int start = positions.get(i)[0];
            int headerEnd = positions.get(i)[1];
            int end = (i + 1 < positions.size()) ? positions.get(i + 1)[0] : content.length();
            
            String headerLine = content.substring(start, headerEnd);
            String sectionContent = content.substring(headerEnd, end).trim();
            
            sections.add(new VersionSection(headerLine, sectionContent));
        }
        
        return sections;
    }
    
    /**
     * Parses a single version section into a ReleaseInfo.
     */
    @Nullable
    private ReleaseInfo parseVersionSection(@NotNull VersionSection section) {
        // Parse header
        Matcher headerMatcher = VERSION_HEADER_PATTERN.matcher(section.header);
        if (!headerMatcher.find()) {
            return null;
        }
        
        String version = headerMatcher.group(1);
        String dateStr = headerMatcher.group(2);
        
        ReleaseInfo.Builder builder = ReleaseInfo.builder()
            .version(version)
            .unreleased(version.equalsIgnoreCase("Unreleased"));
        
        // Parse date if present
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                builder.releaseDate(LocalDate.parse(dateStr, DATE_FORMATTER));
            } catch (DateTimeParseException e) {
                logger.warn("Could not parse date: " + dateStr);
            }
        }
        
        // Set tag name (common convention)
        if (!version.equalsIgnoreCase("Unreleased")) {
            builder.tagName("v" + version);
        }
        
        // Parse entries by type
        parseEntriesByType(section.content, builder);
        
        return builder.build();
    }
    
    /**
     * Parses entries organized by change type.
     */
    private void parseEntriesByType(@NotNull String content, @NotNull ReleaseInfo.Builder builder) {
        // Split content by change type headers
        String[] lines = content.split("\n");
        ChangeType currentType = null;
        
        for (String line : lines) {
            // Check for change type header
            Matcher typeMatcher = CHANGE_TYPE_PATTERN.matcher(line);
            if (typeMatcher.find()) {
                currentType = parseChangeType(typeMatcher.group(1));
                continue;
            }
            
            // Check for list item
            Matcher itemMatcher = LIST_ITEM_PATTERN.matcher(line);
            if (itemMatcher.find() && currentType != null) {
                String description = itemMatcher.group(1).trim();
                builder.addEntry(currentType, description);
                
                // Check for breaking change
                if (BREAKING_CHANGE_PATTERN.matcher(description).find()) {
                    builder.addBreakingChange(description);
                }
            }
        }
    }
    
    /**
     * Parses a change type string to enum.
     */
    @Nullable
    private ChangeType parseChangeType(@NotNull String typeStr) {
        try {
            return ChangeType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Helper class to hold a version section.
     */
    private static class VersionSection {
        final String header;
        final String content;
        
        VersionSection(String header, String content) {
            this.header = header;
            this.content = content;
        }
    }
    
    /**
     * Represents a breaking change with context.
     */
    public static class BreakingChangeInfo {
        private final String version;
        private final LocalDate date;
        private final String description;
        
        public BreakingChangeInfo(String version, LocalDate date, String description) {
            this.version = version;
            this.date = date;
            this.description = description;
        }
        
        public String getVersion() { return version; }
        public LocalDate getDate() { return date; }
        public String getDescription() { return description; }
        
        @Override
        public String toString() {
            return "v" + version + ": " + description;
        }
    }
}
