// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider

@GitHook(event = 'pre-push', command = "sh -c './gradlew gitPrePush -Pgit.remoteName=\"\$1\" -Pgit.remoteUrl=\"\$2\"' sh")
abstract class GitPrePushTask extends AbstractGitHookTask {

    abstract Property<String> getRemoteName()
    abstract Property<String> getRemoteUrl()

    static TaskProvider<GitPrePushTask> register(Project project) {
        return project.tasks.register('gitPrePush', GitPrePushTask) {
            description = 'Run pre-push checks'
            doFirst {
                if (!remoteName.isPresent()) {
                    remoteName.set(project.findProperty('git.remoteName') as String ?: '')
                }
                if (!remoteUrl.isPresent()) {
                    remoteUrl.set(project.findProperty('git.remoteUrl') as String ?: '')
                }
                try {
                    def stdinText = System.in.readAllLines()
                    if (stdinText) {
                        files.set(stdinText)
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

}
