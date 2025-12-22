# Changelog

All notable changes to ChangeLog Pro will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2024-12-22

### Added

- Initial release of ChangeLog Pro
- Multi-step wizard for creating changelog entries with validation and preview
- Quick Add buttons for rapid entry creation (Added, Changed, Deprecated, Removed, Fixed, Security)
- Universal Git provider support:
  - GitHub
  - GitLab
  - Bitbucket
  - Azure DevOps
  - Gitea
  - Gogs
  - Custom/Self-hosted
- Issue tracker integration:
  - JIRA
  - GitHub Issues
  - GitLab Issues
  - YouTrack
  - Linear
  - Azure Boards
  - Redmine
  - Custom
- Automatic Git staging of changelog files after creation
- Auto-detection of Git provider from repository URL
- Fragment-based changelog approach (each entry is a separate YAML file)
- Right sidebar tool window with four tabs:
  - Actions: Initialize project, New Entry wizard, Quick Add buttons
  - Config: Repository URL, Git provider, Issue tracker settings
  - Log: Real-time operation logging
  - Help: Quick reference guide
- Comprehensive in-plugin documentation covering:
  - Overview and features
  - Why Keep a Changelog
  - Getting started guide
  - Change types explanation
  - Best practices
  - Supported platforms
- Welcome notification on first installation
- Keyboard shortcut (Ctrl+Alt+L) for quick entry creation
- Compatible with IntelliJ IDEA 2022.1 through 2025.3

[1.0.0]: https://github.com/changelogpro/changelog-pro-intellij/releases/tag/v1.0.0
