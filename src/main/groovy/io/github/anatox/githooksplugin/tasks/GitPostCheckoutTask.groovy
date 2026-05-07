// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider

@GitHook(event = 'post-checkout', command = "sh -c './gradlew gitPostCheckout -Pgit.fromRef=\"\$1\" -Pgit.toRef=\"\$2\" -Pgit.checkoutType=\"\$3\"' sh")
abstract class GitPostCheckoutTask extends AbstractGitHookTask {

    abstract Property<String> getFromRef()
    abstract Property<String> getToRef()
    abstract Property<String> getCheckoutType()

    static TaskProvider<GitPostCheckoutTask> register(Project project) {
        return project.tasks.register('gitPostCheckout', GitPostCheckoutTask) {
            description = 'Run post-checkout actions'
            doFirst {
                if (!fromRef.isPresent()) {
                    fromRef.set(project.findProperty('git.fromRef') as String ?: '')
                }
                if (!toRef.isPresent()) {
                    toRef.set(project.findProperty('git.toRef') as String ?: '')
                }
                if (!checkoutType.isPresent()) {
                    checkoutType.set(project.findProperty('git.checkoutType') as String ?: '')
                }
            }
        }
    }

}
