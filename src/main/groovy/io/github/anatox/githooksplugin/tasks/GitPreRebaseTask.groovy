// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider

@GitHook(event = 'pre-rebase', command = "sh -c './gradlew gitPreRebase -Pgit.upstream=\"\$1\" -Pgit.branch=\"\$2\"' sh")
abstract class GitPreRebaseTask extends AbstractGitHookTask {

    abstract Property<String> getUpstream()
    abstract Property<String> getBranch()

    static TaskProvider<GitPreRebaseTask> register(Project project) {
        return project.tasks.register('gitPreRebase', GitPreRebaseTask) {
            description = 'Run pre-rebase checks'
            doFirst {
                if (!upstream.isPresent()) {
                    upstream.set(project.findProperty('git.upstream') as String ?: '')
                }
                if (!branch.isPresent()) {
                    branch.set(project.findProperty('git.branch') as String ?: '')
                }
            }
        }
    }

}
