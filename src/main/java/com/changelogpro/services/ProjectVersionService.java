package com.changelogpro.services;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for detecting and updating project versions in Maven and Gradle projects.
 */
public class ProjectVersionService {
    
    public enum ProjectType {
        MAVEN("Maven", "pom.xml"),
        GRADLE_GROOVY("Gradle (Groovy)", "build.gradle"),
        GRADLE_KOTLIN("Gradle (Kotlin)", "build.gradle.kts"),
        UNKNOWN("Unknown", null);
        
        private final String displayName;
        private final String buildFile;
        
        ProjectType(String displayName, String buildFile) {
            this.displayName = displayName;
            this.buildFile = buildFile;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getBuildFile() {
            return buildFile;
        }
    }
    
    private final Project project;
    private final LogService logger;
    
    public ProjectVersionService(@NotNull Project project, @NotNull LogService logger) {
        this.project = project;
        this.logger = logger;
    }
    
    /**
     * Detect the project type (Maven or Gradle).
     */
    public ProjectType detectProjectType() {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return ProjectType.UNKNOWN;
        }
        
        // Check for Gradle Kotlin DSL first (more specific)
        if (Files.exists(Paths.get(basePath, "build.gradle.kts"))) {
            return ProjectType.GRADLE_KOTLIN;
        }
        
        // Check for Gradle Groovy
        if (Files.exists(Paths.get(basePath, "build.gradle"))) {
            return ProjectType.GRADLE_GROOVY;
        }
        
        // Check for Maven
        if (Files.exists(Paths.get(basePath, "pom.xml"))) {
            return ProjectType.MAVEN;
        }
        
        return ProjectType.UNKNOWN;
    }
    
    /**
     * Get the current project version.
     */
    @Nullable
    public String getCurrentVersion() {
        ProjectType type = detectProjectType();
        
        switch (type) {
            case MAVEN:
                return getMavenVersion();
            case GRADLE_GROOVY:
                return getGradleGroovyVersion();
            case GRADLE_KOTLIN:
                return getGradleKotlinVersion();
            default:
                return null;
        }
    }
    
    /**
     * Set the project version.
     */
    public boolean setVersion(String newVersion) {
        ProjectType type = detectProjectType();
        
        switch (type) {
            case MAVEN:
                return setMavenVersion(newVersion);
            case GRADLE_GROOVY:
                return setGradleGroovyVersion(newVersion);
            case GRADLE_KOTLIN:
                return setGradleKotlinVersion(newVersion);
            default:
                logger.warn("Unknown project type, cannot set version");
                return false;
        }
    }
    
    /**
     * Calculate the next snapshot version from a release version.
     * Example: 1.2.3 -> 1.2.4-SNAPSHOT
     */
    public String calculateNextSnapshot(String releaseVersion) {
        if (releaseVersion == null || releaseVersion.isEmpty()) {
            return "0.0.1-SNAPSHOT";
        }
        
        // Remove -SNAPSHOT if present
        String version = releaseVersion.replace("-SNAPSHOT", "");
        
        // Parse version parts
        String[] parts = version.split("\\.");
        if (parts.length < 3) {
            // Pad with zeros
            String[] newParts = new String[3];
            for (int i = 0; i < 3; i++) {
                newParts[i] = i < parts.length ? parts[i] : "0";
            }
            parts = newParts;
        }
        
        // Increment patch version
        try {
            int patch = Integer.parseInt(parts[2]);
            parts[2] = String.valueOf(patch + 1);
        } catch (NumberFormatException e) {
            parts[2] = "1";
        }
        
        return String.join(".", parts) + "-SNAPSHOT";
    }
    
    /**
     * Remove -SNAPSHOT suffix from version.
     */
    public String removeSnapshot(String version) {
        if (version == null) {
            return null;
        }
        return version.replace("-SNAPSHOT", "");
    }
    
    /**
     * Check if version is a snapshot.
     */
    public boolean isSnapshot(String version) {
        return version != null && version.endsWith("-SNAPSHOT");
    }
    
    // ==================== Maven ====================
    
    @Nullable
    private String getMavenVersion() {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return null;
        }
        
        Path pomPath = Paths.get(basePath, "pom.xml");
        if (!Files.exists(pomPath)) {
            return null;
        }
        
        try {
            String content = new String(Files.readAllBytes(pomPath), StandardCharsets.UTF_8);
            
            // Try to find version in project section (not in parent or dependencies)
            // Look for <version> that's a direct child of <project>
            Pattern pattern = Pattern.compile(
                "<project[^>]*>.*?(?:<parent>.*?</parent>)?.*?<version>([^<]+)</version>",
                Pattern.DOTALL
            );
            Matcher matcher = pattern.matcher(content);
            
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
            
            // Fallback: simple version extraction
            Pattern simplePattern = Pattern.compile("<version>([^<]+)</version>");
            Matcher simpleMatcher = simplePattern.matcher(content);
            
            // Skip parent version if exists
            int parentEnd = content.indexOf("</parent>");
            if (parentEnd > 0 && simpleMatcher.find(parentEnd)) {
                return simpleMatcher.group(1).trim();
            } else if (simpleMatcher.find()) {
                return simpleMatcher.group(1).trim();
            }
            
        } catch (IOException e) {
            logger.error("Failed to read pom.xml: " + e.getMessage());
        }
        
        return null;
    }
    
    private boolean setMavenVersion(String newVersion) {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return false;
        }
        
        Path pomPath = Paths.get(basePath, "pom.xml");
        if (!Files.exists(pomPath)) {
            logger.error("pom.xml not found");
            return false;
        }
        
        try {
            String content = new String(Files.readAllBytes(pomPath), StandardCharsets.UTF_8);
            
            // Find and replace the project version (not parent version)
            // This is a simplified approach - for complex POMs, mvn versions:set is better
            
            // Strategy: Find the first <version> after </parent> or after <project> if no parent
            int startIndex = 0;
            int parentEnd = content.indexOf("</parent>");
            if (parentEnd > 0) {
                startIndex = parentEnd;
            }
            
            Pattern pattern = Pattern.compile("<version>([^<]+)</version>");
            Matcher matcher = pattern.matcher(content);
            
            if (matcher.find(startIndex)) {
                String oldVersion = matcher.group(1);
                String newContent = content.substring(0, matcher.start()) +
                    "<version>" + newVersion + "</version>" +
                    content.substring(matcher.end());
                
                Files.write(pomPath, newContent.getBytes(StandardCharsets.UTF_8));
                logger.success("Maven version updated: " + oldVersion + " -> " + newVersion);
                return true;
            }
            
            logger.error("Could not find version in pom.xml");
            return false;
            
        } catch (IOException e) {
            logger.error("Failed to update pom.xml: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== Gradle Groovy ====================
    
    @Nullable
    private String getGradleGroovyVersion() {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return null;
        }
        
        // First check gradle.properties
        String propsVersion = getGradlePropertiesVersion();
        if (propsVersion != null) {
            return propsVersion;
        }
        
        // Then check build.gradle
        Path buildPath = Paths.get(basePath, "build.gradle");
        if (!Files.exists(buildPath)) {
            return null;
        }
        
        try {
            String content = new String(Files.readAllBytes(buildPath), StandardCharsets.UTF_8);
            
            // Look for: version = 'x.x.x' or version = "x.x.x" or version 'x.x.x'
            Pattern pattern = Pattern.compile("version\\s*=?\\s*['\"]([^'\"]+)['\"]");
            Matcher matcher = pattern.matcher(content);
            
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
            
        } catch (IOException e) {
            logger.error("Failed to read build.gradle: " + e.getMessage());
        }
        
        return null;
    }
    
    private boolean setGradleGroovyVersion(String newVersion) {
        // First try gradle.properties
        if (setGradlePropertiesVersion(newVersion)) {
            return true;
        }
        
        String basePath = project.getBasePath();
        if (basePath == null) {
            return false;
        }
        
        Path buildPath = Paths.get(basePath, "build.gradle");
        if (!Files.exists(buildPath)) {
            logger.error("build.gradle not found");
            return false;
        }
        
        try {
            String content = new String(Files.readAllBytes(buildPath), StandardCharsets.UTF_8);
            
            // Replace version = 'x.x.x' or version = "x.x.x"
            Pattern pattern = Pattern.compile("(version\\s*=\\s*)['\"]([^'\"]+)['\"]");
            Matcher matcher = pattern.matcher(content);
            
            if (matcher.find()) {
                String oldVersion = matcher.group(2);
                String quote = content.charAt(matcher.start() + matcher.group(1).length()) == '"' ? "\"" : "'";
                String newContent = matcher.replaceFirst("$1" + quote + newVersion + quote);
                
                Files.write(buildPath, newContent.getBytes(StandardCharsets.UTF_8));
                logger.success("Gradle version updated: " + oldVersion + " -> " + newVersion);
                return true;
            }
            
            logger.error("Could not find version in build.gradle");
            return false;
            
        } catch (IOException e) {
            logger.error("Failed to update build.gradle: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== Gradle Kotlin ====================
    
    @Nullable
    private String getGradleKotlinVersion() {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return null;
        }
        
        // First check gradle.properties
        String propsVersion = getGradlePropertiesVersion();
        if (propsVersion != null) {
            return propsVersion;
        }
        
        // Then check build.gradle.kts
        Path buildPath = Paths.get(basePath, "build.gradle.kts");
        if (!Files.exists(buildPath)) {
            return null;
        }
        
        try {
            String content = new String(Files.readAllBytes(buildPath), StandardCharsets.UTF_8);
            
            // Look for: version = "x.x.x"
            Pattern pattern = Pattern.compile("version\\s*=\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(content);
            
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
            
        } catch (IOException e) {
            logger.error("Failed to read build.gradle.kts: " + e.getMessage());
        }
        
        return null;
    }
    
    private boolean setGradleKotlinVersion(String newVersion) {
        // First try gradle.properties
        if (setGradlePropertiesVersion(newVersion)) {
            return true;
        }
        
        String basePath = project.getBasePath();
        if (basePath == null) {
            return false;
        }
        
        Path buildPath = Paths.get(basePath, "build.gradle.kts");
        if (!Files.exists(buildPath)) {
            logger.error("build.gradle.kts not found");
            return false;
        }
        
        try {
            String content = new String(Files.readAllBytes(buildPath), StandardCharsets.UTF_8);
            
            // Replace version = "x.x.x"
            Pattern pattern = Pattern.compile("(version\\s*=\\s*)\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(content);
            
            if (matcher.find()) {
                String oldVersion = matcher.group(2);
                String newContent = matcher.replaceFirst("$1\"" + newVersion + "\"");
                
                Files.write(buildPath, newContent.getBytes(StandardCharsets.UTF_8));
                logger.success("Gradle Kotlin version updated: " + oldVersion + " -> " + newVersion);
                return true;
            }
            
            logger.error("Could not find version in build.gradle.kts");
            return false;
            
        } catch (IOException e) {
            logger.error("Failed to update build.gradle.kts: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== Gradle Properties ====================
    
    @Nullable
    private String getGradlePropertiesVersion() {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return null;
        }
        
        Path propsPath = Paths.get(basePath, "gradle.properties");
        if (!Files.exists(propsPath)) {
            return null;
        }
        
        try {
            Properties props = new Properties();
            try (InputStream is = Files.newInputStream(propsPath)) {
                props.load(is);
            }
            
            // Check common version property names
            String[] versionKeys = {"version", "projectVersion", "pluginVersion", "appVersion"};
            for (String key : versionKeys) {
                String value = props.getProperty(key);
                if (value != null && !value.isEmpty()) {
                    return value;
                }
            }
            
        } catch (IOException e) {
            // Ignore
        }
        
        return null;
    }
    
    private boolean setGradlePropertiesVersion(String newVersion) {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return false;
        }
        
        Path propsPath = Paths.get(basePath, "gradle.properties");
        if (!Files.exists(propsPath)) {
            return false;
        }
        
        try {
            String content = new String(Files.readAllBytes(propsPath), StandardCharsets.UTF_8);
            
            // Try common version property names
            String[] versionKeys = {"version", "projectVersion", "pluginVersion", "appVersion"};
            
            for (String key : versionKeys) {
                Pattern pattern = Pattern.compile("^(" + key + "\\s*=\\s*)(.+)$", Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(content);
                
                if (matcher.find()) {
                    String oldVersion = matcher.group(2).trim();
                    String newContent = matcher.replaceFirst("$1" + newVersion);
                    
                    Files.write(propsPath, newContent.getBytes(StandardCharsets.UTF_8));
                    logger.success("gradle.properties version updated: " + oldVersion + " -> " + newVersion);
                    return true;
                }
            }
            
        } catch (IOException e) {
            logger.error("Failed to update gradle.properties: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get information about the detected project.
     */
    public String getProjectInfo() {
        ProjectType type = detectProjectType();
        String version = getCurrentVersion();
        
        StringBuilder info = new StringBuilder();
        info.append("Project Type: ").append(type.getDisplayName());
        
        if (version != null) {
            info.append("\nCurrent Version: ").append(version);
            if (isSnapshot(version)) {
                info.append(" (SNAPSHOT)");
            }
        } else {
            info.append("\nVersion: Not detected");
        }
        
        return info.toString();
    }
}
