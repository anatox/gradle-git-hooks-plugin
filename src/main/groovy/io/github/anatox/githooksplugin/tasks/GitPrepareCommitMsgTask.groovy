// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider

@GitHook(event = 'prepare-commit-msg', command = "sh -c './gradlew gitPrepareCommitMsg -Pgit.commitMsgFile=\"\$1\" -Pgit.msgSource=\"\$2\" -Pgit.objectName=\"\$3\"' sh")
abstract class GitPrepareCommitMsgTask extends AbstractGitHookTask {

    abstract Property<String> getCommitMsgFile()
    abstract Property<String> getMsgSource()
    abstract Property<String> getObjectName()

    static TaskProvider<GitPrepareCommitMsgTask> register(Project project) {
        return project.tasks.register('gitPrepareCommitMsg', GitPrepareCommitMsgTask) {
            description = 'Prepare or modify the commit message'
            doFirst {
                if (!commitMsgFile.isPresent()) {
                    commitMsgFile.set(project.findProperty('git.commitMsgFile') as String ?: '')
                }
                if (!msgSource.isPresent()) {
                    msgSource.set(project.findProperty('git.msgSource') as String ?: '')
                }
                if (!objectName.isPresent()) {
                    objectName.set(project.findProperty('git.objectName') as String ?: '')
                }
            }
        }
    }

}
