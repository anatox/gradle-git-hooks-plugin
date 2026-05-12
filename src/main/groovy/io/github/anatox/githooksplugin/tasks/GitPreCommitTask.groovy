// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.work.DisableCachingByDefault

@GitHook(event = 'pre-commit', command = './gradlew gitPreCommit')
@DisableCachingByDefault(because = 'Git hooks depend on repository state')
abstract class GitPreCommitTask extends AbstractGitHookTask {

    static TaskProvider<GitPreCommitTask> register(Project project) {
        project.tasks.register('gitPreCommitCleanup', GitPreCommitCleanupTask) {
            group = 'git hooks'
        }
        return project.tasks.register('gitPreCommit', GitPreCommitTask) {
            description = 'Run pre-commit checks on staged files'
            dependsOn 'gitPreCommitPrepare'
            finalizedBy project.tasks.named('gitPreCommitCleanup')
        }
    }

}
