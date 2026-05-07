// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider

@GitHook(event = 'commit-msg', command = "sh -c './gradlew gitCommitMsg -Pgit.commitMsgFile=\"\$1\"' sh")
abstract class GitCommitMsgTask extends AbstractGitHookTask {

    abstract Property<String> getCommitMsgFile()

    static TaskProvider<GitCommitMsgTask> register(Project project) {
        return project.tasks.register('gitCommitMsg', GitCommitMsgTask) {
            description = 'Run commit message checks'
            doFirst {
                if (!commitMsgFile.isPresent()) {
                    commitMsgFile.set(project.findProperty('git.commitMsgFile') as String ?: '')
                }
            }
        }
    }

}
