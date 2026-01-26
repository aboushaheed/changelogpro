package com.changelogpro.analytics.model;

import com.changelogpro.config.ChangeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Aggregated analytics data for a project.
 * Contains all computed metrics from Git history and changelog files.
 */
public class AnalyticsData {
    
    // Metadata
    private final LocalDateTime generatedAt;
    private final String projectName;
    private final String currentVersion;
    private final String gitProvider;
    
    // Release data
    private final List<ReleaseInfo> releases;
    private final int unreleasedEntryCount;
    
    // Commit data
    private final List<CommitInfo> recentCommits;
    private final int totalCommitCount;
    private final int conventionalCommitCount;
    private final int undocumentedCommitCount;
    
    // Contributor data
    private final List<ContributorStats> contributors;
    
    // Aggregated statistics
    private final Map<ChangeType, Integer> totalEntriesByType;
    private final Map<String, Integer> commitsByMonth;
    private final Map<String, Integer> releasesByMonth;
    
    // Health score
    private final HealthScore healthScore;
    
    private AnalyticsData(Builder builder) {
        this.generatedAt = LocalDateTime.now();
        this.projectName = builder.projectName;
        this.currentVersion = builder.currentVersion;
        this.gitProvider = builder.gitProvider;
        this.releases = Collections.unmodifiableList(new ArrayList<>(builder.releases));
        this.unreleasedEntryCount = builder.unreleasedEntryCount;
        this.recentCommits = Collections.unmodifiableList(new ArrayList<>(builder.recentCommits));
        this.totalCommitCount = builder.totalCommitCount;
        this.conventionalCommitCount = builder.conventionalCommitCount;
        this.undocumentedCommitCount = builder.undocumentedCommitCount;
        this.contributors = Collections.unmodifiableList(new ArrayList<>(builder.contributors));
        this.totalEntriesByType = Collections.unmodifiableMap(new EnumMap<>(builder.totalEntriesByType));
        this.commitsByMonth = Collections.unmodifiableMap(new LinkedHashMap<>(builder.commitsByMonth));
        this.releasesByMonth = Collections.unmodifiableMap(new LinkedHashMap<>(builder.releasesByMonth));
        this.healthScore = builder.healthScore;
    }
    
    // Getters
    
    @NotNull
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    @Nullable
    public String getProjectName() {
        return projectName;
    }
    
    @Nullable
    public String getCurrentVersion() {
        return currentVersion;
    }
    
    @Nullable
    public String getGitProvider() {
        return gitProvider;
    }
    
    @NotNull
    public List<ReleaseInfo> getReleases() {
        return releases;
    }
    
    public int getUnreleasedEntryCount() {
        return unreleasedEntryCount;
    }
    
    @NotNull
    public List<CommitInfo> getRecentCommits() {
        return recentCommits;
    }
    
    public int getTotalCommitCount() {
        return totalCommitCount;
    }
    
    public int getConventionalCommitCount() {
        return conventionalCommitCount;
    }
    
    public int getUndocumentedCommitCount() {
        return undocumentedCommitCount;
    }
    
    @NotNull
    public List<ContributorStats> getContributors() {
        return contributors;
    }
    
    @NotNull
    public Map<ChangeType, Integer> getTotalEntriesByType() {
        return totalEntriesByType;
    }
    
    @NotNull
    public Map<String, Integer> getCommitsByMonth() {
        return commitsByMonth;
    }
    
    @NotNull
    public Map<String, Integer> getReleasesByMonth() {
        return releasesByMonth;
    }
    
    @Nullable
    public HealthScore getHealthScore() {
        return healthScore;
    }
    
    // Computed statistics
    
    /**
     * Returns the total number of releases (excluding unreleased).
     */
    public int getReleaseCount() {
        return (int) releases.stream().filter(r -> !r.isUnreleased()).count();
    }
    
    /**
     * Returns the total number of changelog entries across all releases.
     */
    public int getTotalEntryCount() {
        return totalEntriesByType.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    /**
     * Returns the average number of days between releases.
     */
    public double getAverageReleaseCycleDays() {
        List<ReleaseInfo> datedReleases = releases.stream()
            .filter(r -> !r.isUnreleased() && r.getReleaseDate() != null)
            .sorted(Comparator.comparing(ReleaseInfo::getReleaseDate))
            .toList();
        
        if (datedReleases.size() < 2) return 0;
        
        long totalDays = 0;
        for (int i = 1; i < datedReleases.size(); i++) {
            LocalDate prev = datedReleases.get(i - 1).getReleaseDate();
            LocalDate curr = datedReleases.get(i).getReleaseDate();
            totalDays += ChronoUnit.DAYS.between(prev, curr);
        }
        
        return (double) totalDays / (datedReleases.size() - 1);
    }
    
    /**
     * Returns the percentage of commits that follow Conventional Commits format.
     */
    public double getConventionalCommitPercentage() {
        if (totalCommitCount == 0) return 0;
        return (conventionalCommitCount * 100.0) / totalCommitCount;
    }
    
    /**
     * Returns the percentage of commits that are documented in changelogs.
     */
    public double getDocumentationCoverage() {
        if (totalCommitCount == 0) return 100;
        int documented = totalCommitCount - undocumentedCommitCount;
        return (documented * 100.0) / totalCommitCount;
    }
    
    /**
     * Returns the number of active contributors.
     */
    public int getActiveContributorCount() {
        return contributors.size();
    }
    
    /**
     * Returns the "bus factor" - minimum contributors needed for 80% of commits.
     */
    public int getBusFactor() {
        if (contributors.isEmpty() || totalCommitCount == 0) return 0;
        
        List<ContributorStats> sorted = contributors.stream()
            .sorted(Comparator.comparingInt(ContributorStats::getCommitCount).reversed())
            .toList();
        
        int target = (int) (totalCommitCount * 0.8);
        int cumulative = 0;
        int count = 0;
        
        for (ContributorStats c : sorted) {
            cumulative += c.getCommitCount();
            count++;
            if (cumulative >= target) break;
        }
        
        return count;
    }
    
    /**
     * Returns the count of breaking changes across all releases.
     */
    public int getBreakingChangeCount() {
        return releases.stream()
            .mapToInt(r -> r.getBreakingChanges().size())
            .sum();
    }
    
    /**
     * Returns the most recent release info (excluding unreleased).
     */
    @Nullable
    public ReleaseInfo getLatestRelease() {
        return releases.stream()
            .filter(r -> !r.isUnreleased())
            .max(Comparator.comparing(r -> r.getReleaseDate() != null ? r.getReleaseDate() : LocalDate.MIN))
            .orElse(null);
    }
    
    /**
     * Returns change type distribution as percentages.
     */
    @NotNull
    public Map<ChangeType, Double> getChangeTypeDistribution() {
        Map<ChangeType, Double> distribution = new EnumMap<>(ChangeType.class);
        int total = getTotalEntryCount();
        
        if (total == 0) {
            for (ChangeType type : ChangeType.values()) {
                distribution.put(type, 0.0);
            }
            return distribution;
        }
        
        for (ChangeType type : ChangeType.values()) {
            int count = totalEntriesByType.getOrDefault(type, 0);
            distribution.put(type, (count * 100.0) / total);
        }
        
        return distribution;
    }
    
    @Override
    public String toString() {
        return String.format("AnalyticsData[releases=%d, commits=%d, contributors=%d]",
            getReleaseCount(), totalCommitCount, contributors.size());
    }
    
    /**
     * Builder for AnalyticsData.
     */
    public static class Builder {
        private String projectName;
        private String currentVersion;
        private String gitProvider;
        private final List<ReleaseInfo> releases = new ArrayList<>();
        private int unreleasedEntryCount = 0;
        private final List<CommitInfo> recentCommits = new ArrayList<>();
        private int totalCommitCount = 0;
        private int conventionalCommitCount = 0;
        private int undocumentedCommitCount = 0;
        private final List<ContributorStats> contributors = new ArrayList<>();
        private final Map<ChangeType, Integer> totalEntriesByType = new EnumMap<>(ChangeType.class);
        private final Map<String, Integer> commitsByMonth = new LinkedHashMap<>();
        private final Map<String, Integer> releasesByMonth = new LinkedHashMap<>();
        private HealthScore healthScore;
        
        public Builder projectName(String name) {
            this.projectName = name;
            return this;
        }
        
        public Builder currentVersion(String version) {
            this.currentVersion = version;
            return this;
        }
        
        public Builder gitProvider(String provider) {
            this.gitProvider = provider;
            return this;
        }
        
        public Builder addRelease(ReleaseInfo release) {
            this.releases.add(release);
            return this;
        }
        
        public Builder releases(List<ReleaseInfo> releases) {
            this.releases.addAll(releases);
            return this;
        }
        
        public Builder unreleasedEntryCount(int count) {
            this.unreleasedEntryCount = count;
            return this;
        }
        
        public Builder addRecentCommit(CommitInfo commit) {
            this.recentCommits.add(commit);
            return this;
        }
        
        public Builder recentCommits(List<CommitInfo> commits) {
            this.recentCommits.addAll(commits);
            return this;
        }
        
        public Builder totalCommitCount(int count) {
            this.totalCommitCount = count;
            return this;
        }
        
        public Builder conventionalCommitCount(int count) {
            this.conventionalCommitCount = count;
            return this;
        }
        
        public Builder undocumentedCommitCount(int count) {
            this.undocumentedCommitCount = count;
            return this;
        }
        
        public Builder addContributor(ContributorStats contributor) {
            this.contributors.add(contributor);
            return this;
        }
        
        public Builder contributors(List<ContributorStats> contributors) {
            this.contributors.addAll(contributors);
            return this;
        }
        
        public Builder addEntriesByType(ChangeType type, int count) {
            this.totalEntriesByType.merge(type, count, Integer::sum);
            return this;
        }
        
        public Builder addCommitsByMonth(String month, int count) {
            this.commitsByMonth.put(month, count);
            return this;
        }
        
        public Builder addReleasesByMonth(String month, int count) {
            this.releasesByMonth.put(month, count);
            return this;
        }
        
        public Builder healthScore(HealthScore score) {
            this.healthScore = score;
            return this;
        }
        
        public AnalyticsData build() {
            // Sort releases (newest first)
            releases.sort(Comparator.naturalOrder());
            // Sort contributors (most active first)
            contributors.sort(Comparator.naturalOrder());
            return new AnalyticsData(this);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
