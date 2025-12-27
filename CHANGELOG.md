# Changelog

All notable changes to ChangeLog Pro will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0] - 2024-12-27

### Added

- **Complete Analytics Dashboard** with 7 specialized panels:
  - **Timeline Panel**: Chronological release view with period filtering (All Time, Last Year, 6 Months, 3 Months)
  - **Changes Panel**: Visual breakdown by change type with bar charts and pie charts
  - **Git Insights Panel**: Commit activity analysis, conventional commits tracking, documentation coverage
  - **Contributors Panel**: Team leaderboard with GitHub-style contribution heatmap (52 weeks)
  - **Health Panel**: Project health score (0-100) with grades (A+ to F) and actionable recommendations
  - **Trends Panel**: Release velocity trends, commit patterns, next release prediction, technical debt indicators
  - **Comparison Panel**: Side-by-side release comparison with detailed metrics

- **8 KPI Overview Cards**:
  - Releases count, Commits count, Average cycle time, Current version
  - Contributors count, Conventional commits %, Health grade, Breaking changes count

- **Git History Import Dialog**: Import changelog entries directly from Git commits
  - Range selector (Since last tag, Last 50/100/200 commits, All)
  - Conventional commit auto-detection and mapping
  - Interactive table with filtering options
  - Batch selection tools

- **HTML Export**: Export analytics reports as standalone HTML files
  - Light and dark theme support
  - Customizable sections
  - Professional styling with charts and tables

- **Advanced Analytics Features**:
  - Conventional Commits detection (feat, fix, docs, refactor, perf, test, build, ci, chore)
  - Breaking changes tracking with `!` indicator
  - Bus factor calculation for team risk assessment
  - Release velocity trend analysis (accelerating/slowing/stable)
  - Commit pattern analysis (peak hours, productive days, weekend activity)
  - Next release prediction based on historical data
  - Technical debt indicators
  - Documentation coverage metrics

- **Data Caching**: 5-minute cache for analytics data with force refresh option
- **Auto-refresh**: Analytics tab automatically refreshes when opened
- Commit URL linking for all Git providers
- Editor popup menu action: "Add to Changelog"

### Changed

- Upgraded to Java 17 (from Java 11)
- `ChangeEntry` now supports `commitHash` and `breakingChange` fields
- `GitProvider` now includes `buildCommitUrl()` method
- Improved YAML output with breaking change and commit hash fields
- Markdown output now includes `**BREAKING:**` prefix and commit links
- Enhanced UI with 8 KPI cards in 2 rows

### Fixed

- Timeline period filter now properly filters releases
- Health panel recommendation buttons now wire to actual dialogs
- All placeholder methods replaced with real implementations

## [1.0.1] - 2024-12-24

### Fixed

- Replaced deprecated `Project.getBaseDir()` API with `Project.getBasePath()` for IntelliJ 2025.x compatibility

## [1.0.0] - 2024-12-22

### Added

- Initial release of ChangeLog Pro
- Multi-step wizard for creating changelog entries with validation and preview
- Quick Add buttons for rapid entry creation (Added, Changed, Deprecated, Removed, Fixed, Security)
- Universal Git provider support:
  - GitHub, GitLab, Bitbucket, Azure DevOps, Gitea, Gogs, Custom/Self-hosted
- Issue tracker integration:
  - JIRA, GitHub Issues, GitLab Issues, YouTrack, Linear, Azure Boards, Redmine, Custom
- Automatic Git staging of changelog files after creation
- Auto-detection of Git provider from repository URL
- Fragment-based changelog approach (each entry is a separate YAML file)
- Right sidebar tool window with Actions, Config, Log, and Help tabs
- Comprehensive in-plugin documentation
- Welcome notification on first installation
- Keyboard shortcut (Ctrl+Alt+L) for quick entry creation
- Compatible with IntelliJ IDEA 2022.1 through 2025.3

[2.0.0]: https://github.com/asouidi/changelog-pro/releases/tag/v2.0.0
[1.0.1]: https://github.com/asouidi/changelog-pro/releases/tag/v1.0.1
[1.0.0]: https://github.com/asouidi/changelog-pro/releases/tag/v1.0.0
