// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import io.github.anatox.githooksplugin.GitHookElement
import io.github.anatox.githooksplugin.GitHooksPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = 'Githooks file generation depends on registered tasks')
abstract class GitHooksInstallTask extends DefaultTask {

    @TaskAction
    void install() {
        def githooksFile = project.rootProject.file('.githooks')
        if (githooksFile.exists()) githooksFile.delete()

        def hookCommands = project.tasks
            .findAll { it instanceof AbstractGitHookTask }
            .collectEntries { task ->
                def annotation = task.class.getAnnotation(GitHook)
                if (!annotation) return [:]
                def command = annotation.command()
                command = command.readLines()*.trim().findAll { it }.join(' ')
                if (command.contains('${')) {
                    def binding = [cacheRoot: GitHooksPlugin.PLUGIN_PROPS.getProperty('git-hooks.cache.root')]
                    command = new groovy.text.SimpleTemplateEngine().createTemplate(command).make(binding).toString()
                }
                if (command =~ /\$\d+/) {
                    command = "sh -c '${command.replaceAll(/'/, "'\\\\''")}' sh"
                }
                [(annotation.event()): command]
            }

        def gitConfig = { String... args ->
            project.exec {
                it.workingDir = project.rootDir
                it.commandLine(['git', 'config', '--file', '.githooks'] + args as List)
            }.assertNormalExitValue()
        }

        def gitHooks = project.extensions.getByName('gitHooks') as NamedDomainObjectContainer<GitHookElement>

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