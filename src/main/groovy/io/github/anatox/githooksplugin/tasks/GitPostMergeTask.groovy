// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider

@GitHook(event = 'post-merge', command = "sh -c './gradlew gitPostMerge -Pgit.isSquashMerge=\"\$1\"' sh")
abstract class GitPostMergeTask extends AbstractGitHookTask {

    abstract Property<String> getIsSquashMerge()

    static TaskProvider<GitPostMergeTask> register(Project project) {
        return project.tasks.register('gitPostMerge', GitPostMergeTask) {
            description = 'Run post-merge actions'
            doFirst {
                if (!isSquashMerge.isPresent()) {
                    isSquashMerge.set(project.findProperty('git.isSquashMerge') as String ?: '')
                }
            }
        }
    }

}
