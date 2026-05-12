// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.work.DisableCachingByDefault

@GitHook(event = 'post-checkout', command = '''
    ./gradlew gitPostCheckout
        -Pgit.postCheckout.from="$1"
        -Pgit.postCheckout.to="$2"
        -Pgit.postCheckout.flag="$3"
    ''')
@DisableCachingByDefault(because = 'Git hooks depend on repository state')
abstract class GitPostCheckoutTask extends AbstractGitHookTask {

    static TaskProvider<GitPostCheckoutTask> register(Project project) {
        return project.tasks.register('gitPostCheckout', GitPostCheckoutTask) {
            description = 'Run post-checkout actions'
        }
    }

}
