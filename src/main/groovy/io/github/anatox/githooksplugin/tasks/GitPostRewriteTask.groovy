// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider

@GitHook(event = 'post-rewrite', command = "sh -c './gradlew gitPostRewrite -Pgit.command=\"\$1\"' sh")
abstract class GitPostRewriteTask extends AbstractGitHookTask {

    abstract Property<String> getCommand()

    static TaskProvider<GitPostRewriteTask> register(Project project) {
        return project.tasks.register('gitPostRewrite', GitPostRewriteTask) {
            description = 'Run post-rewrite actions'
            doFirst {
                if (!command.isPresent()) {
                    command.set(project.findProperty('git.command') as String ?: '')
                }
            }
        }
    }

}
