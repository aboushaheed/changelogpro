package com.changelogpro.analytics.model;

import com.changelogpro.config.ChangeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.*;

/**
 * Represents statistics for a single contributor.
 * Tracks commits, changelog entries, and activity patterns.
 */
public class ContributorStats implements Comparable<ContributorStats> {
    
    private final String name;
    private final String email;
    private int commitCount;
    private int changelogEntryCount;
    private final Map<ChangeType, Integer> entriesByType;
    private final Map<String, Integer> conventionalTypeCount;
    private LocalDate firstContribution;
    private LocalDate lastContribution;
    private final Map<LocalDate, Integer> activityByDate;
    
    public ContributorStats(@NotNull String name, @Nullable String email) {
        this.name = name;
        this.email = email;
        this.commitCount = 0;
        this.changelogEntryCount = 0;
        this.entriesByType = new EnumMap<>(ChangeType.class);
        this.conventionalTypeCount = new HashMap<>();
        this.activityByDate = new TreeMap<>();
    }
    
    // Mutation methods
    
    /**
     * Increments the commit count and updates activity tracking.
     */
    public void addCommit(@NotNull LocalDate date, @Nullable String conventionalType) {
        commitCount++;
        activityByDate.merge(date, 1, Integer::sum);
        
        if (conventionalType != null) {
            conventionalTypeCount.merge(conventionalType.toLowerCase(), 1, Integer::sum);
        }
        
        if (firstContribution == null || date.isBefore(firstContribution)) {
            firstContribution = date;
        }
        if (lastContribution == null || date.isAfter(lastContribution)) {
            lastContribution = date;
        }
    }
    
    /**
     * Adds a changelog entry contribution.
     */
    public void addChangelogEntry(@NotNull ChangeType type) {
        changelogEntryCount++;
        entriesByType.merge(type, 1, Integer::sum);
    }
    
    // Getters
    
    @NotNull
    public String getName() {
        return name;
    }
    
    @Nullable
    public String getEmail() {
        return email;
    }
    
    public int getCommitCount() {
        return commitCount;
    }
    
    public int getChangelogEntryCount() {
        return changelogEntryCount;
    }
    
    @NotNull
    public Map<ChangeType, Integer> getEntriesByType() {
        return Collections.unmodifiableMap(entriesByType);
    }
    
    @NotNull
    public Map<String, Integer> getConventionalTypeCount() {
        return Collections.unmodifiableMap(conventionalTypeCount);
    }
    
    @Nullable
    public LocalDate getFirstContribution() {
        return firstContribution;
    }
    
    @Nullable
    public LocalDate getLastContribution() {
        return lastContribution;
    }
    
    @NotNull
    public Map<LocalDate, Integer> getActivityByDate() {
        return Collections.unmodifiableMap(activityByDate);
    }
    
    /**
     * Returns the number of entries for a specific change type.
     */
    public int getEntryCount(ChangeType type) {
        return entriesByType.getOrDefault(type, 0);
    }
    
    /**
     * Returns the primary contribution type based on conventional commits.
     */
    @Nullable
    public String getPrimaryConventionalType() {
        return conventionalTypeCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    /**
     * Returns the percentage of conventional commits for this contributor.
     */
    public double getConventionalCommitPercentage() {
        if (commitCount == 0) return 0.0;
        int conventionalCount = conventionalTypeCount.values().stream()
            .mapToInt(Integer::intValue)
            .sum();
        return (conventionalCount * 100.0) / commitCount;
    }
    
    /**
     * Returns the number of active days (days with at least one commit).
     */
    public int getActiveDays() {
        return activityByDate.size();
    }
    
    /**
     * Returns activity for a specific week (Monday to Sunday).
     * Index 0 = Monday, 6 = Sunday.
     */
    public int[] getWeeklyPattern() {
        int[] pattern = new int[7];
        for (Map.Entry<LocalDate, Integer> entry : activityByDate.entrySet()) {
            int dayOfWeek = entry.getKey().getDayOfWeek().getValue() - 1; // 0-indexed
            pattern[dayOfWeek] += entry.getValue();
        }
        return pattern;
    }
    
    @Override
    public int compareTo(@NotNull ContributorStats other) {
        // Sort by total contribution (commits + entries), descending
        int thisTotal = this.commitCount + this.changelogEntryCount;
        int otherTotal = other.commitCount + other.changelogEntryCount;
        return Integer.compare(otherTotal, thisTotal);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContributorStats that = (ContributorStats) o;
        return Objects.equals(name, that.name) && Objects.equals(email, that.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, email);
    }
    
    @Override
    public String toString() {
        return String.format("%s: %d commits, %d entries", name, commitCount, changelogEntryCount);
    }
}
