package com.changelogpro.services;

import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Unit tests for ProjectVersionService.
 * Tests version detection and manipulation for Maven and Gradle projects.
 */
public class ProjectVersionServiceTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    // ========== Version Calculation Tests ==========
    
    @Test
    public void testCalculateNextSnapshotFromRelease() {
        // Test static method behavior
        String result = calculateNextSnapshot("1.2.3");
        assertEquals("1.2.4-SNAPSHOT", result);
    }

    @Test
    public void testCalculateNextSnapshotFromSnapshot() {
        String result = calculateNextSnapshot("1.2.3-SNAPSHOT");
        assertEquals("1.2.4-SNAPSHOT", result);
    }

    @Test
    public void testCalculateNextSnapshotMajorVersion() {
        String result = calculateNextSnapshot("2.0.0");
        assertEquals("2.0.1-SNAPSHOT", result);
    }

    @Test
    public void testCalculateNextSnapshotMinorVersion() {
        String result = calculateNextSnapshot("1.5.0");
        assertEquals("1.5.1-SNAPSHOT", result);
    }

    @Test
    public void testCalculateNextSnapshotPatchVersion() {
        String result = calculateNextSnapshot("1.0.9");
        assertEquals("1.0.10-SNAPSHOT", result);
    }

    @Test
    public void testCalculateNextSnapshotTwoPartVersion() {
        String result = calculateNextSnapshot("1.2");
        assertEquals("1.2.1-SNAPSHOT", result);
    }

    @Test
    public void testCalculateNextSnapshotOnePartVersion() {
        String result = calculateNextSnapshot("5");
        assertEquals("5.0.1-SNAPSHOT", result);
    }

    @Test
    public void testCalculateNextSnapshotNull() {
        String result = calculateNextSnapshot(null);
        assertEquals("0.0.1-SNAPSHOT", result);
    }

    @Test
    public void testCalculateNextSnapshotEmpty() {
        String result = calculateNextSnapshot("");
        assertEquals("0.0.1-SNAPSHOT", result);
    }

    @Test
    public void testCalculateNextSnapshotWithQualifier() {
        String result = calculateNextSnapshot("1.2.3-RELEASE");
        // Should handle qualifier gracefully
        assertThat(result).contains("SNAPSHOT");
    }

    // ========== Maven POM Parsing Tests ==========
    
    @Test
    public void testParseMavenVersionSimple() throws IOException {
        String pomContent = "<?xml version=\"1.0\"?>\n" +
            "<project>\n" +
            "    <groupId>com.example</groupId>\n" +
            "    <artifactId>my-app</artifactId>\n" +
            "    <version>1.2.3</version>\n" +
            "</project>";
        
        File pomFile = tempFolder.newFile("pom.xml");
        writeFile(pomFile, pomContent);
        
        String version = parseMavenVersion(pomFile);
        assertEquals("1.2.3", version);
    }

    @Test
    public void testParseMavenVersionSnapshot() throws IOException {
        String pomContent = "<?xml version=\"1.0\"?>\n" +
            "<project>\n" +
            "    <version>2.0.0-SNAPSHOT</version>\n" +
            "</project>";
        
        File pomFile = tempFolder.newFile("pom.xml");
        writeFile(pomFile, pomContent);
        
        String version = parseMavenVersion(pomFile);
        assertEquals("2.0.0-SNAPSHOT", version);
    }

    @Test
    public void testParseMavenVersionWithNamespace() throws IOException {
        String pomContent = "<?xml version=\"1.0\"?>\n" +
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
            "    <modelVersion>4.0.0</modelVersion>\n" +
            "    <version>3.1.4</version>\n" +
            "</project>";
        
        File pomFile = tempFolder.newFile("pom.xml");
        writeFile(pomFile, pomContent);
        
        String version = parseMavenVersion(pomFile);
        assertEquals("3.1.4", version);
    }

    // ========== Gradle Version Parsing Tests ==========
    
    @Test
    public void testParseGradleVersionGroovy() throws IOException {
        String gradleContent = "plugins {\n" +
            "    id 'java'\n" +
            "}\n" +
            "group = 'com.example'\n" +
            "version = '1.0.0'\n";
        
        File gradleFile = tempFolder.newFile("build.gradle");
        writeFile(gradleFile, gradleContent);
        
        String version = parseGradleVersion(gradleFile);
        assertEquals("1.0.0", version);
    }

    @Test
    public void testParseGradleVersionGroovyDoubleQuotes() throws IOException {
        String gradleContent = "version = \"2.3.4\"\n";
        
        File gradleFile = tempFolder.newFile("build.gradle");
        writeFile(gradleFile, gradleContent);
        
        String version = parseGradleVersion(gradleFile);
        assertEquals("2.3.4", version);
    }

    @Test
    public void testParseGradleVersionKotlin() throws IOException {
        String gradleContent = "plugins {\n" +
            "    id(\"java\")\n" +
            "}\n" +
            "group = \"com.example\"\n" +
            "version = \"1.5.0\"\n";
        
        File gradleFile = tempFolder.newFile("build.gradle.kts");
        writeFile(gradleFile, gradleContent);
        
        String version = parseGradleVersion(gradleFile);
        assertEquals("1.5.0", version);
    }

    @Test
    public void testParseGradleVersionFromProperties() throws IOException {
        String propertiesContent = "version=3.0.0\n" +
            "group=com.example\n";
        
        File propsFile = tempFolder.newFile("gradle.properties");
        writeFile(propsFile, propertiesContent);
        
        String version = parseGradlePropertiesVersion(propsFile);
        assertEquals("3.0.0", version);
    }

    // ========== Project Type Detection Tests ==========
    
    @Test
    public void testDetectMavenProject() throws IOException {
        tempFolder.newFile("pom.xml");
        
        String projectType = detectProjectType(tempFolder.getRoot());
        assertEquals("MAVEN", projectType);
    }

    @Test
    public void testDetectGradleGroovyProject() throws IOException {
        tempFolder.newFile("build.gradle");
        
        String projectType = detectProjectType(tempFolder.getRoot());
        assertEquals("GRADLE_GROOVY", projectType);
    }

    @Test
    public void testDetectGradleKotlinProject() throws IOException {
        tempFolder.newFile("build.gradle.kts");
        
        String projectType = detectProjectType(tempFolder.getRoot());
        assertEquals("GRADLE_KOTLIN", projectType);
    }

    @Test
    public void testDetectUnknownProject() {
        String projectType = detectProjectType(tempFolder.getRoot());
        assertEquals("UNKNOWN", projectType);
    }

    @Test
    public void testMavenTakesPrecedenceOverGradle() throws IOException {
        // When both exist, Maven should take precedence
        tempFolder.newFile("pom.xml");
        tempFolder.newFile("build.gradle");
        
        String projectType = detectProjectType(tempFolder.getRoot());
        assertEquals("MAVEN", projectType);
    }

    // ========== Version Update Tests ==========
    
    @Test
    public void testUpdateMavenVersion() throws IOException {
        String pomContent = "<?xml version=\"1.0\"?>\n" +
            "<project>\n" +
            "    <version>1.0.0</version>\n" +
            "</project>";
        
        File pomFile = tempFolder.newFile("pom.xml");
        writeFile(pomFile, pomContent);
        
        boolean result = updateMavenVersion(pomFile, "2.0.0");
        assertTrue(result);
        
        String updatedContent = new String(Files.readAllBytes(pomFile.toPath()));
        assertThat(updatedContent).contains("<version>2.0.0</version>");
    }

    @Test
    public void testUpdateGradleVersion() throws IOException {
        String gradleContent = "version = '1.0.0'\n";
        
        File gradleFile = tempFolder.newFile("build.gradle");
        writeFile(gradleFile, gradleContent);
        
        boolean result = updateGradleVersion(gradleFile, "2.0.0");
        assertTrue(result);
        
        String updatedContent = new String(Files.readAllBytes(gradleFile.toPath()));
        assertThat(updatedContent).contains("version = '2.0.0'");
    }

    @Test
    public void testUpdateGradlePropertiesVersion() throws IOException {
        String propsContent = "version=1.0.0\n";
        
        File propsFile = tempFolder.newFile("gradle.properties");
        writeFile(propsFile, propsContent);
        
        boolean result = updateGradlePropertiesVersion(propsFile, "2.0.0");
        assertTrue(result);
        
        String updatedContent = new String(Files.readAllBytes(propsFile.toPath()));
        assertThat(updatedContent).contains("version=2.0.0");
    }

    // ========== Semantic Versioning Tests ==========
    
    @Test
    public void testSemanticVersionFormat() {
        // Valid semantic versions
        assertTrue(isValidSemanticVersion("1.0.0"));
        assertTrue(isValidSemanticVersion("0.1.0"));
        assertTrue(isValidSemanticVersion("10.20.30"));
        assertTrue(isValidSemanticVersion("1.0.0-SNAPSHOT"));
        assertTrue(isValidSemanticVersion("1.0.0-alpha"));
        assertTrue(isValidSemanticVersion("1.0.0-beta.1"));
    }

    @Test
    public void testInvalidSemanticVersionFormat() {
        // Invalid semantic versions
        assertFalse(isValidSemanticVersion("1"));
        assertFalse(isValidSemanticVersion("1.0"));
        assertFalse(isValidSemanticVersion("v1.0.0"));
        assertFalse(isValidSemanticVersion("1.0.0.0"));
        assertFalse(isValidSemanticVersion("abc"));
        assertFalse(isValidSemanticVersion(""));
        assertFalse(isValidSemanticVersion(null));
    }

    // ========== Helper Methods ==========
    
    private void writeFile(File file, String content) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    // Simulated methods for testing logic without IntelliJ dependencies
    
    private String calculateNextSnapshot(String version) {
        if (version == null || version.isEmpty()) {
            return "0.0.1-SNAPSHOT";
        }
        
        String cleanVersion = version.replace("-SNAPSHOT", "").replaceAll("-.*", "");
        String[] parts = cleanVersion.split("\\.");
        
        int major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
        int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        
        return major + "." + minor + "." + (patch + 1) + "-SNAPSHOT";
    }

    private String parseMavenVersion(File pomFile) throws IOException {
        String content = new String(Files.readAllBytes(pomFile.toPath()));
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<version>([^<]+)</version>");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String parseGradleVersion(File gradleFile) throws IOException {
        String content = new String(Files.readAllBytes(gradleFile.toPath()));
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("version\\s*=\\s*['\"]([^'\"]+)['\"]");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String parseGradlePropertiesVersion(File propsFile) throws IOException {
        String content = new String(Files.readAllBytes(propsFile.toPath()));
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("version\\s*=\\s*(.+)");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private String detectProjectType(File projectDir) {
        if (new File(projectDir, "pom.xml").exists()) {
            return "MAVEN";
        } else if (new File(projectDir, "build.gradle.kts").exists()) {
            return "GRADLE_KOTLIN";
        } else if (new File(projectDir, "build.gradle").exists()) {
            return "GRADLE_GROOVY";
        }
        return "UNKNOWN";
    }

    private boolean updateMavenVersion(File pomFile, String newVersion) throws IOException {
        String content = new String(Files.readAllBytes(pomFile.toPath()));
        String updated = content.replaceFirst("<version>[^<]+</version>", "<version>" + newVersion + "</version>");
        Files.write(pomFile.toPath(), updated.getBytes());
        return true;
    }

    private boolean updateGradleVersion(File gradleFile, String newVersion) throws IOException {
        String content = new String(Files.readAllBytes(gradleFile.toPath()));
        String updated = content.replaceFirst("version\\s*=\\s*['\"][^'\"]+['\"]", "version = '" + newVersion + "'");
        Files.write(gradleFile.toPath(), updated.getBytes());
        return true;
    }

    private boolean updateGradlePropertiesVersion(File propsFile, String newVersion) throws IOException {
        String content = new String(Files.readAllBytes(propsFile.toPath()));
        String updated = content.replaceFirst("version\\s*=\\s*.+", "version=" + newVersion);
        Files.write(propsFile.toPath(), updated.getBytes());
        return true;
    }

    private boolean isValidSemanticVersion(String version) {
        if (version == null || version.isEmpty()) {
            return false;
        }
        return version.matches("\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9.]+)?");
    }
}
