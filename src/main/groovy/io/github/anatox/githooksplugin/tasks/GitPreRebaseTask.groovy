// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.work.DisableCachingByDefault

@GitHook(event = 'pre-rebase', command = './gradlew gitPreRebase -Pgit.preRebase.upstream="$1" -Pgit.preRebase.branch="$2"')
@DisableCachingByDefault(because = 'Git hooks depend on repository state')
abstract class GitPreRebaseTask extends AbstractGitHookTask {

    static TaskProvider<GitPreRebaseTask> register(Project project) {
        return project.tasks.register('gitPreRebase', GitPreRebaseTask) {
            description = 'Run pre-rebase checks'
        }
    }

}
