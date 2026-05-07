// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

@GitHook(event = 'pre-commit', command = './gradlew gitPreCommit')
abstract class GitPreCommitTask extends AbstractGitHookTask {

    private static final String PRE_COMMIT_STASH = 'gradle-setup-plugin-pre-commit'
    private static final String STASH_MARKER = '.git/gradle-precommit-stashed'

    static TaskProvider<GitPreCommitTask> register(Project project) {
        project.tasks.register('gitPreCommitCleanup') {
            group = 'git hooks'
            doLast {
                def marker = project.rootProject.file(STASH_MARKER)
                if (!marker.exists()) return
                marker.delete()
                try {
                    project.exec {
                        it.workingDir = project.rootDir
                        it.commandLine('git', 'stash', 'pop', '--index')
                    }.assertNormalExitValue()
                } catch (Exception e) {
                    project.exec {
                        it.workingDir = project.rootDir
                        it.commandLine('git', 'checkout', '--', '.')
                    }.assertNormalExitValue()
                    project.exec {
                        it.workingDir = project.rootDir
                        it.commandLine('git', 'stash', 'pop', '--index')
                    }.assertNormalExitValue()
                }
            }
        }
        return project.tasks.register('gitPreCommit', GitPreCommitTask) {
            description = 'Run pre-commit checks on staged files'
            finalizedBy project.tasks.named('gitPreCommitCleanup')
            doFirst {
                project.rootProject.file(STASH_MARKER).text = '1'
                project.exec {
                    it.workingDir = project.rootDir
                    it.commandLine('git', 'stash', 'push', '--keep-index', '--include-untracked', '-m', PRE_COMMIT_STASH)
                }.assertNormalExitValue()
                def proc = 'git diff --name-only --cached --diff-filter=ACMR'.execute([], project.rootDir)
                proc.waitFor()
                files.set(proc.text.trim().readLines().findAll { it })
            }
        }
    }

}
