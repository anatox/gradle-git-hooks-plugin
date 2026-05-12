// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.work.DisableCachingByDefault

@GitHook(event = 'post-commit', command = './gradlew gitPostCommit')
@DisableCachingByDefault(because = 'Git hooks depend on repository state')
abstract class GitPostCommitTask extends AbstractGitHookTask {

    static TaskProvider<GitPostCommitTask> register(Project project) {
        return project.tasks.register('gitPostCommit', GitPostCommitTask) {
            description = 'Run post-commit actions'
        }
    }

}
