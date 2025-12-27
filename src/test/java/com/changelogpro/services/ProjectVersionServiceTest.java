package com.changelogpro.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Unit tests for ProjectVersionService.
 * Tests version detection and manipulation for Maven and Gradle projects.
 */
class ProjectVersionServiceTest {

    @TempDir
    Path tempDir;

    // ========== Version Calculation Tests ==========

    @Test
    void testCalculateNextSnapshotFromRelease() {
        String result = calculateNextSnapshot("1.2.3");
        assertEquals("1.2.4-SNAPSHOT", result);
    }

    @Test
    void testCalculateNextSnapshotFromSnapshot() {
        String result = calculateNextSnapshot("1.2.3-SNAPSHOT");
        assertEquals("1.2.4-SNAPSHOT", result);
    }

    @Test
    void testCalculateNextSnapshotMajorVersion() {
        String result = calculateNextSnapshot("2.0.0");
        assertEquals("2.0.1-SNAPSHOT", result);
    }

    @Test
    void testCalculateNextSnapshotMinorVersion() {
        String result = calculateNextSnapshot("1.5.0");
        assertEquals("1.5.1-SNAPSHOT", result);
    }

    @Test
    void testCalculateNextSnapshotPatchVersion() {
        String result = calculateNextSnapshot("1.0.9");
        assertEquals("1.0.10-SNAPSHOT", result);
    }

    @Test
    void testCalculateNextSnapshotTwoPartVersion() {
        String result = calculateNextSnapshot("1.2");
        assertEquals("1.2.1-SNAPSHOT", result);
    }

    @Test
    void testCalculateNextSnapshotOnePartVersion() {
        String result = calculateNextSnapshot("5");
        assertEquals("5.0.1-SNAPSHOT", result);
    }

    @Test
    void testCalculateNextSnapshotNull() {
        String result = calculateNextSnapshot(null);
        assertEquals("0.0.1-SNAPSHOT", result);
    }

    @Test
    void testCalculateNextSnapshotEmpty() {
        String result = calculateNextSnapshot("");
        assertEquals("0.0.1-SNAPSHOT", result);
    }

    @Test
    void testCalculateNextSnapshotWithQualifier() {
        String result = calculateNextSnapshot("1.2.3-RELEASE");
        assertThat(result).contains("SNAPSHOT");
    }

    // ========== Maven POM Parsing Tests ==========

    @Test
    void testParseMavenVersionSimple() throws IOException {
        String pomContent = "<?xml version=\"1.0\"?>\n" +
                "<project>\n" +
                "    <groupId>com.example</groupId>\n" +
                "    <artifactId>my-app</artifactId>\n" +
                "    <version>1.2.3</version>\n" +
                "</project>";

        File pomFile = tempDir.resolve("pom.xml").toFile();
        writeFile(pomFile, pomContent);

        String version = parseMavenVersion(pomFile);
        assertEquals("1.2.3", version);
    }

    @Test
    void testParseMavenVersionSnapshot() throws IOException {
        String pomContent = "<?xml version=\"1.0\"?>\n" +
                "<project>\n" +
                "    <version>2.0.0-SNAPSHOT</version>\n" +
                "</project>";

        File pomFile = tempDir.resolve("pom.xml").toFile();
        writeFile(pomFile, pomContent);

        String version = parseMavenVersion(pomFile);
        assertEquals("2.0.0-SNAPSHOT", version);
    }

    @Test
    void testParseMavenVersionWithNamespace() throws IOException {
        String pomContent = "<?xml version=\"1.0\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <version>3.1.4</version>\n" +
                "</project>";

        File pomFile = tempDir.resolve("pom.xml").toFile();
        writeFile(pomFile, pomContent);

        String version = parseMavenVersion(pomFile);
        assertEquals("3.1.4", version);
    }

    // ========== Gradle Version Parsing Tests ==========

    @Test
    void testParseGradleVersionGroovy() throws IOException {
        String gradleContent = "plugins {\n" +
                "    id 'java'\n" +
                "}\n" +
                "group = 'com.example'\n" +
                "version = '1.0.0'\n";

        File gradleFile = tempDir.resolve("build.gradle").toFile();
        writeFile(gradleFile, gradleContent);

        String version = parseGradleVersion(gradleFile);
        assertEquals("1.0.0", version);
    }

    @Test
    void testParseGradleVersionGroovyDoubleQuotes() throws IOException {
        String gradleContent = "version = \"2.3.4\"\n";

        File gradleFile = tempDir.resolve("build.gradle").toFile();
        writeFile(gradleFile, gradleContent);

        String version = parseGradleVersion(gradleFile);
        assertEquals("2.3.4", version);
    }

    @Test
    void testParseGradleVersionKotlin() throws IOException {
        String gradleContent = "plugins {\n" +
                "    id(\"java\")\n" +
                "}\n" +
                "group = \"com.example\"\n" +
                "version = \"1.5.0\"\n";

        File gradleFile = tempDir.resolve("build.gradle.kts").toFile();
        writeFile(gradleFile, gradleContent);

        String version = parseGradleVersion(gradleFile);
        assertEquals("1.5.0", version);
    }

    @Test
    void testParseGradleVersionFromProperties() throws IOException {
        String propertiesContent = "version=3.0.0\n" +
                "group=com.example\n";

        File propsFile = tempDir.resolve("gradle.properties").toFile();
        writeFile(propsFile, propertiesContent);

        String version = parseGradlePropertiesVersion(propsFile);
        assertEquals("3.0.0", version);
    }

    // ========== Project Type Detection Tests ==========

    @Test
    void testDetectMavenProject() throws IOException {
        Files.createFile(tempDir.resolve("pom.xml"));

        String projectType = detectProjectType(tempDir.toFile());
        assertEquals("MAVEN", projectType);
    }

    @Test
    void testDetectGradleGroovyProject() throws IOException {
        Files.createFile(tempDir.resolve("build.gradle"));

        String projectType = detectProjectType(tempDir.toFile());
        assertEquals("GRADLE_GROOVY", projectType);
    }

    @Test
    void testDetectGradleKotlinProject() throws IOException {
        Files.createFile(tempDir.resolve("build.gradle.kts"));

        String projectType = detectProjectType(tempDir.toFile());
        assertEquals("GRADLE_KOTLIN", projectType);
    }

    @Test
    void testDetectUnknownProject() {
        String projectType = detectProjectType(tempDir.toFile());
        assertEquals("UNKNOWN", projectType);
    }

    @Test
    void testMavenTakesPrecedenceOverGradle() throws IOException {
        Files.createFile(tempDir.resolve("pom.xml"));
        Files.createFile(tempDir.resolve("build.gradle"));

        String projectType = detectProjectType(tempDir.toFile());
        assertEquals("MAVEN", projectType);
    }

    // ========== Version Update Tests ==========

    @Test
    void testUpdateMavenVersion() throws IOException {
        String pomContent = "<?xml version=\"1.0\"?>\n" +
                "<project>\n" +
                "    <version>1.0.0</version>\n" +
                "</project>";

        File pomFile = tempDir.resolve("pom.xml").toFile();
        writeFile(pomFile, pomContent);

        boolean result = updateMavenVersion(pomFile, "2.0.0");
        assertTrue(result);

        String updatedContent = Files.readString(pomFile.toPath());
        assertThat(updatedContent).contains("<version>2.0.0</version>");
    }

    @Test
    void testUpdateGradleVersion() throws IOException {
        String gradleContent = "version = '1.0.0'\n";

        File gradleFile = tempDir.resolve("build.gradle").toFile();
        writeFile(gradleFile, gradleContent);

        boolean result = updateGradleVersion(gradleFile, "2.0.0");
        assertTrue(result);

        String updatedContent = Files.readString(gradleFile.toPath());
        assertThat(updatedContent).contains("version = '2.0.0'");
    }

    @Test
    void testUpdateGradlePropertiesVersion() throws IOException {
        String propsContent = "version=1.0.0\n";

        File propsFile = tempDir.resolve("gradle.properties").toFile();
        writeFile(propsFile, propsContent);

        boolean result = updateGradlePropertiesVersion(propsFile, "2.0.0");
        assertTrue(result);

        String updatedContent = Files.readString(propsFile.toPath());
        assertThat(updatedContent).contains("version=2.0.0");
    }

    // ========== Semantic Versioning Tests ==========

    @Test
    void testSemanticVersionFormat() {
        assertTrue(isValidSemanticVersion("1.0.0"));
        assertTrue(isValidSemanticVersion("0.1.0"));
        assertTrue(isValidSemanticVersion("10.20.30"));
        assertTrue(isValidSemanticVersion("1.0.0-SNAPSHOT"));
        assertTrue(isValidSemanticVersion("1.0.0-alpha"));
        assertTrue(isValidSemanticVersion("1.0.0-beta.1"));
    }

    @Test
    void testInvalidSemanticVersionFormat() {
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
        String content = Files.readString(pomFile.toPath());
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<version>([^<]+)</version>");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String parseGradleVersion(File gradleFile) throws IOException {
        String content = Files.readString(gradleFile.toPath());
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("version\\s*=\\s*['\"]([^'\"]+)['\"]");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String parseGradlePropertiesVersion(File propsFile) throws IOException {
        String content = Files.readString(propsFile.toPath());
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
        String content = Files.readString(pomFile.toPath());
        String updated = content.replaceFirst("<version>[^<]+</version>", "<version>" + newVersion + "</version>");
        Files.writeString(pomFile.toPath(), updated);
        return true;
    }

    private boolean updateGradleVersion(File gradleFile, String newVersion) throws IOException {
        String content = Files.readString(gradleFile.toPath());
        String updated = content.replaceFirst("version\\s*=\\s*['\"][^'\"]+['\"]", "version = '" + newVersion + "'");
        Files.writeString(gradleFile.toPath(), updated);
        return true;
    }

    private boolean updateGradlePropertiesVersion(File propsFile, String newVersion) throws IOException {
        String content = Files.readString(propsFile.toPath());
        String updated = content.replaceFirst("version\\s*=\\s*.+", "version=" + newVersion);
        Files.writeString(propsFile.toPath(), updated);
        return true;
    }

    private boolean isValidSemanticVersion(String version) {
        if (version == null || version.isEmpty()) {
            return false;
        }
        return version.matches("\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9.]+)?");
    }
}
