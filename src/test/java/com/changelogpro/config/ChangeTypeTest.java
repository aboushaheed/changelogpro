package com.changelogpro.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ChangeType enum.
 */
class ChangeTypeTest {

    // ========== Display Name Tests ==========

    @Test
    void testAddedDisplayName() {
        assertEquals("Added", ChangeType.ADDED.getDisplayName());
    }

    @Test
    void testChangedDisplayName() {
        assertEquals("Changed", ChangeType.CHANGED.getDisplayName());
    }

    @Test
    void testDeprecatedDisplayName() {
        assertEquals("Deprecated", ChangeType.DEPRECATED.getDisplayName());
    }

    @Test
    void testRemovedDisplayName() {
        assertEquals("Removed", ChangeType.REMOVED.getDisplayName());
    }

    @Test
    void testFixedDisplayName() {
        assertEquals("Fixed", ChangeType.FIXED.getDisplayName());
    }

    @Test
    void testSecurityDisplayName() {
        assertEquals("Security", ChangeType.SECURITY.getDisplayName());
    }

    // ========== Directory Name Tests ==========

    @Test
    void testAddedDirectoryName() {
        assertEquals("added", ChangeType.ADDED.getDirectoryName());
    }

    @Test
    void testChangedDirectoryName() {
        assertEquals("changed", ChangeType.CHANGED.getDirectoryName());
    }

    @Test
    void testDeprecatedDirectoryName() {
        assertEquals("deprecated", ChangeType.DEPRECATED.getDirectoryName());
    }

    @Test
    void testRemovedDirectoryName() {
        assertEquals("removed", ChangeType.REMOVED.getDirectoryName());
    }

    @Test
    void testFixedDirectoryName() {
        assertEquals("fixed", ChangeType.FIXED.getDirectoryName());
    }

    @Test
    void testSecurityDirectoryName() {
        assertEquals("security", ChangeType.SECURITY.getDirectoryName());
    }

    // ========== Description Tests ==========

    @Test
    void testAddedDescription() {
        assertThat(ChangeType.ADDED.getDescription()).contains("New feature");
    }

    @Test
    void testChangedDescription() {
        assertThat(ChangeType.CHANGED.getDescription()).contains("existing functionality");
    }

    @Test
    void testDeprecatedDescription() {
        assertThat(ChangeType.DEPRECATED.getDescription()).contains("removed");
    }

    @Test
    void testRemovedDescription() {
        assertThat(ChangeType.REMOVED.getDescription()).contains("removed");
    }

    @Test
    void testFixedDescription() {
        assertThat(ChangeType.FIXED.getDescription()).contains("Bug fix");
    }

    @Test
    void testSecurityDescription() {
        assertThat(ChangeType.SECURITY.getDescription()).contains("Vulnerability");
    }

    // ========== Color Tests ==========

    @Test
    void testAddedColor() {
        assertThat(ChangeType.ADDED.getColor()).startsWith("#");
    }

    @Test
    void testFixedColor() {
        assertThat(ChangeType.FIXED.getColor()).startsWith("#");
    }

    @Test
    void testSecurityColor() {
        assertThat(ChangeType.SECURITY.getColor()).startsWith("#");
    }

    // ========== Markdown Header Tests ==========

    @Test
    void testAddedMarkdownHeader() {
        assertEquals("### Added", ChangeType.ADDED.getMarkdownHeader());
    }

    @Test
    void testFixedMarkdownHeader() {
        assertEquals("### Fixed", ChangeType.FIXED.getMarkdownHeader());
    }

    @Test
    void testSecurityMarkdownHeader() {
        assertEquals("### Security", ChangeType.SECURITY.getMarkdownHeader());
    }

    // ========== All Types Exist Tests ==========

    @Test
    void testAllTypesHaveDisplayName() {
        for (ChangeType type : ChangeType.values()) {
            assertThat(type.getDisplayName()).isNotNull().isNotEmpty();
        }
    }

    @Test
    void testAllTypesHaveDirectoryName() {
        for (ChangeType type : ChangeType.values()) {
            assertThat(type.getDirectoryName()).isNotNull().isNotEmpty();
        }
    }

    @Test
    void testAllTypesHaveDescription() {
        for (ChangeType type : ChangeType.values()) {
            assertThat(type.getDescription()).isNotNull().isNotEmpty();
        }
    }

    @Test
    void testAllTypesHaveColor() {
        for (ChangeType type : ChangeType.values()) {
            assertThat(type.getColor()).isNotNull().startsWith("#");
        }
    }

    @Test
    void testAllTypesHaveIcon() {
        for (ChangeType type : ChangeType.values()) {
            assertThat(type.getIcon()).isNotNull();
        }
    }

    @Test
    void testAllTypesHaveMarkdownHeader() {
        for (ChangeType type : ChangeType.values()) {
            assertThat(type.getMarkdownHeader()).isNotNull().startsWith("### ");
        }
    }

    // ========== Keep a Changelog Compliance Tests ==========

    @Test
    void testAllKeepAChangelogTypesExist() {
        // Keep a Changelog specifies these 6 types
        assertThat(ChangeType.values()).hasSize(6);
        assertNotNull(ChangeType.ADDED);
        assertNotNull(ChangeType.CHANGED);
        assertNotNull(ChangeType.DEPRECATED);
        assertNotNull(ChangeType.REMOVED);
        assertNotNull(ChangeType.FIXED);
        assertNotNull(ChangeType.SECURITY);
    }

    // ========== ToString Tests ==========

    @Test
    void testToString() {
        assertEquals("Added", ChangeType.ADDED.toString());
        assertEquals("Fixed", ChangeType.FIXED.toString());
        assertEquals("Security", ChangeType.SECURITY.toString());
    }

    // ========== Directory Name Lowercase Tests ==========

    @Test
    void testDirectoryNamesAreLowercase() {
        for (ChangeType type : ChangeType.values()) {
            String dirName = type.getDirectoryName();
            assertEquals(dirName.toLowerCase(), dirName);
        }
    }

    // ========== Enum Value Tests ==========

    @Test
    void testValueOf() {
        assertEquals(ChangeType.ADDED, ChangeType.valueOf("ADDED"));
        assertEquals(ChangeType.FIXED, ChangeType.valueOf("FIXED"));
        assertEquals(ChangeType.SECURITY, ChangeType.valueOf("SECURITY"));
    }

    @Test
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> ChangeType.valueOf("INVALID"));
    }
}
