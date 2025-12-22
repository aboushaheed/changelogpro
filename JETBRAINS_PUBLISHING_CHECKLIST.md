# JetBrains Marketplace Publishing Checklist

## Before Publishing Checklist

Before publishing a plugin, make sure it:

1. **Follows all recommendations from Plugin User Experience (UX)**
2. **Follows all requirements from Plugin Overview page**

## Key Requirements

### Plugin.xml Requirements
- Valid `<id>` - unique identifier
- Valid `<name>` - display name
- Valid `<vendor>` with email and URL
- Valid `<description>` - HTML formatted, detailed
- Valid `<change-notes>` - version history
- Valid `<depends>` - declare dependencies
- Valid `sinceBuild` and `untilBuild`

### Plugin Signing
- Plugin must be signed before publishing
- Use `signPlugin` Gradle task
- Certificate chain and private key required

### First Publication
- First plugin publication must always be uploaded manually
- Subsequent versions can be deployed via Gradle

### Do Not Repackage Libraries
- Do not repackage libraries into the main plugin JAR file
- Otherwise, Plugin Verifier will yield false positives

## Publishing Process

1. Create JetBrains Account
2. Upload plugin manually first time
3. Use Gradle for subsequent versions
4. Use Personal Access Token for automation

## Gradle Tasks

- `buildPlugin` - Create distribution ZIP
- `signPlugin` - Sign the plugin
- `publishPlugin` - Deploy to Marketplace

## Release Channels

- Default: Available to all users
- `alpha`: https://plugins.jetbrains.com/plugins/alpha/list
- `beta`: https://plugins.jetbrains.com/plugins/beta/list
- `eap`: https://plugins.jetbrains.com/plugins/eap/list


## Plugin User Experience (UX) Guidelines

### General Advice
- Plugin should bring significant value to users
- Prioritize crucial functionalities
- Consider sharing work in progress with limited group for feedback

### Ease of Use
- Features should work out of the box after installation
- No special user interactions required for crucial features
- Default settings should reflect typical usage
- Settings and actions should be easy to find
- Place settings in proper locations (e.g., Languages & Frameworks)

### Stability
- Implement functional tests to minimize regression issues
- Set up issue tracker for bug reports
- Implement error reporting from within IDE
- Use logging consistently

### Performance
- Follow performance tips (PSI Performance, Avoiding UI Freezes)
- Make functionality work during dumb mode when possible

### Distribution Size
- Decrease number of dependencies
- Reuse platform utilities and libraries
- No duplicate dependencies
- Optimize assets (icons, images)
- Consider on-demand download for large resources

### Consistent Behavior
- Design features similar to existing IDE functionalities
- Review existing plugins for consistency

### Consistent and Good-Looking UI
- Follow UI Guidelines
- Match icon style with IDE
- Use recommended UI controls
- Use UI Inspector to see existing implementations

### High-Quality Texts
- No typos or grammatical errors
- Follow Text section of UI Guidelines
- Proofread all texts

### Plugin Description and Presentation
- Clear and polished description
- Follow JetBrains Marketplace documentation rules
