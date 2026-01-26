package com.changelogpro.analytics.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the health score of a project's changelog management.
 * Provides a composite score based on multiple criteria with recommendations.
 */
public class HealthScore {
    
    /**
     * Severity levels for recommendations.
     */
    public enum Severity {
        HIGH("🔴", "High priority"),
        MEDIUM("🟡", "Medium priority"),
        LOW("🟢", "Low priority");
        
        private final String icon;
        private final String description;
        
        Severity(String icon, String description) {
            this.icon = icon;
            this.description = description;
        }
        
        public String getIcon() { return icon; }
        public String getDescription() { return description; }
    }
    
    /**
     * A recommendation for improving the project health.
     */
    public static class Recommendation {
        private final Severity severity;
        private final String title;
        private final String description;
        private final String actionLabel;
        private final String actionId;
        
        public Recommendation(Severity severity, String title, String description, 
                              String actionLabel, String actionId) {
            this.severity = severity;
            this.title = title;
            this.description = description;
            this.actionLabel = actionLabel;
            this.actionId = actionId;
        }
        
        public Severity getSeverity() { return severity; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getActionLabel() { return actionLabel; }
        public String getActionId() { return actionId; }
        
        @Override
        public String toString() {
            return severity.getIcon() + " " + title;
        }
    }
    
    /**
     * A single criterion in the health score.
     */
    public static class Criterion {
        private final String name;
        private final String description;
        private final int score;
        private final int maxScore;
        private final boolean passed;
        
        public Criterion(String name, String description, int score, int maxScore) {
            this.name = name;
            this.description = description;
            this.score = Math.min(score, maxScore);
            this.maxScore = maxScore;
            this.passed = score >= maxScore * 0.8; // 80% threshold for "passed"
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getScore() { return score; }
        public int getMaxScore() { return maxScore; }
        public boolean isPassed() { return passed; }
        
        public String getIcon() {
            if (passed) return "✅";
            if (score >= maxScore * 0.5) return "⚠️";
            return "❌";
        }
        
        public double getPercentage() {
            return maxScore > 0 ? (score * 100.0) / maxScore : 0;
        }
        
        @Override
        public String toString() {
            return String.format("%s %s: %d/%d", getIcon(), name, score, maxScore);
        }
    }
    
    // Score criteria
    private final List<Criterion> criteria;
    private final List<Recommendation> recommendations;
    
    // Computed values
    private final int totalScore;
    private final int maxPossibleScore;
    
    private HealthScore(Builder builder) {
        this.criteria = Collections.unmodifiableList(new ArrayList<>(builder.criteria));
        this.recommendations = Collections.unmodifiableList(new ArrayList<>(builder.recommendations));
        
        this.totalScore = criteria.stream().mapToInt(Criterion::getScore).sum();
        this.maxPossibleScore = criteria.stream().mapToInt(Criterion::getMaxScore).sum();
    }
    
    // Getters
    
    @NotNull
    public List<Criterion> getCriteria() {
        return criteria;
    }
    
    @NotNull
    public List<Recommendation> getRecommendations() {
        return recommendations;
    }
    
    public int getTotalScore() {
        return totalScore;
    }
    
    public int getMaxPossibleScore() {
        return maxPossibleScore;
    }
    
    /**
     * Returns the score as a percentage (0-100).
     */
    public int getScorePercentage() {
        return maxPossibleScore > 0 ? (totalScore * 100) / maxPossibleScore : 0;
    }
    
    /**
     * Returns a grade based on the score percentage.
     */
    @NotNull
    public String getGrade() {
        int pct = getScorePercentage();
        if (pct >= 90) return "EXCELLENT";
        if (pct >= 80) return "GOOD";
        if (pct >= 60) return "FAIR";
        if (pct >= 40) return "POOR";
        return "CRITICAL";
    }
    
    /**
     * Returns a color hex code for the grade.
     */
    @NotNull
    public String getGradeColor() {
        int pct = getScorePercentage();
        if (pct >= 90) return "#1a7f37"; // Green
        if (pct >= 80) return "#3fb950"; // Light green
        if (pct >= 60) return "#bf8700"; // Yellow
        if (pct >= 40) return "#cf222e"; // Red
        return "#8b0000"; // Dark red
    }
    
    /**
     * Returns the number of passing criteria.
     */
    public int getPassingCriteriaCount() {
        return (int) criteria.stream().filter(Criterion::isPassed).count();
    }
    
    /**
     * Returns the number of high-priority recommendations.
     */
    public int getHighPriorityCount() {
        return (int) recommendations.stream()
            .filter(r -> r.getSeverity() == Severity.HIGH)
            .count();
    }
    
    @Override
    public String toString() {
        return String.format("Health Score: %d/%d (%s)", totalScore, maxPossibleScore, getGrade());
    }
    
    /**
     * Builder for HealthScore.
     */
    public static class Builder {
        private final List<Criterion> criteria = new ArrayList<>();
        private final List<Recommendation> recommendations = new ArrayList<>();
        
        public Builder addCriterion(String name, String description, int score, int maxScore) {
            criteria.add(new Criterion(name, description, score, maxScore));
            return this;
        }
        
        public Builder addRecommendation(Severity severity, String title, String description,
                                         String actionLabel, String actionId) {
            recommendations.add(new Recommendation(severity, title, description, actionLabel, actionId));
            return this;
        }
        
        public HealthScore build() {
            return new HealthScore(this);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
