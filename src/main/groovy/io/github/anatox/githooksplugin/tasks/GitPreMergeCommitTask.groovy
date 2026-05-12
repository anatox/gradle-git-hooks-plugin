// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.work.DisableCachingByDefault

@GitHook(event = 'pre-merge-commit', command = './gradlew gitPreMergeCommit')
@DisableCachingByDefault(because = 'Git hooks depend on repository state')
abstract class GitPreMergeCommitTask extends AbstractGitHookTask {

    static TaskProvider<GitPreMergeCommitTask> register(Project project) {
        return project.tasks.register('gitPreMergeCommit', GitPreMergeCommitTask) {
            description = 'Run pre-merge-commit checks'
            dependsOn 'gitPreMergeCommitPrepare'
        }
    }

}
