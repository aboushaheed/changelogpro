# ChangeLog Pro - Analytics Dashboard

## Overview

The Analytics Dashboard is a powerful feature in ChangeLog Pro v2.0 that provides comprehensive insights into your project's changelog management and Git history. It helps you understand release patterns, contributor activity, project health, and future predictions.

## Features

### 1. Overview Cards (8 KPIs in 2 rows)

**Row 1 - Core Metrics:**

| Card | Description |
|------|-------------|
| **Releases** | Total number of releases in your changelog |
| **Commits** | Total number of commits in your Git history |
| **Avg Cycle** | Average number of days between releases |
| **Version** | Current project version |

**Row 2 - Quality Metrics:**

| Card | Description |
|------|-------------|
| **Contributors** | Number of active contributors |
| **Conventional** | Percentage of conventional commits |
| **Health** | Project health grade (A+ to F) |
| **Breaking** | Total breaking changes count |

### 2. Timeline Tab

The Timeline tab shows a chronological view of all your releases:

- **Visual Timeline**: Releases organized with visual cards
- **Release Cards**: Each release shows:
  - Version number and release date
  - Change type badges (Added, Fixed, Changed, etc.)
  - Breaking change indicator ⚠️
  - Summary of changes
  - Total entry count
- **Period Filtering**: 
  - All Time
  - Last Year
  - Last 6 Months
  - Last 3 Months
- **Count Display**: "X of Y releases" when filtered

### 3. Changes Tab

Analyze the distribution of your changelog entries:

- **Bar Chart**: Visual representation of entry distribution by type
- **Breakdown Panel**: Detailed counts for each change type
- **Breaking Changes List**: All breaking changes across versions
- **Type Colors**: 
  - Added (green), Fixed (purple), Changed (blue)
  - Deprecated (yellow), Removed (red), Security (red)

### 4. Git Insights Tab

Deep dive into your Git repository activity:

- **Monthly Activity Chart**: Bar chart with gradient fills showing last 6 months
- **Key Metrics Panel**:
  - Total commits
  - Conventional commits count and percentage with progress bar
  - Documentation coverage with progress bar
  - Undocumented commits (highlighted if >20)
- **Conventional Breakdown**: All commit types sorted by frequency
- **Recent Commits Table** (7 columns):
  - Short hash (monospace, blue, clickable)
  - Conventional type (color-coded)
  - Subject (truncated)
  - Breaking change indicator ⚠️
  - Issue reference
  - Author
  - Date

### 5. Contributors Tab

Understand your team's contribution patterns:

- **GitHub-Style Heatmap** (52 weeks x 7 days):
  - Interactive tooltips showing "X commits on [date]"
  - Month labels across top
  - Day labels on left (Mon, Wed, Fri)
  - 5-level color intensity scale
  - Legend: "Less [5 boxes] More"
- **Leaderboard** (Top 15):
  - 🥇🥈🥉 medals for top 3
  - Commit count
  - Primary conventional commit type
  - Progress bars showing % of total
- **Team Stats Badges**:
  - Contributors count
  - Bus Factor with risk indicator (⚠️ Risk / Fair / Good)
  - Total Commits
  - Avg commits per person
  - Conventional Commits adoption %

### 6. Health Tab

Get a comprehensive health score for your changelog practices:

- **Score Circle**: Visual gauge showing overall score (0-100)
- **Grade System**: A+, A, B+, B, C+, C, D, F
- **Color-Coded Grades**:
  - Green: A+, A (≥90)
  - Blue: B+, B (≥75)
  - Yellow: C+, C (≥60)
  - Red: D, F (<60)
- **Criteria Breakdown**:
  - Changelog format compliance (20 pts)
  - Semantic versioning adherence (15 pts)
  - Documentation coverage (20 pts)
  - Release frequency (20 pts)
  - Breaking change documentation (15 pts)
  - Unreleased entry management (10 pts)
- **Recommendations Panel**: Actionable suggestions with buttons:
  - Import from Git History → Opens Git Import dialog
  - Add Entry → Opens New Entry wizard
  - Generate CHANGELOG → Opens Generate dialog
  - Open CHANGELOG.md → Opens file in editor

### 7. Trends Tab (NEW in v2.0)

Analyze patterns and predict future releases:

- **Release Velocity Panel**:
  - Average release cycle
  - Trend indicator (↗ Accelerating / → Stable / ↘ Slowing)
  - Releases per month

- **Commit Patterns Panel**:
  - Most productive day of week
  - Peak commit hour
  - Weekend commits percentage
  - Day-of-week activity chart (Mon-Sun)

- **Next Release Prediction Panel**:
  - Last release date
  - Days since last release
  - Predicted next release date
  - Release readiness progress bar
  - Unreleased changes count

- **Technical Debt Indicators**:
  - Undocumented commits (HIGH/MEDIUM/LOW severity)
  - Non-conventional commits percentage
  - Breaking changes accumulation
  - Low bus factor warning
  - Deprecated features pending removal

### 8. Compare Tab (NEW in v2.0)

Compare two releases side by side:

- **Release Selectors**: Dropdown to choose Release 1 and Release 2
- **Swap Button**: Quickly swap selected releases
- **Comparison Grid**:
  - Release Date
  - Days Between releases
  - Total Entries (with ↑↓ diff indicator)
  - Breaking Changes (highlighted in red)
  - Per-type breakdown (Added, Fixed, etc.)
- **Summary Panel**:
  - Size comparison (more/fewer changes)
  - Breaking changes warning
  - Focus analysis (dominant change type per release)

## Action Buttons

### Header Buttons

| Button | Description |
|--------|-------------|
| **Export HTML** | Export analytics as standalone HTML report |
| **Import Git** | Import changelog entries from Git history |
| **Refresh** | Force reload all analytics data |

### Git Import Dialog

Full-featured dialog for importing from Git:

- **Range Options**: Since last tag, Last 50/100/200, All commits
- **Filters**: Conventional only, Exclude merge commits
- **Interactive Table**: Checkbox selection, type mapping
- **Auto-mapping**: feat→ADDED, fix→FIXED, refactor→CHANGED, etc.
- **Batch Operations**: Select All, Select None, Select Conventional

### HTML Export Dialog

Export analytics as a styled HTML report:

- **Section Selection**: Choose which panels to include
- **Theme**: Light or Dark mode
- **Sections Available**:
  - Release Timeline
  - Changes Distribution
  - Git Insights
  - Contributors
  - Health Score
  - Recent Commits Table

## Data Caching

- **Cache Duration**: 5 minutes
- **Force Refresh**: Click refresh button or use `loadDataAsync(true)`
- **Clear Cache**: Automatically clears on project close
- **Cache Key**: Based on project path

## Git Provider Support

Works with all major Git providers:
- GitHub, GitLab, Bitbucket, Azure DevOps, Gitea, Gogs

Data is collected using standard Git CLI commands.

## Conventional Commits

Recognizes [Conventional Commits](https://www.conventionalcommits.org/) format:

```
<type>[optional scope][!]: <description>
```

**Supported Types:**
| Type | Changelog Mapping | Color |
|------|-------------------|-------|
| feat | ADDED | Green |
| fix | FIXED | Purple |
| docs | (skipped) | Blue |
| perf | FIXED | Yellow |
| refactor | CHANGED | Gray |
| revert | REMOVED | Red |
| security | SECURITY | Red |
| test, build, ci, chore | (skipped) | Gray |

**Breaking Changes Detection:**
- `!` after type/scope: `feat!: breaking change`
- `BREAKING CHANGE:` in commit body

## Health Score Criteria

| Criterion | Max Score | Description |
|-----------|-----------|-------------|
| Changelog format | 20 | Keep a Changelog compliance |
| Semantic versioning | 15 | SemVer pattern adherence |
| Documentation coverage | 20 | % of documented commits |
| Release frequency | 20 | 14-60 days is ideal |
| Breaking change docs | 15 | Breaking changes documented |
| Unreleased management | 10 | Reasonable pending count |

## Performance

- Asynchronous loading (non-blocking)
- Recent commits limited to 200 for display
- Historical analysis uses up to 1000 commits
- Caching reduces redundant Git operations
- Lazy panel initialization

## Troubleshooting

### No Git data showing
- Ensure you're in a Git repository
- Check that Git is installed and in PATH
- Verify the project path is correct

### Empty releases list
- Check that CHANGELOG.md exists in project root
- Verify Keep a Changelog format
- Try: changelog.md, HISTORY.md, CHANGES.md

### Low health score
Review recommendations panel. Common issues:
- Missing dates on releases
- No conventional commits
- Too many undocumented commits
- Infrequent or too frequent releases
