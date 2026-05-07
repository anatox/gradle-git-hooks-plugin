// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin

import io.github.anatox.githooksplugin.tasks.AbstractGitHookTask
import io.github.anatox.githooksplugin.tasks.GitHook
import io.github.anatox.githooksplugin.tasks.GitCommitMsgTask
import io.github.anatox.githooksplugin.tasks.GitPostCheckoutTask
import io.github.anatox.githooksplugin.tasks.GitPostCommitTask
import io.github.anatox.githooksplugin.tasks.GitPostMergeTask
import io.github.anatox.githooksplugin.tasks.GitPostRewriteTask
import io.github.anatox.githooksplugin.tasks.GitPreCommitTask
import io.github.anatox.githooksplugin.tasks.GitPreMergeCommitTask
import io.github.anatox.githooksplugin.tasks.GitPrePushTask
import io.github.anatox.githooksplugin.tasks.GitPreRebaseTask
import io.github.anatox.githooksplugin.tasks.GitPrepareCommitMsgTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.NamedDomainObjectContainer

class GitHooksPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def gitHooks = project.container(GitHookElement)
        project.extensions.add('gitHooks', gitHooks)
        gitHooks.create('pre-commit')

        GitPreCommitTask.register(project)
        GitCommitMsgTask.register(project)
        GitPrepareCommitMsgTask.register(project)
        GitPrePushTask.register(project)
        GitPreRebaseTask.register(project)
        GitPreMergeCommitTask.register(project)
        GitPostCheckoutTask.register(project)
        GitPostCommitTask.register(project)
        GitPostMergeTask.register(project)
        GitPostRewriteTask.register(project)
        registerInstallGitHooks(project, gitHooks)
    }

    private static void registerInstallGitHooks(Project project, NamedDomainObjectContainer<GitHookElement> gitHooks) {
        project.tasks.register('installGitHooks') {
            group = 'git hooks'
            description = 'Generate .githooks config-based hooks file'
            doLast {
                def githooksFile = project.rootProject.file('.githooks')
                if (githooksFile.exists()) githooksFile.delete()

                def hookCommands = project.tasks
                    .findAll { it instanceof AbstractGitHookTask }
                    .collectEntries { task ->
                        def ann = task.class.getAnnotation(GitHook)
                        ann ? [(ann.event()): ann.command()] : [:]
                    }

                def gitConfig = { String... args ->
                    project.exec {
                        it.workingDir = project.rootDir
                        it.commandLine(['git', 'config', '--file', '.githooks'] + args as List)
                    }.assertNormalExitValue()
                }

                gitHooks.each { hook ->
                    def section = hook.name
                    def enabled = hook.enable.get()
                    def command = hookCommands[section]
                    if (!command) return

                    gitConfig("hook.gradle-${section}.event", section)
                    gitConfig("hook.gradle-${section}.command", command)
                    if (!enabled) {
                        gitConfig('--bool', "hook.gradle-${section}.enabled", 'false')
                    }
                }
            }
        }
    }

}
