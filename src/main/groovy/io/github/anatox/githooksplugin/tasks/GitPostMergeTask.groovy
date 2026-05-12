// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.work.DisableCachingByDefault

@GitHook(event = 'post-merge', command = './gradlew gitPostMerge -Pgit.postMerge.isSquashMerge="$1"')
@DisableCachingByDefault(because = 'Git hooks depend on repository state')
abstract class GitPostMergeTask extends AbstractGitHookTask {

    static TaskProvider<GitPostMergeTask> register(Project project) {
        return project.tasks.register('gitPostMerge', GitPostMergeTask) {
            description = 'Run post-merge actions'
        }
    }

}
