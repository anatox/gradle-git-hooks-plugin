// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.work.DisableCachingByDefault

@GitHook(event = 'prepare-commit-msg', command = '''
    ./gradlew gitPrepareCommitMsg
        -Pgit.prepareCommitMsg.messageFile="$1"
        -Pgit.prepareCommitMsg.messageSource="$2"
        -Pgit.prepareCommitMsg.objectName="$3"
    ''')
@DisableCachingByDefault(because = 'Git hooks depend on repository state')
abstract class GitPrepareCommitMsgTask extends AbstractGitHookTask {

    static TaskProvider<GitPrepareCommitMsgTask> register(Project project) {
        return project.tasks.register('gitPrepareCommitMsg', GitPrepareCommitMsgTask) {
            description = 'Prepare or modify the commit message'
        }
    }

}
