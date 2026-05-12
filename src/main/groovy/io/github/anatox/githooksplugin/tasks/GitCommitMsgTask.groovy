// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.work.DisableCachingByDefault

@GitHook(event = 'commit-msg', command = './gradlew gitCommitMsg -Pgit.commitMsg.messageFile="$1"')
@DisableCachingByDefault(because = 'Git hooks depend on repository state')
abstract class GitCommitMsgTask extends AbstractGitHookTask {

    static TaskProvider<GitCommitMsgTask> register(Project project) {
        return project.tasks.register('gitCommitMsg', GitCommitMsgTask) {
            description = 'Run commit message checks'
        }
    }

}
