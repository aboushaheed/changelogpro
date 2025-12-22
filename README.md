# ChangeLog Pro

**Professional Changelog Management for IntelliJ IDEA**

ChangeLog Pro is a powerful IntelliJ IDEA plugin that helps you maintain professional, well-structured changelogs following the [Keep a Changelog](https://keepachangelog.com) standard and [Semantic Versioning](https://semver.org) principles.

## Overview

Managing changelogs manually can be tedious and error-prone, especially in teams where multiple developers work on the same project. ChangeLog Pro solves this by using a fragment-based approach where each change is stored as a separate YAML file, preventing merge conflicts and enabling parallel development.

## Features

### Universal Git Support

ChangeLog Pro works with all major Git hosting platforms including GitHub, GitLab, Bitbucket, Azure DevOps, Gitea, and Gogs. It automatically detects your Git provider from the repository URL and configures appropriate settings for Pull Request or Merge Request links.

### Issue Tracker Integration

Connect your changelog entries to your issue tracker of choice. ChangeLog Pro supports JIRA, GitHub Issues, GitLab Issues, YouTrack, Linear, Azure Boards, and Redmine. Custom issue trackers can also be configured with a base URL pattern.

### Multi-Step Wizard

The intuitive wizard guides you through creating changelog entries with validation at each step. Select the change type, enter details including description, issue ID, and PR/MR number, then review and confirm before creating the entry.

### Quick Add Buttons

For rapid entry creation, use the Quick Add buttons to create entries for any of the six standard change types: Added, Changed, Deprecated, Removed, Fixed, and Security.

### Comprehensive Documentation

Built-in documentation explains changelog best practices, the importance of keeping a changelog, and how to write effective entries. Access it anytime from the Help tab in the tool window.

## Installation

### From JetBrains Marketplace

1. Open IntelliJ IDEA
2. Go to **Settings → Plugins → Marketplace**
3. Search for "ChangeLog Pro"
4. Click **Install** and restart the IDE

### From Disk

1. Download the plugin ZIP file
2. Go to **Settings → Plugins → ⚙️ → Install Plugin from Disk...**
3. Select the downloaded ZIP file
4. Restart IntelliJ IDEA

## Getting Started

After installation, the ChangeLog Pro icon appears in the right sidebar. Click it to open the tool window.

### Initialize Your Project

Click **Initialize Project** to set up the changelog structure. The wizard will auto-detect your repository URL and Git provider. Configure your issue tracker and click OK to create the directory structure.

### Create Changelog Entries

Use the **New Entry (Wizard)** button for guided entry creation, or use the Quick Add buttons for rapid entry creation. Each entry is saved as a YAML file in `.changes/unreleased/` organized by change type.

### Directory Structure

After initialization, your project will have the following structure:

```
project-root/
├── .changes/
│   └── unreleased/
│       ├── added/
│       ├── changed/
│       ├── deprecated/
│       ├── removed/
│       ├── fixed/
│       └── security/
├── changelog-pro.properties
└── CHANGELOG.md
```

## Configuration

The Config tab in the tool window allows you to modify settings at any time:

| Setting | Description |
|---------|-------------|
| Repository URL | The URL of your Git repository |
| Git Provider | GitHub, GitLab, Bitbucket, etc. |
| Issue Tracker | JIRA, GitHub Issues, etc. |
| Tracker URL | Base URL for issue links |
| Auto-stage | Automatically stage files after creation |

## Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| New Changelog Entry | `Ctrl+Alt+L` |

## Building from Source

### Prerequisites

- JDK 11 or higher
- Gradle 8.x

### Build Commands

```bash
# Build the plugin
./gradlew buildPlugin

# Run in a sandbox IDE
./gradlew runIde

# Verify plugin compatibility
./gradlew verifyPlugin
```

The built plugin will be located at `build/distributions/changelog-pro-1.0.0.zip`.

## Compatibility

ChangeLog Pro is compatible with IntelliJ IDEA Community and Ultimate editions from version 2022.1 through 2025.3.

## Contributing

Contributions are welcome! Please read our contributing guidelines before submitting pull requests.

## Author

Developed by **Abdelmoula SOUIDI**

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## Support

For bug reports and feature requests, please contact abdelmoula.souidi@gmail.com. For more information, visit [Izem Technologies](https://www.izemtechnologies.com).
