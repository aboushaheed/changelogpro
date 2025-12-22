plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.changelogpro"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

intellij {
    version.set("2022.1.4")
    type.set("IC")
    updateSinceUntilBuild.set(false)
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
        options.encoding = "UTF-8"
    }

    test {
        useJUnit()
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
        }
    }

    patchPluginXml {
        sinceBuild.set("221")
        untilBuild.set("253.*")
        
        pluginDescription.set("""
            <h2>ChangeLog Pro - Professional Changelog Management</h2>
            
            <p><b>ChangeLog Pro</b> is a powerful IntelliJ IDEA plugin that helps you maintain professional, 
            well-structured changelogs following the <a href="https://keepachangelog.com">Keep a Changelog</a> 
            standard and <a href="https://semver.org">Semantic Versioning</a> principles.</p>
            
            <h3>Key Features</h3>
            <ul>
                <li><b>Universal Git Support</b> - Works with GitHub, GitLab, Bitbucket, Azure DevOps, and any Git repository</li>
                <li><b>Multi-Step Wizard</b> - Guided changelog entry creation with validation</li>
                <li><b>Issue Tracker Integration</b> - JIRA, GitHub Issues, GitLab Issues, YouTrack, and more</li>
                <li><b>Auto-Detection</b> - Automatically detects your Git provider and issue tracker</li>
                <li><b>Keep a Changelog Format</b> - Follows the industry-standard changelog format</li>
                <li><b>Git Integration</b> - Automatic staging of changelog files</li>
                <li><b>In-Plugin Documentation</b> - Complete guide to changelog best practices</li>
            </ul>
            
            <h3>Why Keep a Changelog?</h3>
            <p>A changelog makes it easier for users and contributors to see what notable changes have been made 
            between each release. It's a curated, chronologically ordered list of notable changes for each version 
            of a project.</p>
            
            <h3>Getting Started</h3>
            <ol>
                <li>Open the ChangeLog Pro panel from the right sidebar</li>
                <li>Click "Initialize Project" to set up your changelog structure</li>
                <li>Use "New Entry" to add changelog entries as you develop</li>
            </ol>
            
            <h3>Author</h3>
            <p>Developed by <b>Abdelmoula SOUIDI</b></p>
        """.trimIndent())
        
        changeNotes.set("""
            <h3>Version 1.0.0</h3>
            <ul>
                <li>Initial release</li>
                <li>Universal Git provider support (GitHub, GitLab, Bitbucket, Azure DevOps)</li>
                <li>Multi-step changelog entry wizard</li>
                <li>Issue tracker integration (JIRA, GitHub Issues, GitLab Issues, YouTrack)</li>
                <li>Keep a Changelog format compliance</li>
                <li>Comprehensive in-plugin documentation</li>
                <li>IntelliJ IDEA 2022.1 - 2025.3 compatibility</li>
            </ul>
        """.trimIndent())
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    buildSearchableOptions {
        enabled = false
    }
}
