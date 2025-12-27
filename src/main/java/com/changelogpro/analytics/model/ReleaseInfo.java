package com.changelogpro.analytics.model;

import com.changelogpro.config.ChangeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.*;

/**
 * Represents a release/version with its changelog entries and metadata.
 * Stores information parsed from CHANGELOG.md or generated from fragments.
 */
public class ReleaseInfo implements Comparable<ReleaseInfo> {
    
    private final String version;
    private final LocalDate releaseDate;
    private final boolean isUnreleased;
    private final Map<ChangeType, List<String>> entriesByType;
    private final List<String> breakingChanges;
    private final String tagName;
    
    private ReleaseInfo(Builder builder) {
        this.version = builder.version;
        this.releaseDate = builder.releaseDate;
        this.isUnreleased = builder.isUnreleased;
        this.entriesByType = Collections.unmodifiableMap(new LinkedHashMap<>(builder.entriesByType));
        this.breakingChanges = Collections.unmodifiableList(new ArrayList<>(builder.breakingChanges));
        this.tagName = builder.tagName;
    }
    
    // Getters
    
    @NotNull
    public String getVersion() {
        return version;
    }
    
    @Nullable
    public LocalDate getReleaseDate() {
        return releaseDate;
    }
    
    public boolean isUnreleased() {
        return isUnreleased;
    }
    
    @NotNull
    public Map<ChangeType, List<String>> getEntriesByType() {
        return entriesByType;
    }
    
    @NotNull
    public List<String> getBreakingChanges() {
        return breakingChanges;
    }
    
    @Nullable
    public String getTagName() {
        return tagName;
    }
    
    /**
     * Returns total count of entries across all types.
     */
    public int getTotalEntryCount() {
        return entriesByType.values().stream()
            .mapToInt(List::size)
            .sum();
    }
    
    /**
     * Returns count of entries for a specific type.
     */
    public int getEntryCount(ChangeType type) {
        List<String> entries = entriesByType.get(type);
        return entries != null ? entries.size() : 0;
    }
    
    /**
     * Returns true if this release has breaking changes.
     */
    public boolean hasBreakingChanges() {
        return !breakingChanges.isEmpty();
    }
    
    /**
     * Returns a summary string showing counts by type.
     * Example: "3 Added, 2 Fixed, 1 Security"
     */
    public String getSummary() {
        List<String> parts = new ArrayList<>();
        for (ChangeType type : ChangeType.values()) {
            int count = getEntryCount(type);
            if (count > 0) {
                parts.add(count + " " + type.getDisplayName());
            }
        }
        return String.join(", ", parts);
    }
    
    /**
     * Returns a map of entry counts by type.
     */
    @NotNull
    public Map<ChangeType, Integer> getEntryCounts() {
        Map<ChangeType, Integer> counts = new EnumMap<>(ChangeType.class);
        for (ChangeType type : ChangeType.values()) {
            counts.put(type, getEntryCount(type));
        }
        return counts;
    }
    
    @Override
    public int compareTo(@NotNull ReleaseInfo other) {
        // Unreleased always comes first
        if (this.isUnreleased && !other.isUnreleased) return -1;
        if (!this.isUnreleased && other.isUnreleased) return 1;
        
        // Then sort by date (newest first)
        if (this.releaseDate != null && other.releaseDate != null) {
            return other.releaseDate.compareTo(this.releaseDate);
        }
        
        // Fall back to version string comparison
        return compareVersions(other.version, this.version);
    }
    
    /**
     * Simple semantic version comparison.
     */
    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.replaceAll("[^0-9.]", "").split("\\.");
        String[] parts2 = v2.replaceAll("[^0-9.]", "").split("\\.");
        
        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? parseVersionPart(parts1[i]) : 0;
            int num2 = i < parts2.length ? parseVersionPart(parts2[i]) : 0;
            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return 0;
    }
    
    private int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReleaseInfo that = (ReleaseInfo) o;
        return Objects.equals(version, that.version);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(version);
    }
    
    @Override
    public String toString() {
        String dateStr = releaseDate != null ? " (" + releaseDate + ")" : "";
        return version + dateStr + " - " + getTotalEntryCount() + " entries";
    }
    
    /**
     * Builder for ReleaseInfo.
     */
    public static class Builder {
        private String version;
        private LocalDate releaseDate;
        private boolean isUnreleased = false;
        private final Map<ChangeType, List<String>> entriesByType = new LinkedHashMap<>();
        private final List<String> breakingChanges = new ArrayList<>();
        private String tagName;
        
        public Builder version(String version) {
            this.version = version;
            return this;
        }
        
        public Builder releaseDate(LocalDate date) {
            this.releaseDate = date;
            return this;
        }
        
        public Builder unreleased(boolean isUnreleased) {
            this.isUnreleased = isUnreleased;
            return this;
        }
        
        public Builder tagName(String tagName) {
            this.tagName = tagName;
            return this;
        }
        
        public Builder addEntry(ChangeType type, String description) {
            entriesByType.computeIfAbsent(type, k -> new ArrayList<>()).add(description);
            return this;
        }
        
        public Builder addEntries(ChangeType type, List<String> descriptions) {
            entriesByType.computeIfAbsent(type, k -> new ArrayList<>()).addAll(descriptions);
            return this;
        }
        
        public Builder addBreakingChange(String description) {
            breakingChanges.add(description);
            return this;
        }
        
        public ReleaseInfo build() {
            Objects.requireNonNull(version, "version is required");
            return new ReleaseInfo(this);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
