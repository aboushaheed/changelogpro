plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.10.5"
}

group = "com.changelogpro"
version = "2.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.3")
        bundledPlugin("com.intellij.java")
    }

    // -----------------------------
    // Tests (JUnit 5 + JUnit 4 support)
    // -----------------------------
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    // REQUIRED to avoid: "Failed to load JUnit Platform"
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // If you still have JUnit 4 tests using org.junit.Test / @Rule TemporaryFolder:
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")

    testImplementation("org.mockito:mockito-core:5.13.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.13.0")

    testImplementation("org.assertj:assertj-core:3.26.3")
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
        }
    }

    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("243.*")

        pluginDescription.set(
            """
            <h2>ChangeLog Pro - Professional Changelog Management &amp; Analytics</h2>
            
            <p><b>ChangeLog Pro</b> is a powerful IntelliJ IDEA plugin that helps you maintain professional, 
            well-structured changelogs following the <a href="https://keepachangelog.com">Keep a Changelog</a> 
            standard and <a href="https://semver.org">Semantic Versioning</a> principles.</p>
            
            <h3>Key Features</h3>
            <ul>
                <li><b>Universal Git Support</b> - GitHub, GitLab, Bitbucket, Azure DevOps, Gitea, Gogs</li>
                <li><b>Multi-Step Wizard</b> - Guided changelog entry creation with validation</li>
                <li><b>Issue Tracker Integration</b> - JIRA, GitHub Issues, GitLab Issues, YouTrack, Linear</li>
                <li><b>Keep a Changelog Format</b> - Industry-standard changelog format</li>
                <li><b>Git Integration</b> - Automatic staging and commit URL linking</li>
            </ul>
            
            <h3>NEW in v2.0: Analytics Dashboard</h3>
            <ul>
                <li><b>Overview Cards</b> - Key metrics at a glance</li>
                <li><b>Release Timeline</b> - Chronological view with filtering</li>
                <li><b>Git Insights</b> - Conventional commits analysis, documentation coverage</li>
                <li><b>Contributors Panel</b> - GitHub-style heatmap (52 weeks)</li>
                <li><b>Health Score</b> - Project health rating with recommendations</li>
                <li><b>Git History Import</b> - Import commits as changelog entries</li>
                <li><b>HTML Export</b> - Export reports with light/dark themes</li>
            </ul>
            
            <p>Developed by <b>Abdelmoula SOUIDI</b></p>
            """.trimIndent()
        )

        changeNotes.set(
            """
            <h3>Version 2.0.0 - Analytics Dashboard</h3>
            <ul>
                <li><b>Added</b> - Complete Analytics Dashboard with 6 panels</li>
                <li><b>Added</b> - Git History Import dialog</li>
                <li><b>Added</b> - HTML Export with theme support</li>
                <li><b>Added</b> - GitHub-style contribution heatmap</li>
                <li><b>Added</b> - Health Score with recommendations</li>
                <li><b>Added</b> - Breaking changes tracking</li>
                <li><b>Added</b> - Bus factor calculation</li>
                <li><b>Added</b> - Data caching (5 min expiry)</li>
                <li><b>Changed</b> - Updated to Java 21</li>
                <li><b>Fixed</b> - All action buttons now functional</li>
            </ul>
            """.trimIndent()
        )
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
