package com.changelogpro.analytics.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for HealthScore model.
 */
class HealthScoreTest {

    // ========== Basic Score Tests ==========

    @Test
    void testBuildBasicScore() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Test Criterion", "Description", 15, 20)
                .build();

        assertThat(score.getTotalScore()).isEqualTo(15);
        assertThat(score.getMaxPossibleScore()).isEqualTo(20);
        assertThat(score.getScorePercentage()).isEqualTo(75);
    }

    @Test
    void testMultipleCriteria() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Criterion 1", "Desc 1", 18, 20)
                .addCriterion("Criterion 2", "Desc 2", 12, 15)
                .addCriterion("Criterion 3", "Desc 3", 8, 10)
                .build();

        assertThat(score.getTotalScore()).isEqualTo(38);
        assertThat(score.getMaxPossibleScore()).isEqualTo(45);
        assertThat(score.getCriteria()).hasSize(3);
    }

    @Test
    void testScoreClampedToMax() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Over Score", "Score exceeds max", 25, 20)
                .build();

        // Score should be clamped to max
        assertThat(score.getCriteria().getFirst().getScore()).isEqualTo(20);
        assertThat(score.getTotalScore()).isEqualTo(20);
    }

    // ========== Grade Tests ==========

    @Test
    void testGradeExcellent() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Perfect", "100%", 100, 100)
                .build();

        assertThat(score.getGrade()).isEqualTo("EXCELLENT");
    }

    @Test
    void testGradeExcellentAt90() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Great", "90%", 90, 100)
                .build();

        assertThat(score.getGrade()).isEqualTo("EXCELLENT");
    }

    @Test
    void testGradeGood() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Good", "85%", 85, 100)
                .build();

        assertThat(score.getGrade()).isEqualTo("GOOD");
    }

    @Test
    void testGradeFair() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Fair", "65%", 65, 100)
                .build();

        assertThat(score.getGrade()).isEqualTo("FAIR");
    }

    @Test
    void testGradePoor() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Poor", "45%", 45, 100)
                .build();

        assertThat(score.getGrade()).isEqualTo("POOR");
    }

    @Test
    void testGradeCritical() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Critical", "20%", 20, 100)
                .build();

        assertThat(score.getGrade()).isEqualTo("CRITICAL");
    }

    // ========== Grade Color Tests ==========

    @Test
    void testGradeColorIsHex() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Test", "Test", 50, 100)
                .build();

        assertThat(score.getGradeColor()).startsWith("#");
        assertThat(score.getGradeColor()).hasSize(7);
    }

    @Test
    void testDifferentGradesHaveDifferentColors() {
        HealthScore excellent = HealthScore.builder()
                .addCriterion("Excellent", "", 95, 100).build();
        HealthScore good = HealthScore.builder()
                .addCriterion("Good", "", 85, 100).build();
        HealthScore poor = HealthScore.builder()
                .addCriterion("Poor", "", 45, 100).build();

        assertThat(excellent.getGradeColor()).isNotEqualTo(good.getGradeColor());
        assertThat(good.getGradeColor()).isNotEqualTo(poor.getGradeColor());
    }

    // ========== Criterion Tests ==========

    @Test
    void testCriterionPassed() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Passing", "Above 80%", 17, 20)
                .build();

        HealthScore.Criterion criterion = score.getCriteria().getFirst();
        assertThat(criterion.isPassed()).isTrue();
        assertThat(criterion.getIcon()).isEqualTo("✅");
    }

    @Test
    void testCriterionWarning() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Warning", "Between 50-80%", 12, 20)
                .build();

        HealthScore.Criterion criterion = score.getCriteria().getFirst();
        assertThat(criterion.isPassed()).isFalse();
        assertThat(criterion.getIcon()).isEqualTo("⚠️");
    }

    @Test
    void testCriterionFailed() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Failed", "Below 50%", 5, 20)
                .build();

        HealthScore.Criterion criterion = score.getCriteria().getFirst();
        assertThat(criterion.isPassed()).isFalse();
        assertThat(criterion.getIcon()).isEqualTo("❌");
    }

    @Test
    void testCriterionPercentage() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Test", "Test", 15, 20)
                .build();

        HealthScore.Criterion criterion = score.getCriteria().getFirst();
        assertThat(criterion.getPercentage()).isEqualTo(75.0);
    }

    @Test
    void testPassingCriteriaCount() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Pass 1", "", 18, 20)
                .addCriterion("Pass 2", "", 16, 20)
                .addCriterion("Fail 1", "", 5, 20)
                .addCriterion("Fail 2", "", 10, 20)
                .build();

        assertThat(score.getPassingCriteriaCount()).isEqualTo(2);
    }

    // ========== Recommendation Tests ==========

    @Test
    void testAddRecommendation() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Test", "", 50, 100)
                .addRecommendation(
                        HealthScore.Severity.HIGH,
                        "Improve coverage",
                        "Add more tests",
                        "Run Tests",
                        "run_tests"
                )
                .build();

        assertThat(score.getRecommendations()).hasSize(1);

        HealthScore.Recommendation rec = score.getRecommendations().getFirst();
        assertThat(rec.getSeverity()).isEqualTo(HealthScore.Severity.HIGH);
        assertThat(rec.getTitle()).isEqualTo("Improve coverage");
        assertThat(rec.getDescription()).isEqualTo("Add more tests");
        assertThat(rec.getActionLabel()).isEqualTo("Run Tests");
        assertThat(rec.getActionId()).isEqualTo("run_tests");
    }

    @Test
    void testMultipleRecommendations() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Test", "", 50, 100)
                .addRecommendation(HealthScore.Severity.HIGH, "High", "", "", "")
                .addRecommendation(HealthScore.Severity.MEDIUM, "Medium", "", "", "")
                .addRecommendation(HealthScore.Severity.LOW, "Low", "", "", "")
                .build();

        assertThat(score.getRecommendations()).hasSize(3);
    }

    @Test
    void testHighPriorityCount() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Test", "", 50, 100)
                .addRecommendation(HealthScore.Severity.HIGH, "H1", "", "", "")
                .addRecommendation(HealthScore.Severity.HIGH, "H2", "", "", "")
                .addRecommendation(HealthScore.Severity.MEDIUM, "M1", "", "", "")
                .addRecommendation(HealthScore.Severity.LOW, "L1", "", "", "")
                .build();

        assertThat(score.getHighPriorityCount()).isEqualTo(2);
    }

    @Test
    void testNoRecommendations() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Perfect", "", 100, 100)
                .build();

        assertThat(score.getRecommendations()).isEmpty();
        assertThat(score.getHighPriorityCount()).isEqualTo(0);
    }

    // ========== Severity Tests ==========

    @Test
    void testSeverityIcons() {
        assertThat(HealthScore.Severity.HIGH.getIcon()).isEqualTo("🔴");
        assertThat(HealthScore.Severity.MEDIUM.getIcon()).isEqualTo("🟡");
        assertThat(HealthScore.Severity.LOW.getIcon()).isEqualTo("🟢");
    }

    @Test
    void testSeverityDescriptions() {
        assertThat(HealthScore.Severity.HIGH.getDescription()).contains("High");
        assertThat(HealthScore.Severity.MEDIUM.getDescription()).contains("Medium");
        assertThat(HealthScore.Severity.LOW.getDescription()).contains("Low");
    }

    // ========== Edge Cases ==========

    @Test
    void testEmptyScore() {
        HealthScore score = HealthScore.builder().build();

        assertThat(score.getTotalScore()).isEqualTo(0);
        assertThat(score.getMaxPossibleScore()).isEqualTo(0);
        assertThat(score.getScorePercentage()).isEqualTo(0);
        assertThat(score.getCriteria()).isEmpty();
    }

    @Test
    void testZeroMaxScore() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Zero Max", "Edge case", 0, 0)
                .build();

        HealthScore.Criterion criterion = score.getCriteria().getFirst();
        assertThat(criterion.getPercentage()).isEqualTo(0.0);
    }

    // ========== Immutability Tests ==========

    @Test
    void testCriteriaListIsImmutable() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Test", "", 50, 100)
                .build();

        assertThatThrownBy(() ->
                score.getCriteria().add(new HealthScore.Criterion("New", "", 10, 20))
        ).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testRecommendationsListIsImmutable() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Test", "", 50, 100)
                .addRecommendation(HealthScore.Severity.LOW, "Test", "", "", "")
                .build();

        assertThatThrownBy(() ->
                score.getRecommendations().add(
                        new HealthScore.Recommendation(HealthScore.Severity.HIGH, "New", "", "", "")
                )
        ).isInstanceOf(UnsupportedOperationException.class);
    }

    // ========== ToString Tests ==========

    @Test
    void testToString() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Test 1", "", 80, 100)
                .addCriterion("Test 2", "", 5, 10)
                .build();

        String str = score.toString();
        assertThat(str).contains("85");
        assertThat(str).contains("110");
        assertThat(str).contains("FAIR"); // 85/110 = 77%
    }

    @Test
    void testCriterionToString() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Coverage", "Test coverage", 15, 20)
                .build();

        String str = score.getCriteria().get(0).toString();
        assertThat(str).contains("Coverage");
        assertThat(str).contains("15/20");
    }

    @Test
    void testRecommendationToString() {
        HealthScore score = HealthScore.builder()
                .addCriterion("Test", "", 50, 100)
                .addRecommendation(HealthScore.Severity.HIGH, "Fix Bug", "Critical issue", "", "")
                .build();

        String str = score.getRecommendations().get(0).toString();
        assertThat(str).contains("🔴");
        assertThat(str).contains("Fix Bug");
    }
}
