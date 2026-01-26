package com.changelogpro.analytics;

import com.changelogpro.analytics.model.*;
import com.changelogpro.config.ChangeLogConfig;
import com.changelogpro.config.ChangeType;
import com.changelogpro.services.ChangeLogProjectService;
import com.changelogpro.services.LogService;
import com.changelogpro.services.ProjectVersionService;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aggregates analytics data from multiple sources into a unified view.
 * Combines Git history, changelog files, and unreleased fragments.
 * 
 * <p>This is the main entry point for collecting analytics data.
 * Use {@link #collectAnalytics()} to gather all metrics.
 * 
 * <p>Includes caching to avoid expensive recomputation:
 * <ul>
 *     <li>Cache expires after 5 minutes</li>
 *     <li>Force refresh available via {@link #collectAnalytics(boolean)}</li>
 * </ul>
 */
public class AnalyticsAggregator {
    
    private static final int RECENT_COMMITS_LIMIT = 50;
    private static final int HISTORY_MONTHS = 12;
    private static final long CACHE_EXPIRY_MS = 5 * 60 * 1000; // 5 minutes
    
    // Static cache per project
    private static final Map<String, CachedData> projectCache = new ConcurrentHashMap<>();
    
    private final Project project;
    private final LogService logger;
    private final GitAnalyticsService gitService;
    private final ChangelogAnalyticsService changelogService;
    private final ChangeLogProjectService projectService;
    
    /**
     * Cached analytics data with timestamp.
     */
    private static class CachedData {
        final AnalyticsData data;
        final long timestamp;
        
        CachedData(AnalyticsData data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MS;
        }
    }
    
    public AnalyticsAggregator(@NotNull Project project) {
        this.project = project;
        this.projectService = ChangeLogProjectService.getInstance(project);
        this.logger = projectService.getLogService();
        this.gitService = new GitAnalyticsService(project, logger);
        this.changelogService = new ChangelogAnalyticsService(project, logger);
    }
    
    /**
     * Collects all analytics data for the project.
     * Uses cached data if available and not expired.
     * 
     * @return Aggregated analytics data
     */
    @NotNull
    public AnalyticsData collectAnalytics() {
        return collectAnalytics(false);
    }
    
    /**
     * Collects all analytics data for the project.
     * 
     * @param forceRefresh If true, bypasses cache and recomputes data
     * @return Aggregated analytics data
     */
    @NotNull
    public AnalyticsData collectAnalytics(boolean forceRefresh) {
        String projectKey = project.getBasePath();
        if (projectKey == null) {
            projectKey = project.getName();
        }
        
        // Check cache
        if (!forceRefresh) {
            CachedData cached = projectCache.get(projectKey);
            if (cached != null && !cached.isExpired()) {
                logger.info("Using cached analytics data");
                return cached.data;
            }
        }
        
        logger.info("Collecting analytics data...");
        
        AnalyticsData.Builder builder = AnalyticsData.builder();
        
        // Project metadata
        collectProjectMetadata(builder);
        
        // Changelog data
        collectChangelogData(builder);
        
        // Git data
        if (gitService.isGitRepository()) {
            collectGitData(builder);
        }
        
        // Calculate health score
        HealthScore healthScore = calculateHealthScore(builder);
        builder.healthScore(healthScore);
        
        AnalyticsData data = builder.build();
        
        // Update cache
        projectCache.put(projectKey, new CachedData(data));
        
        logger.success("Analytics collection complete");
        return data;
    }
    
    /**
     * Clears the cache for the current project.
     */
    public void clearCache() {
        String projectKey = project.getBasePath();
        if (projectKey == null) {
            projectKey = project.getName();
        }
        projectCache.remove(projectKey);
        logger.info("Analytics cache cleared");
    }
    
    /**
     * Clears all cached analytics data.
     */
    public static void clearAllCaches() {
        projectCache.clear();
    }
    
    /**
     * Collects basic project metadata.
     */
    private void collectProjectMetadata(@NotNull AnalyticsData.Builder builder) {
        // Project name
        String projectName = project.getName();
        builder.projectName(projectName);
        
        // Current version
        ProjectVersionService versionService = projectService.getProjectVersionService();
        String currentVersion = versionService.getCurrentVersion();
        builder.currentVersion(currentVersion);
        
        // Git provider
        ChangeLogConfig config = projectService.getConfig();
        builder.gitProvider(config.getGitProvider().getDisplayName());
    }
    
    /**
     * Collects data from CHANGELOG.md and unreleased fragments.
     */
    private void collectChangelogData(@NotNull AnalyticsData.Builder builder) {
        // Parse existing changelog
        List<ReleaseInfo> releases = changelogService.parseChangelog();
        builder.releases(releases);
        
        // Count entries by type
        Map<ChangeType, Integer> entriesByType = changelogService.countEntriesByType(releases);
        for (Map.Entry<ChangeType, Integer> entry : entriesByType.entrySet()) {
            builder.addEntriesByType(entry.getKey(), entry.getValue());
        }
        
        // Group releases by month
        Map<String, Integer> releasesByMonth = changelogService.groupReleasesByMonth(releases);
        for (Map.Entry<String, Integer> entry : releasesByMonth.entrySet()) {
            builder.addReleasesByMonth(entry.getKey(), entry.getValue());
        }
        
        // Count unreleased entries
        int unreleasedCount = projectService.getFileService().countTotalUnreleasedEntries();
        builder.unreleasedEntryCount(unreleasedCount);
        
        logger.info("Found " + releases.size() + " releases in changelog");
    }
    
    /**
     * Collects data from Git history.
     */
    private void collectGitData(@NotNull AnalyticsData.Builder builder) {
        // Total commit count
        int totalCommits = gitService.getTotalCommitCount();
        builder.totalCommitCount(totalCommits);
        
        // Get all commits for analysis (limited for performance)
        List<CommitInfo> allCommits = gitService.getCommits(null, "HEAD", 1000);
        
        // Recent commits for display
        List<CommitInfo> recentCommits = allCommits.stream()
            .limit(RECENT_COMMITS_LIMIT)
            .toList();
        builder.recentCommits(recentCommits);
        
        // Conventional commit count
        int conventionalCount = gitService.countConventionalCommits(allCommits);
        builder.conventionalCommitCount(conventionalCount);
        
        // Commits by month
        Map<String, Integer> commitsByMonth = gitService.groupCommitsByMonth(allCommits);
        for (Map.Entry<String, Integer> entry : commitsByMonth.entrySet()) {
            builder.addCommitsByMonth(entry.getKey(), entry.getValue());
        }
        
        // Contributor statistics
        Map<String, ContributorStats> contributorMap = gitService.calculateContributorStats(allCommits);
        List<ContributorStats> contributors = new ArrayList<>(contributorMap.values());
        contributors.sort(Comparator.naturalOrder()); // Sort by contribution
        builder.contributors(contributors);
        
        // Calculate undocumented commits
        int documentedEstimate = calculateDocumentedCommits(allCommits, builder);
        builder.undocumentedCommitCount(Math.max(0, allCommits.size() - documentedEstimate));
        
        logger.info("Analyzed " + allCommits.size() + " commits from " + contributors.size() + " contributors");
    }
    
    /**
     * Estimates the number of documented commits based on changelog entries.
     * Uses a heuristic: each changelog entry roughly corresponds to one or more commits.
     */
    private int calculateDocumentedCommits(@NotNull List<CommitInfo> commits, 
                                           @NotNull AnalyticsData.Builder builder) {
        // Count conventional commits that would be documented
        // (feat, fix are typically documented; chore, docs, etc. are not)
        Set<String> documentedTypes = Set.of("feat", "fix", "perf", "security", "revert");
        
        long documentedConventional = commits.stream()
            .filter(c -> c.getConventionalType() != null)
            .filter(c -> documentedTypes.contains(c.getConventionalType()))
            .count();
        
        // For non-conventional commits, assume 50% might be documented
        long nonConventional = commits.stream()
            .filter(c -> c.getConventionalType() == null)
            .filter(c -> !c.isMergeCommit())
            .count();
        
        return (int) (documentedConventional + nonConventional / 2);
    }
    
    /**
     * Calculates the project health score based on collected data.
     */
    @NotNull
    private HealthScore calculateHealthScore(@NotNull AnalyticsData.Builder dataBuilder) {
        HealthScore.Builder scoreBuilder = HealthScore.builder();
        
        // Build a temporary data object for calculations
        // (We need some data that's already been set)
        
        // 1. Changelog format compliance (20 points)
        List<ReleaseInfo> releases = new ArrayList<>(); // Get from builder context
        int formatScore = calculateFormatScore();
        scoreBuilder.addCriterion(
            "Changelog format",
            "Keep a Changelog compliant",
            formatScore, 20
        );
        
        // 2. Semantic versioning (15 points)
        int semverScore = calculateSemverScore();
        scoreBuilder.addCriterion(
            "Semantic versioning",
            "Properly followed",
            semverScore, 15
        );
        
        // 3. Documentation coverage (20 points)
        int coverageScore = calculateCoverageScore();
        scoreBuilder.addCriterion(
            "Documentation coverage",
            "Percentage of commits documented",
            coverageScore, 20
        );
        
        // 4. Release frequency (20 points)
        int frequencyScore = calculateFrequencyScore();
        scoreBuilder.addCriterion(
            "Release frequency",
            "Regular release cadence",
            frequencyScore, 20
        );
        
        // 5. Breaking change documentation (15 points)
        int breakingScore = calculateBreakingChangeScore();
        scoreBuilder.addCriterion(
            "Breaking change docs",
            "Breaking changes documented",
            breakingScore, 15
        );
        
        // 6. Unreleased management (10 points)
        int unreleasedScore = calculateUnreleasedScore();
        scoreBuilder.addCriterion(
            "Unreleased management",
            "Pending entries count",
            unreleasedScore, 10
        );
        
        // Add recommendations based on scores
        addRecommendations(scoreBuilder, formatScore, coverageScore, frequencyScore);
        
        return scoreBuilder.build();
    }
    
    private int calculateFormatScore() {
        // Check if changelog exists and follows format
        List<ReleaseInfo> releases = changelogService.parseChangelog();
        if (releases.isEmpty()) {
            return 5; // Minimal score if no changelog
        }
        
        // Check structure
        releases.stream()
                .allMatch(r -> r.getVersion() != null);
        boolean hasProperHeaders = true;
        boolean hasDates = releases.stream()
            .filter(r -> !r.isUnreleased())
            .allMatch(r -> r.getReleaseDate() != null);
        boolean hasTypedEntries = releases.stream()
            .anyMatch(r -> !r.getEntriesByType().isEmpty());
        
        int score = 5; // Base score for having a changelog
        if (hasProperHeaders) score += 5;
        if (hasDates) score += 5;
        if (hasTypedEntries) score += 5;
        
        return score;
    }
    
    private int calculateSemverScore() {
        List<ReleaseInfo> releases = changelogService.parseChangelog();
        if (releases.isEmpty()) return 0;
        
        // Check if versions follow semver pattern
        long semverCompliant = releases.stream()
            .filter(r -> !r.isUnreleased())
            .filter(r -> r.getVersion().matches("\\d+\\.\\d+\\.\\d+.*"))
            .count();
        
        long total = releases.stream().filter(r -> !r.isUnreleased()).count();
        if (total == 0) return 15;
        
        return (int) ((semverCompliant * 15) / total);
    }
    
    private int calculateCoverageScore() {
        if (!gitService.isGitRepository()) return 20;
        
        int totalCommits = gitService.getTotalCommitCount();
        if (totalCommits == 0) return 20;
        
        // Estimate coverage based on changelog entries vs commits
        List<ReleaseInfo> releases = changelogService.parseChangelog();
        int totalEntries = releases.stream()
            .mapToInt(ReleaseInfo::getTotalEntryCount)
            .sum();
        
        // Rough estimate: each entry covers ~5 commits on average
        double coverage = Math.min(1.0, (totalEntries * 5.0) / totalCommits);
        return (int) (coverage * 20);
    }
    
    private int calculateFrequencyScore() {
        List<ReleaseInfo> releases = changelogService.parseChangelog();
        List<ReleaseInfo> datedReleases = releases.stream()
            .filter(r -> !r.isUnreleased() && r.getReleaseDate() != null)
            .sorted(Comparator.comparing(ReleaseInfo::getReleaseDate))
            .toList();
        
        if (datedReleases.size() < 2) return 10;
        
        // Calculate average days between releases
        long totalDays = 0;
        for (int i = 1; i < datedReleases.size(); i++) {
            LocalDate prev = datedReleases.get(i - 1).getReleaseDate();
            LocalDate curr = datedReleases.get(i).getReleaseDate();
            totalDays += java.time.temporal.ChronoUnit.DAYS.between(prev, curr);
        }
        
        double avgDays = (double) totalDays / (datedReleases.size() - 1);
        
        // Score based on release frequency
        // Ideal: 14-60 days = full score
        // Too fast (<7 days) or too slow (>120 days) = lower score
        if (avgDays >= 14 && avgDays <= 60) return 20;
        if (avgDays >= 7 && avgDays <= 90) return 15;
        if (avgDays >= 1 && avgDays <= 120) return 10;
        return 5;
    }
    
    private int calculateBreakingChangeScore() {
        List<ReleaseInfo> releases = changelogService.parseChangelog();
        
        // Find releases with breaking changes
        List<ReleaseInfo> withBreaking = releases.stream()
            .filter(ReleaseInfo::hasBreakingChanges)
            .toList();
        
        if (withBreaking.isEmpty()) return 15; // No breaking changes = full score
        
        // Check if breaking changes are in Changed/Removed sections (proper documentation)
        long properlyDocumented = withBreaking.stream()
            .filter(r -> {
                int changed = r.getEntryCount(ChangeType.CHANGED);
                int removed = r.getEntryCount(ChangeType.REMOVED);
                return changed > 0 || removed > 0;
            })
            .count();
        
        return (int) ((properlyDocumented * 15) / withBreaking.size());
    }
    
    private int calculateUnreleasedScore() {
        int unreleasedCount = projectService.getFileService().countTotalUnreleasedEntries();
        
        // Ideal: 1-20 unreleased entries = full score
        // Too few (0) = might be forgetting to document
        // Too many (>50) = should release more often
        if (unreleasedCount >= 1 && unreleasedCount <= 20) return 10;
        if (unreleasedCount == 0 || unreleasedCount <= 30) return 8;
        if (unreleasedCount <= 50) return 5;
        return 2;
    }
    
    private void addRecommendations(@NotNull HealthScore.Builder builder,
                                    int formatScore, int coverageScore, int frequencyScore) {
        // Low documentation coverage
        if (coverageScore < 15) {
            int undocumented = gitService.getTotalCommitCount() - 
                (changelogService.parseChangelog().stream()
                    .mapToInt(ReleaseInfo::getTotalEntryCount).sum() * 5);
            
            builder.addRecommendation(
                HealthScore.Severity.HIGH,
                "Many commits not documented",
                undocumented + " commits may not be in changelog",
                "Import from Git History",
                "import_git_history"
            );
        }
        
        // No unreleased entries but commits exist
        int unreleased = projectService.getFileService().countTotalUnreleasedEntries();
        if (unreleased == 0 && gitService.getCommitsSinceLastTag().size() > 5) {
            builder.addRecommendation(
                HealthScore.Severity.MEDIUM,
                "No unreleased entries",
                "Consider documenting recent changes",
                "Add Entry",
                "new_entry"
            );
        }
        
        // Many unreleased entries
        if (unreleased > 20) {
            builder.addRecommendation(
                HealthScore.Severity.LOW,
                unreleased + " unreleased entries",
                "Consider generating a new release",
                "Generate CHANGELOG",
                "generate_changelog"
            );
        }
        
        // Low format compliance
        if (formatScore < 15) {
            builder.addRecommendation(
                HealthScore.Severity.MEDIUM,
                "Changelog format issues",
                "Some releases missing dates or proper structure",
                "Review CHANGELOG.md",
                "open_changelog"
            );
        }
    }
}
