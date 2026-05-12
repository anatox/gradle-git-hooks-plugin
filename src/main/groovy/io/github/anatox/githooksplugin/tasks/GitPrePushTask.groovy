// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import io.github.anatox.githooksplugin.GitHooksPlugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.work.DisableCachingByDefault

@GitHook(event = 'pre-push', command = '''
    mkdir -p ${cacheRoot};
    cat > ${cacheRoot}/pre-push-stdin.txt;
    exec ./gradlew gitPrePush
        -Pgit.prePush.remoteName="$1"
        -Pgit.prePush.remoteUrl="$2"
    ''')
@DisableCachingByDefault(because = 'Git hooks depend on repository state')
abstract class GitPrePushTask extends AbstractGitHookTask {

    static TaskProvider<GitPrePushTask> register(Project project) {
        return project.tasks.register('gitPrePush', GitPrePushTask) {
            description = 'Run pre-push checks'
            doLast {
                def stdinFile = project.rootProject.file("${GitHooksPlugin.CACHE_ROOT}/pre-push-stdin.txt")
                stdinFile.delete()
            }
        }
    }

}
