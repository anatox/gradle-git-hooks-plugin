# AGENTS.md - Gradle Git Hooks Plugin

## Project Brief

**Gradle Git Hooks Plugin** provides config-based Git hooks (Git 2.54+) managed through Gradle. It consists of:
- **Main plugin** (`GitHooksPlugin`) — applies to `Project`, registers hook tasks and the `gitHooks` extension
- **Setup plugin** (`GitHooksSetupPlugin`) — applies to `Settings`, auto-runs during initialization to configure Git config, hooks, LFS, and submodules

**Plugin IDs**: `io.github.anatox.git-hooks` (main), `io.github.anatox.git-hooks.setup` (setup)
**Version**: managed by axion-release (see [Versioning](#versioning))

## Contexts

### 1. Plugin Development Context
When working on the plugin **itself** (in this repository):
- Use `./gradlew setup` for full environment setup
- Plugin development follows standard Gradle plugin conventions
- The plugin has its own setup tasks defined in `build.gradle` for self-configuration
- Testing: Use `./gradlew publishToMavenLocal` to test locally

### 2. Plugin Usage Context
When **using** this plugin in other projects:
- Plugin runs automatically during Gradle initialization
- Configure via `settings.gradle` plugin management
- In CI, use `-Psetup` flag to force execution
- Plugin reads configuration files from project root (`.gitconfig`, `.githooks`, `.gitmodules`)
- Git hooks are managed via the `GitHooksPlugin` and `gitHooks` extension

## Project Specifics

### Architecture
- **Main class**: `GitHooksSetupPlugin` implements `Plugin<Settings>` (runs in initialization phase)
- **Plugin class**: `GitHooksPlugin` implements `Plugin<Project>` (registers git hook tasks and extension)
- **Package**: `io.github.anatox.githooksplugin` — root for both plugins and extension (`GitHookElement`)
- **Tasks package**: `io.github.anatox.githooksplugin.tasks` — `AbstractGitHookTask` base class, `@GitHook` annotation, and all concrete hook tasks
- **Caching**: Uses SHA-256 hash of config files in `.gradle/[plugin-id]/setup.properties`
- **Conditional execution**: Skips in CI unless `-Psetup` property is provided

### Versioning
Versioning is automated via [axion-release](https://github.com/allegro/axion-release-plugin) and driven by **conventional commits**. The plugin uses a custom `versionIncrementer` that reads commit messages between Git tags (`v` prefix) to determine the version bump:

- **Major bump**: commits containing `BREAKING CHANGE`, `BREAKING-CHANGE`, or `!:` shorthand (e.g. `feat!:`, `fix(api)!:`)
- **Minor bump**: commits starting with `feat:` or `feat(scope):`
- **Patch bump**: everything else

Tags use a `v` prefix (e.g. `v0.1.6` → `v0.2.0`). The version is set via `version = scmVersion.version` and the previous version is available as `scmVersion.previousVersion` for changelog generation.

Changelog and GitHub releases are handled by `shipkit-changelog` and `shipkit-github-release` plugins, consuming the axion-release version.

### Key Features
1. **Git Configuration**: Includes local `.gitconfig` if present
2. **Config-Based Git Hooks**: `installGitHooks` task generates `.githooks` with hook commands; setup plugin includes it via `git config include.path` to enable Git 2.54+ config-based hooks
3. **Git LFS**: Installs and configures Git LFS, pulls LFS files
4. **Git Hooks**: Fixes shebang lines (`#!/bin/sh` → `#!/usr/bin/env sh`)
5. **Submodules**: Initializes and updates Git submodules
6. **Cross-platform**: Handles Windows/Unix line endings with `dos2unix`

### Dependencies
- **Runtime**: Gradle API only (no external dependencies)
- **Build**: Groovy, Java 17+
- **Consumer**: Git 2.54+ (for config-based hooks)

## Code Style & Conventions

### File Headers
**All new source files must include SPDX license headers**:
```groovy
// SPDX-FileCopyrightText: [Year] [Author/Organization] [Email]
// SPDX-License-Identifier: Apache-2.0
```

### Naming
- **Classes**: `GitHooksSetupPlugin`, `Utils` (PascalCase)
- **Methods**: `isUpToDate()`, `computeHash()` (camelCase)
- **Constants**: `PLUGIN_ID`, `MARKER_PATH` (SCREAMING_SNAKE_CASE)
- **Variables**: `rootDir`, `markerFile` (camelCase)

### Logging
- Use `logger.lifecycle()` for user-facing messages with `[setup]` prefix
- Use `logger.info()` for informational messages
- Use `logger.warn()` for non-critical issues

### Error Handling
- Throw `RuntimeException` for command execution failures
- Log warnings for non-critical issues (e.g., dos2unix failures)
- Use `ignoreExitValue = true` for optional operations

### Groovy Conventions
- Use `def` for local variables
- Use static typing for public API methods
- Prefer Groovy collections and closures over Java equivalents
- Use safe navigation operator (`?.`) for nullable references

## Important Notes

### Plugin Lifecycle
- Setup plugin runs in `Settings` phase (before project evaluation)
- Main plugin runs in `Project` phase
- Caches state based on config file hashes
- Automatically skips if already up-to-date

### Configuration Files
The plugin looks for these files in the project root:
- `.gitconfig` (optional) - Git configuration
- `.githooks` (generated) - Config-based Git hooks (Git 2.54+), outputs of `installGitHooks` task
- `.gitmodules` (optional) - Git submodules

### Environment Variables
- `CI` - When set, plugin skips automatic execution (use `-Psetup` to force)
- Plugin respects standard Gradle properties for publishing

### Testing Strategy
1. **Local testing**: `./gradlew publishToMavenLocal`
2. **Integration**: Apply to test project with `settings.gradle`
3. **Manual verification**: Check created files and Git configuration

## Development Workflow

1. **Setup**: `./gradlew setup` (configures development environment)
2. **Build**: `./gradlew build` (compiles plugin)
3. **Test**: `./gradlew publishToMavenLocal` (install locally)
4. **Publish**: Set `MAVEN_*` env vars and run `./gradlew publish`

## Common Tasks

### Adding New Setup Step
1. Add private static method to `GitHooksSetupPlugin`
2. Call from `apply()` method
3. Include relevant file in `computeHash()` if caching is needed
4. Update `README.md` if functionality is user-facing

### Adding New Git Hook Task
1. Create new task class in `io.github.anatox.githooksplugin.tasks` extending `AbstractGitHookTask`
2. Annotate with `@GitHook(event = '...', command = '...')`
3. Implement static `register(Project)` method
4. Register the task in `GitHooksPlugin.apply()`

### Debugging
- Run with `./gradlew -Psetup` to force execution
- Check `.gradle/[plugin-id]/setup.properties` for cache state
- Enable Gradle debug logging with `--debug` flag

### Cross-platform Issues
- Use `isWindows()` helper for OS-specific logic
- Handle line endings in Git hooks
