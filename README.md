# Gradle Git Hooks Plugin

Gradle plugin for config-based Git hooks management with auto-setup.

* Provides config-based Git hooks (Git 2.54+) managed through Gradle
* Includes a companion setup plugin for automatic initialization during Gradle init phase
* Also automates Git configuration, LFS, and submodule management

<!-- TOC -->
- [Gradle Git Hooks Plugin](#gradle-git-hooks-plugin)
  - [Plugin Development](#plugin-development)
    - [Quick Start](#quick-start)
    - [Useful Commands](#useful-commands)
  - [Using the Plugin](#using-the-plugin)
    - [Connecting the Plugin](#connecting-the-plugin)
    - [Configuring Git Hooks (Consumer Project)](#configuring-git-hooks-consumer-project)
    - [Composing Git Hook Tasks](#composing-git-hook-tasks)
    - [Running Setup](#running-setup)
    - [Initial Setup](#initial-setup)
  - [Useful Links](#useful-links)
<!-- /TOC -->

## Plugin Development

### Quick Start

System Requirements:
- Java 17 or higher
- Git 2.54+ (for config-based hooks)

Clone the repository and perform full setup:

```shell
git clone <repository-url> git-hooks-plugin
cd git-hooks-plugin
./gradlew setup
```

The `./gradlew setup` command will perform full environment configuration for the plugin:
- Configures Git options from `.gitconfig`
- Configures Git hooks options from `.githooks`
- Installs Git LFS and pulls LFS files
- Initializes and updates Git submodules

### Useful Commands

For plugin development:

- `./gradlew setup` — Full plugin repository setup
- `./gradlew build` — Plugin compilation
- `./gradlew publishToMavenLocal` — Publication to local Maven repository for testing
- `./gradlew publish` — Publication to remote Maven repository

Versioning is automated via [axion-release](https://github.com/allegro/axion-release-plugin) and driven by [conventional commits](https://www.conventionalcommits.org/):
- `feat:` / `feat(scope):` → minor bump
- `BREAKING CHANGE:` / `fix!:`, `xxx!:` → major bump
- everything else → patch bump

Tags use a `v` prefix (e.g. `v0.1.6` → `v0.2.0`).

Changelog and GitHub releases are handled by [shipkit-changelog](https://github.com/shipkit/shipkit-changelog) and [shipkit-github-release](https://github.com/shipkit/shipkit-github-release) plugins, consuming the axion-release version.

The following environment variables are required for publishing to a remote repository:
- `MAVEN_USERNAME` — Username / token
- `MAVEN_PASSWORD` — Password / access token
- `MAVEN_PUBLISH_URL` — Repository URL (or pass via `-Pmaven.publish.url=...`)

Separate setup components (used automatically when running `./gradlew setup`):

- `./gradlew setupGitConfig` — Connects local `.gitconfig` (runs only if `.gitconfig` exists)
- `./gradlew setupGitHooks` — Configures Git LFS and git hooks (LFS install + pull)
- `./gradlew gitLfsInstall` — Installs Git LFS and fixes hook shebangs
- `./gradlew gitLfsPull` — Pulls Git LFS files

Hook management tasks:
- `./gradlew installGitHooks` — Generates the `.githooks` config-based hooks file from registered hook tasks
- `./gradlew gitPreCommit` — Runs pre-commit checks
- `./gradlew gitCommitMsg`, `gitPrePush`, `gitPostCheckout` — etc.

## Using the Plugin

### Connecting the Plugin

Add the repository and plugin to your project's `settings.gradle` (example):

```gradle
pluginManagement {
    repositories {
        maven {
            url = System.getenv('EXAMPLE_URL')
            credentials {
                username = System.getenv('EXAMPLE_USERNAME')
                password = System.getenv('EXAMPLE_PASSWORD')
            }
        }
        gradlePluginPortal()
    }
}

plugins {
    id 'io.github.anatox.git-hooks.setup' version '0.1.0'
    id 'io.github.anatox.git-hooks' version '0.1.0'
}
```

The `io.github.anatox.git-hooks.setup` plugin runs automatically during initialization. The `io.github.anatox.git-hooks` plugin provides the `gitHooks` extension and registers hook tasks in the project.

### Configuring Git Hooks (Consumer Project)

In your `build.gradle`, enable or disable specific git hooks via the `gitHooks` extension:

```gradle
gitHooks {
    'pre-commit' {
        enable = true
    }
    'pre-push' {
        enable = false
    }
}
```

### Composing Git Hook Tasks

The plugin registers core hook tasks (`gitPreCommit`, `gitPrePush`, etc.) that consumers can wire additional tasks to via Gradle's `dependsOn` mechanism. This allows composing project-specific checks into the hook pipeline.

Hook context is available via the `gitHooksContext` extension, providing typed access to staged files (`ConfigurableFileCollection`), hook properties, and other Git-provided values.

**Example: Register and wire custom checks**

```groovy
tasks.register('checkSpotless') {
    group = 'git hooks'
    description = 'Run Spotless format check'
    dependsOn 'spotlessCheck'
}

tasks.register('checkMergeConflicts') {
    group = 'git hooks'
    description = 'Check for git merge conflict markers in staged files'
    dependsOn 'gitPreCommitPrepare'
    doLast {
        gitHooksContext.preCommit.stagedFiles.each { stagedFile ->
            if (stagedFile.text.contains('<<<<<<<')) {
                throw new GradleException("Merge conflict markers in: ${stagedFile.path}")
            }
        }
    }
}

// Wire custom tasks as dependencies of git hook tasks
tasks.named('gitPreCommit') {
    dependsOn 'checkSpotless'
    dependsOn 'checkMergeConflicts'
}

tasks.named('gitPrePush') {
    dependsOn 'checkSpotless'
}
```

The `gitPreCommit` ensures `gitPreCommitPrepare` stashes and detects files first, then runs all dependencies. A failing dependency causes the hook to fail, preventing the commit or push.

**Using hook context for commit-msg hooks:**

```groovy
tasks.register('validateCommitMessage') {
    group = 'git hooks'
    doLast {
        def msgFile = gitHooksContext.commitMsg.messageFile.get().asFile
        if (msgFile.exists() && msgFile.text.trim().length() < 10) {
            throw new GradleException('Commit message too short')
        }
    }
}
tasks.named('gitCommitMsg') {
    dependsOn 'validateCommitMessage'
}
```

**Using typed context properties:**

```groovy
tasks.register('checkPrepareCommitMsg') {
    group = 'git hooks'
    doLast {
        def msgFile = gitHooksContext.prepareCommitMsg.messageFile.get().asFile
        def source = gitHooksContext.prepareCommitMsg.messageSource.get()
        if (source == GitPrepareMessageSource.MERGE) {
            println 'Preparing merge commit message'
        }
    }
}

tasks.register('checkPostMerge') {
    group = 'git hooks'
    doLast {
        def mergeFlag = gitHooksContext.postMerge.isSquashMerge.get()
        if (mergeFlag == GitPostMergeFlag.SQUASH_MERGE) {
            println 'Squash merge detected'
        }
    }
}
```

You can also disable individual hooks via the `gitHooks` extension:

```groovy
gitHooks {
    'pre-push' {
        enable = false
    }
}
```

### Running Setup

The plugin runs **automatically** with each Gradle command execution — setup occurs during the initialization phase (`settings.gradle`).

```shell
./gradlew   # setup will run automatically with any command
```

In CI environment (when `CI` environment variable is set), automatic setup does not run. To force setup in CI, use the `-Psetup` flag:

```shell
./gradlew -Psetup build
```

The plugin will automatically configure:
- Git configuration and submodules
- Config-based Git hooks (via `.githooks`)
- Git LFS

### Initial Setup

Before running the plugin, you can prepare optional configuration files in the repository root:

- `.gitconfig` — Git options configuration (can be skipped if not needed)
- `.githooks` — Config-based Git hooks (Git 2.54+), generated by `installGitHooks`
- `.gitmodules` — If using Git submodules

## Useful Links

- [Gradle Plugin Development Guide](https://docs.gradle.org/current/userguide/custom_plugins.html)
- [Config-Based Git Hooks (Git 2.54+)](https://git-scm.com/docs/githooks#_config_hooks)
- [Git LFS Documentation](https://git-lfs.github.com/)
