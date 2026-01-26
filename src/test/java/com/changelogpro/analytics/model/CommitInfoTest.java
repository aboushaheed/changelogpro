package com.changelogpro.analytics.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for CommitInfo model.
 */
class CommitInfoTest {

    // ========== Builder Tests ==========

    @Test
    void testBuildBasicCommit() {
        LocalDateTime now = LocalDateTime.now();

        CommitInfo commit = CommitInfo.builder()
                .hash("abc123def456789")
                .author("John Doe")
                .authorEmail("john@example.com")
                .dateTime(now)
                .subject("Add new feature")
                .build();

        assertThat(commit.getHash()).isEqualTo("abc123def456789");
        assertThat(commit.getShortHash()).isEqualTo("abc123d");
        assertThat(commit.getAuthor()).isEqualTo("John Doe");
        assertThat(commit.getAuthorEmail()).isEqualTo("john@example.com");
        assertThat(commit.getDateTime()).isEqualTo(now);
        assertThat(commit.getSubject()).isEqualTo("Add new feature");
    }

    @Test
    void testShortHashIsSevenCharacters() {
        CommitInfo commit = CommitInfo.builder()
                .hash("abcdef1234567890abcdef1234567890")
                .author("Test")
                .dateTime(LocalDateTime.now())
                .subject("Test commit")
                .build();

        assertThat(commit.getShortHash()).hasSize(7);
        assertThat(commit.getShortHash()).isEqualTo("abcdef1");
    }

    @Test
    void testShortHashForShortInput() {
        CommitInfo commit = CommitInfo.builder()
                .hash("abc")
                .author("Test")
                .dateTime(LocalDateTime.now())
                .subject("Test commit")
                .build();

        assertThat(commit.getShortHash()).isEqualTo("abc");
    }

    // ========== Conventional Commit Tests ==========

    @Test
    void testConventionalCommitType() {
        CommitInfo commit = CommitInfo.builder()
                .hash("abc123")
                .author("Test")
                .dateTime(LocalDateTime.now())
                .subject("feat: Add new feature")
                .conventionalType("feat")
                .build();

        assertThat(commit.isConventionalCommit()).isTrue();
        assertThat(commit.getConventionalType()).isEqualTo("feat");
    }

    @Test
    void testConventionalCommitWithScope() {
        CommitInfo commit = CommitInfo.builder()
                .hash("abc123")
                .author("Test")
                .dateTime(LocalDateTime.now())
                .subject("feat(api): Add new endpoint")
                .conventionalType("feat")
                .conventionalScope("api")
                .build();

        assertThat(commit.isConventionalCommit()).isTrue();
        assertThat(commit.getConventionalType()).isEqualTo("feat");
        assertThat(commit.getConventionalScope()).isEqualTo("api");
    }

    @Test
    void testBreakingChange() {
        CommitInfo commit = CommitInfo.builder()
                .hash("abc123")
                .author("Test")
                .dateTime(LocalDateTime.now())
                .subject("feat!: Breaking API change")
                .conventionalType("feat")
                .breakingChange(true)
                .build();

        assertThat(commit.isBreakingChange()).isTrue();
    }

    @Test
    void testNonConventionalCommit() {
        CommitInfo commit = CommitInfo.builder()
                .hash("abc123")
                .author("Test")
                .dateTime(LocalDateTime.now())
                .subject("Updated the README file")
                .build();

        assertThat(commit.isConventionalCommit()).isFalse();
        assertThat(commit.getConventionalType()).isNull();
    }

    // ========== Issue Reference Tests ==========

    @Test
    void testIssueReference() {
        CommitInfo commit = CommitInfo.builder()
                .hash("abc123")
                .author("Test")
                .dateTime(LocalDateTime.now())
                .subject("Fix bug #123")
                .issueReference("#123")
                .build();

        assertThat(commit.getIssueReference()).isEqualTo("#123");
    }

    @Test
    void testJiraIssueReference() {
        CommitInfo commit = CommitInfo.builder()
                .hash("abc123")
                .author("Test")
                .dateTime(LocalDateTime.now())
                .subject("JIRA-456: Fix critical bug")
                .issueReference("JIRA-456")
                .build();

        assertThat(commit.getIssueReference()).isEqualTo("JIRA-456");
    }

    // ========== Merge Commit Tests ==========

    @Test
    void testMergeCommit() {
        CommitInfo commit = CommitInfo.builder()
                .hash("abc123")
                .author("Test")
                .dateTime(LocalDateTime.now())
                .subject("Merge branch 'feature' into main")
                .mergeCommit(true)
                .build();

        assertThat(commit.isMergeCommit()).isTrue();
    }

    @Test
    void testNonMergeCommit() {
        CommitInfo commit = CommitInfo.builder()
                .hash("abc123")
                .author("Test")
                .dateTime(LocalDateTime.now())
                .subject("Regular commit")
                .mergeCommit(false)
                .build();

        assertThat(commit.isMergeCommit()).isFalse();
    }

    // ========== Equality Tests ==========

    @Test
    void testEqualsWithSameHash() {
        LocalDateTime now = LocalDateTime.now();

        CommitInfo commit1 = CommitInfo.builder()
                .hash("abc123")
                .author("Author 1")
                .dateTime(now)
                .subject("Subject 1")
                .build();

        CommitInfo commit2 = CommitInfo.builder()
                .hash("abc123")
                .author("Author 2")
                .dateTime(now.plusDays(1))
                .subject("Subject 2")
                .build();

        assertThat(commit1).isEqualTo(commit2);
        assertThat(commit1.hashCode()).isEqualTo(commit2.hashCode());
    }

    @Test
    void testNotEqualsWithDifferentHash() {
        LocalDateTime now = LocalDateTime.now();

        CommitInfo commit1 = CommitInfo.builder()
                .hash("abc123")
                .author("Author")
                .dateTime(now)
                .subject("Subject")
                .build();

        CommitInfo commit2 = CommitInfo.builder()
                .hash("def456")
                .author("Author")
                .dateTime(now)
                .subject("Subject")
                .build();

        assertThat(commit1).isNotEqualTo(commit2);
    }

    // ========== ToString Tests ==========

    @Test
    void testToString() {
        CommitInfo commit = CommitInfo.builder()
                .hash("abc123def456789")
                .author("John Doe")
                .dateTime(LocalDateTime.now())
                .subject("Add new feature")
                .build();

        String str = commit.toString();
        assertThat(str).contains("abc123d");
        assertThat(str).contains("John Doe");
        assertThat(str).contains("Add new feature");
    }

    // ========== Validation Tests ==========

    @Test
    void testBuildWithoutHash() {
        assertThatThrownBy(() ->
                CommitInfo.builder()
                        .author("Test")
                        .dateTime(LocalDateTime.now())
                        .subject("Test")
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void testBuildWithoutAuthor() {
        assertThatThrownBy(() ->
                CommitInfo.builder()
                        .hash("abc123")
                        .dateTime(LocalDateTime.now())
                        .subject("Test")
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void testBuildWithoutDateTime() {
        assertThatThrownBy(() ->
                CommitInfo.builder()
                        .hash("abc123")
                        .author("Test")
                        .subject("Test")
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void testBuildWithoutSubject() {
        assertThatThrownBy(() ->
                CommitInfo.builder()
                        .hash("abc123")
                        .author("Test")
                        .dateTime(LocalDateTime.now())
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    // ========== Body Tests ==========

    @Test
    void testCommitWithBody() {
        CommitInfo commit = CommitInfo.builder()
                .hash("abc123")
                .author("Test")
                .dateTime(LocalDateTime.now())
                .subject("feat: Add feature")
                .body("This commit adds a new feature.\n\nIt includes multiple changes.")
                .build();

        assertThat(commit.getBody()).contains("multiple changes");
    }

    @Test
    void testCommitWithoutBody() {
        CommitInfo commit = CommitInfo.builder()
                .hash("abc123")
                .author("Test")
                .dateTime(LocalDateTime.now())
                .subject("Quick fix")
                .build();

        assertThat(commit.getBody()).isNull();
    }
}
