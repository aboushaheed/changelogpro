package com.changelogpro.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ChangeEntry class.
 */
class ChangeEntryTest {

    private ChangeEntry entry;

    @BeforeEach
    void setUp() {
        entry = new ChangeEntry();
    }

    // ========== Constructor Tests ==========

    @Test
    void testDefaultConstructor() {
        ChangeEntry newEntry = new ChangeEntry();
        assertNotNull(newEntry);

        assertEquals(ChangeType.ADDED, newEntry.getType()); // Default type
        assertNotNull(newEntry.getId());
        assertNotNull(newEntry.getCreatedAt());
        assertTrue(newEntry.getTimestamp() > 0);
    }

    @Test
    void testTypeConstructor() {
        ChangeEntry newEntry = new ChangeEntry(ChangeType.FIXED);
        assertEquals(ChangeType.FIXED, newEntry.getType());
    }

    @Test
    void testFullConstructor() {
        ChangeEntry newEntry = new ChangeEntry(
                ChangeType.ADDED,
                "New feature description",
                "PROJ-123",
                "456",
                "developer"
        );

        assertEquals(ChangeType.ADDED, newEntry.getType());
        assertEquals("New feature description", newEntry.getDescription());
        assertEquals("PROJ-123", newEntry.getIssueId());
        assertEquals("456", newEntry.getPrNumber());
        assertEquals("developer", newEntry.getAuthor());
    }

    @Test
    void testFullConstructorWithNulls() {
        ChangeEntry newEntry = new ChangeEntry(
                ChangeType.FIXED,
                null,
                null,
                null,
                null
        );

        assertEquals(ChangeType.FIXED, newEntry.getType());
        assertEquals("", newEntry.getDescription());
        assertEquals("", newEntry.getIssueId());
        assertEquals("", newEntry.getPrNumber());
        assertEquals("", newEntry.getAuthor());
    }

    // ========== Getter/Setter Tests ==========

    @Test
    void testSetAndGetType() {
        entry.setType(ChangeType.FIXED);
        assertEquals(ChangeType.FIXED, entry.getType());
    }

    @Test
    void testSetAndGetDescription() {
        entry.setDescription("Bug fix description");
        assertEquals("Bug fix description", entry.getDescription());
    }

    @Test
    void testSetAndGetIssueId() {
        entry.setIssueId("PROJ-456");
        assertEquals("PROJ-456", entry.getIssueId());
    }

    @Test
    void testSetAndGetPrNumber() {
        entry.setPrNumber("789");
        assertEquals("789", entry.getPrNumber());
    }

    @Test
    void testSetAndGetAuthor() {
        entry.setAuthor("john.doe");
        assertEquals("john.doe", entry.getAuthor());
    }

    @Test
    void testSetAndGetTimestamp() {
        long timestamp = System.currentTimeMillis();
        entry.setTimestamp(timestamp);
        assertEquals(timestamp, entry.getTimestamp());
    }

    @Test
    void testSetAndGetFilePath() {
        entry.setFilePath("/path/to/file.yaml");
        assertEquals("/path/to/file.yaml", entry.getFilePath());
    }

    // ========== ID Generation Tests ==========

    @Test
    void testIdIsGenerated() {
        assertNotNull(entry.getId());
        assertThat(entry.getId()).hasSize(8);
    }

    @Test
    void testIdIsUnique() {
        ChangeEntry entry1 = new ChangeEntry();
        ChangeEntry entry2 = new ChangeEntry();
        assertNotEquals(entry1.getId(), entry2.getId());
    }

    // ========== Filename Generation Tests ==========

    @Test
    void testGenerateFileName() {
        entry.setDescription("New feature");
        String fileName = entry.generateFileName();

        assertThat(fileName).endsWith(".yaml");
        assertThat(fileName).contains(entry.getId());
        assertThat(fileName).contains("new_feature");
    }

    @Test
    void testGenerateFileNameWithSpecialChars() {
        entry.setDescription("Fix: bug with special chars! @#$%");
        String fileName = entry.generateFileName();

        assertThat(fileName).endsWith(".yaml");
        assertThat(fileName).doesNotContain("@");
        assertThat(fileName).doesNotContain("#");
        assertThat(fileName).doesNotContain("$");
    }

    @Test
    void testGenerateFileNameWithLongDescription() {
        entry.setDescription("This is a very long description that should be truncated to fit in a reasonable filename length");
        String fileName = entry.generateFileName();

        assertThat(fileName).endsWith(".yaml");
        // Description part should be truncated
        assertThat(fileName.length()).isLessThan(100);
    }

    @Test
    void testGenerateFileNameWithEmptyDescription() {
        entry.setDescription("");
        String fileName = entry.generateFileName();

        assertThat(fileName).endsWith(".yaml");
        assertThat(fileName).contains("entry");
    }

    // ========== All Change Types Tests ==========

    @Test
    void testAllChangeTypesCanBeSet() {
        for (ChangeType type : ChangeType.values()) {
            entry.setType(type);
            assertEquals(type, entry.getType());
        }
    }

    // ========== Formatted Date Tests ==========

    @Test
    void testGetFormattedDate() {
        String formattedDate = entry.getFormattedDate();
        assertNotNull(formattedDate);
        assertThat(formattedDate).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}");
    }

    // ========== Real World Scenario Tests ==========

    @Test
    void testBugFixEntry() {
        ChangeEntry bugFix = new ChangeEntry(
                ChangeType.FIXED,
                "Fixed null pointer exception when user clicks save button",
                "BUG-789",
                "234",
                "dev.team"
        );

        assertEquals(ChangeType.FIXED, bugFix.getType());
        assertThat(bugFix.getDescription()).contains("null pointer");
        assertEquals("BUG-789", bugFix.getIssueId());
        assertEquals("234", bugFix.getPrNumber());
        assertEquals("dev.team", bugFix.getAuthor());
    }

    @Test
    void testFeatureEntry() {
        ChangeEntry feature = new ChangeEntry(
                ChangeType.ADDED,
                "Added dark mode support for the application",
                "FEAT-100",
                "567",
                "ui.team"
        );

        assertEquals(ChangeType.ADDED, feature.getType());
        assertThat(feature.getDescription()).contains("dark mode");
    }

    @Test
    void testSecurityEntry() {
        ChangeEntry security = new ChangeEntry(
                ChangeType.SECURITY,
                "Fixed XSS vulnerability in user input handling",
                "SEC-001",
                "999",
                "security.team"
        );

        assertEquals(ChangeType.SECURITY, security.getType());
        assertThat(security.getDescription()).contains("XSS");
    }

    @Test
    void testDeprecationEntry() {
        ChangeEntry deprecation = new ChangeEntry(
                ChangeType.DEPRECATED,
                "Deprecated legacy API v1 endpoints, use v2 instead",
                "API-200",
                "333",
                "api.team"
        );

        assertEquals(ChangeType.DEPRECATED, deprecation.getType());
        assertThat(deprecation.getDescription()).contains("legacy API");
    }

    @Test
    void testChangedEntry() {
        ChangeEntry changed = new ChangeEntry(
                ChangeType.CHANGED,
                "Updated logging format to JSON",
                "LOG-50",
                "444",
                "ops.team"
        );

        assertEquals(ChangeType.CHANGED, changed.getType());
        assertThat(changed.getDescription()).contains("logging");
    }

    @Test
    void testRemovedEntry() {
        ChangeEntry removed = new ChangeEntry(
                ChangeType.REMOVED,
                "Removed deprecated authentication method",
                "AUTH-99",
                "555",
                "security.team"
        );

        assertEquals(ChangeType.REMOVED, removed.getType());
        assertThat(removed.getDescription()).contains("deprecated");
    }
}
