# ChangeLog Pro

**Professional Changelog Management & Analytics for IntelliJ IDEA**

[![Version](https://img.shields.io/badge/version-2.0.0-blue.svg)](https://plugins.jetbrains.com/plugin/changelog-pro)
[![IntelliJ](https://img.shields.io/badge/IntelliJ-2022.1%20–%202025.3-orange.svg)](https://www.jetbrains.com/idea/)

ChangeLog Pro is a powerful IntelliJ IDEA plugin that helps you maintain professional, well-structured changelogs following the [Keep a Changelog](https://keepachangelog.com) standard and [Semantic Versioning](https://semver.org) principles.

## 🚀 What's New in v2.0

- **📊 Complete Analytics Dashboard** with 7 specialized panels
- **📈 Trends & Predictions** for release velocity and patterns
- **🔍 Git Insights** with conventional commits analysis
- **👥 Contributors Heatmap** (GitHub-style, 52 weeks)
- **🏥 Health Score** with actionable recommendations
- **📥 Git History Import** to import commits as changelog entries
- **📤 HTML Export** with light/dark themes

## ✨ Features

### 📊 Analytics Dashboard (NEW in v2.0)

Get deep insights into your project's changelog and Git history:

| Panel | Description |
|-------|-------------|
| **Timeline** | Chronological release view with period filtering |
| **Changes** | Visual breakdown by change type (bar/pie charts) |
| **Git Insights** | Commit activity, conventional commits %, coverage |
| **Contributors** | Team leaderboard with 52-week activity heatmap |
| **Health** | Project health score (A+ to F) with recommendations |
| **Trends** | Release velocity, commit patterns, predictions |
| **Compare** | Side-by-side release comparison |

**8 KPI Cards** at a glance:
- Releases · Commits · Avg Cycle · Version
- Contributors · Conventional % · Health · Breaking Changes

### 🔌 Universal Git Support

Works with all major Git hosting platforms:
- GitHub, GitLab, Bitbucket, Azure DevOps, Gitea, Gogs, Custom

### 🎫 Issue Tracker Integration

- JIRA, GitHub Issues, GitLab Issues, YouTrack, Linear, Azure Boards, Redmine, Custom

### 📝 Multi-Step Wizard

Guided changelog entry creation with validation at each step.

### ⚡ Quick Add Buttons

Rapid entry creation for all 6 standard change types:
- Added, Changed, Deprecated, Removed, Fixed, Security

### 📥 Git History Import

Import changelog entries directly from Git commits:
- Auto-detect conventional commits
- Map commit types to changelog categories
- Batch import with filtering

### 📤 HTML Export

Export analytics reports as standalone HTML files with:
- Light and dark theme support
- Customizable sections
- Professional styling

## 📦 Installation

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

## 🏁 Getting Started

After installation, the ChangeLog Pro icon appears in the right sidebar.

### 1. Initialize Your Project

Click **Initialize Project** to set up the changelog structure:
- Auto-detects your repository URL and Git provider
- Configure your issue tracker
- Creates the directory structure

### 2. Create Changelog Entries

- **New Entry (Wizard)** - Guided entry creation
- **Quick Add** - Rapid entry creation
- **Import Git** - Import from Git history

### 3. View Analytics

Click the **Analytics** tab to see:
- Project health score and recommendations
- Release velocity trends
- Contributors activity heatmap
- Git insights and conventional commits metrics

### Directory Structure

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

## ⚙️ Configuration

| Setting | Description |
|---------|-------------|
| Repository URL | The URL of your Git repository |
| Git Provider | GitHub, GitLab, Bitbucket, etc. |
| Issue Tracker | JIRA, GitHub Issues, etc. |
| Tracker URL | Base URL for issue links |
| Auto-stage | Automatically stage files after creation |

## ⌨️ Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| New Changelog Entry | `Ctrl+Alt+L` |

## 🛠️ Building from Source

### Prerequisites

- JDK 17 or higher
- Gradle 8.x

### Build Commands

```bash
# Build the plugin
./gradlew buildPlugin

# Run in a sandbox IDE
./gradlew runIde

# Run tests
./gradlew test

# Verify plugin compatibility
./gradlew verifyPlugin
```

The built plugin will be located at `build/distributions/changelog-pro-2.0.0.zip`.

## 🔧 Compatibility

ChangeLog Pro is compatible with:
- IntelliJ IDEA Community and Ultimate (2022.1 – 2025.3)
- Android Studio
- PyCharm, WebStorm, PhpStorm, and other JetBrains IDEs

## 📊 Analytics Features in Detail

### Health Score Calculation

The health score (0-100) is based on:
- Changelog usage and consistency
- Release frequency and cycle time
- Documentation coverage
- Conventional commits adoption
- Team bus factor

### Trend Analysis

- Release velocity (accelerating/stable/slowing)
- Commit patterns (peak hours, productive days)
- Next release prediction
- Technical debt indicators

### Git Insights

- Conventional commits percentage
- Breaking changes tracking
- Documentation coverage
- Monthly activity charts

## 👨‍💻 Author

Developed by **Abdelmoula SOUIDI**

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please read our contributing guidelines before submitting pull requests.

## 📧 Support

- **Issues**: [GitHub Issues](https://github.com/asouidi/changelog-pro/issues)
- **Email**: abdelmoula.souidi@gmail.com
