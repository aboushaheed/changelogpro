package com.changelogpro.config;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ChangeType enum.
 */
public class ChangeTypeTest {

    // ========== Display Name Tests ==========
    
    @Test
    public void testAddedDisplayName() {
        assertEquals("Added", ChangeType.ADDED.getDisplayName());
    }

    @Test
    public void testChangedDisplayName() {
        assertEquals("Changed", ChangeType.CHANGED.getDisplayName());
    }

    @Test
    public void testDeprecatedDisplayName() {
        assertEquals("Deprecated", ChangeType.DEPRECATED.getDisplayName());
    }

    @Test
    public void testRemovedDisplayName() {
        assertEquals("Removed", ChangeType.REMOVED.getDisplayName());
    }

    @Test
    public void testFixedDisplayName() {
        assertEquals("Fixed", ChangeType.FIXED.getDisplayName());
    }

    @Test
    public void testSecurityDisplayName() {
        assertEquals("Security", ChangeType.SECURITY.getDisplayName());
    }

    // ========== Directory Name Tests ==========
    
    @Test
    public void testAddedDirectoryName() {
        assertEquals("added", ChangeType.ADDED.getDirectoryName());
    }

    @Test
    public void testChangedDirectoryName() {
        assertEquals("changed", ChangeType.CHANGED.getDirectoryName());
    }

    @Test
    public void testDeprecatedDirectoryName() {
        assertEquals("deprecated", ChangeType.DEPRECATED.getDirectoryName());
    }

    @Test
    public void testRemovedDirectoryName() {
        assertEquals("removed", ChangeType.REMOVED.getDirectoryName());
    }

    @Test
    public void testFixedDirectoryName() {
        assertEquals("fixed", ChangeType.FIXED.getDirectoryName());
    }

    @Test
    public void testSecurityDirectoryName() {
        assertEquals("security", ChangeType.SECURITY.getDirectoryName());
    }

    // ========== Description Tests ==========
    
    @Test
    public void testAddedDescription() {
        assertThat(ChangeType.ADDED.getDescription()).contains("New feature");
    }

    @Test
    public void testChangedDescription() {
        assertThat(ChangeType.CHANGED.getDescription()).contains("existing functionality");
    }

    @Test
    public void testDeprecatedDescription() {
        assertThat(ChangeType.DEPRECATED.getDescription()).contains("removed");
    }

    @Test
    public void testRemovedDescription() {
        assertThat(ChangeType.REMOVED.getDescription()).contains("removed");
    }

    @Test
    public void testFixedDescription() {
        assertThat(ChangeType.FIXED.getDescription()).contains("Bug fix");
    }

    @Test
    public void testSecurityDescription() {
        assertThat(ChangeType.SECURITY.getDescription()).contains("Vulnerability");
    }

    // ========== Color Tests ==========
    
    @Test
    public void testAddedColor() {
        assertThat(ChangeType.ADDED.getColor()).startsWith("#");
    }

    @Test
    public void testFixedColor() {
        assertThat(ChangeType.FIXED.getColor()).startsWith("#");
    }

    @Test
    public void testSecurityColor() {
        assertThat(ChangeType.SECURITY.getColor()).startsWith("#");
    }

    // ========== Markdown Header Tests ==========
    
    @Test
    public void testAddedMarkdownHeader() {
        assertEquals("### Added", ChangeType.ADDED.getMarkdownHeader());
    }

    @Test
    public void testFixedMarkdownHeader() {
        assertEquals("### Fixed", ChangeType.FIXED.getMarkdownHeader());
    }

    @Test
    public void testSecurityMarkdownHeader() {
        assertEquals("### Security", ChangeType.SECURITY.getMarkdownHeader());
    }

    // ========== All Types Exist Tests ==========
    
    @Test
    public void testAllTypesHaveDisplayName() {
        for (ChangeType type : ChangeType.values()) {
            assertThat(type.getDisplayName()).isNotNull().isNotEmpty();
        }
    }

    @Test
    public void testAllTypesHaveDirectoryName() {
        for (ChangeType type : ChangeType.values()) {
            assertThat(type.getDirectoryName()).isNotNull().isNotEmpty();
        }
    }

    @Test
    public void testAllTypesHaveDescription() {
        for (ChangeType type : ChangeType.values()) {
            assertThat(type.getDescription()).isNotNull().isNotEmpty();
        }
    }

    @Test
    public void testAllTypesHaveColor() {
        for (ChangeType type : ChangeType.values()) {
            assertThat(type.getColor()).isNotNull().startsWith("#");
        }
    }

    @Test
    public void testAllTypesHaveIcon() {
        for (ChangeType type : ChangeType.values()) {
            assertThat(type.getIcon()).isNotNull();
        }
    }

    @Test
    public void testAllTypesHaveMarkdownHeader() {
        for (ChangeType type : ChangeType.values()) {
            assertThat(type.getMarkdownHeader()).isNotNull().startsWith("### ");
        }
    }

    // ========== Keep a Changelog Compliance Tests ==========
    
    @Test
    public void testAllKeepAChangelogTypesExist() {
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
    public void testToString() {
        assertEquals("Added", ChangeType.ADDED.toString());
        assertEquals("Fixed", ChangeType.FIXED.toString());
        assertEquals("Security", ChangeType.SECURITY.toString());
    }

    // ========== Directory Name Lowercase Tests ==========
    
    @Test
    public void testDirectoryNamesAreLowercase() {
        for (ChangeType type : ChangeType.values()) {
            String dirName = type.getDirectoryName();
            assertEquals(dirName.toLowerCase(), dirName);
        }
    }

    // ========== Enum Value Tests ==========
    
    @Test
    public void testValueOf() {
        assertEquals(ChangeType.ADDED, ChangeType.valueOf("ADDED"));
        assertEquals(ChangeType.FIXED, ChangeType.valueOf("FIXED"));
        assertEquals(ChangeType.SECURITY, ChangeType.valueOf("SECURITY"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfInvalid() {
        ChangeType.valueOf("INVALID");
    }
}
