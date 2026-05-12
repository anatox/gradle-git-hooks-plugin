// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin

import io.github.anatox.githooksplugin.extensions.*
import io.github.anatox.githooksplugin.tasks.*
import io.github.anatox.githooksplugin.util.PluginProperties
import org.gradle.api.Plugin
import org.gradle.api.Project

class GitHooksPlugin implements Plugin<Project> {

    static final PluginProperties PLUGIN_PROPS = new PluginProperties(GitHooksPlugin)
    static final String PLUGIN_ID = PLUGIN_PROPS.getProperty('git-hooks.plugin.id')
    static final String PLUGIN_VERSION = PLUGIN_PROPS.getProperty('git-hooks.plugin.version')
    static final String CACHE_ROOT = PLUGIN_PROPS.getProperty('git-hooks.cache.root')

    @Override
    void apply(Project project) {
        def gitHooks = project.container(GitHookElement)
        project.extensions.add('gitHooks', gitHooks)
        gitHooks.create('pre-commit')

        registerContext(project)

        registerPrepareTasks(project)
        registerHookTasks(project)
        registerInstallGitHooks(project)
    }

    private static void registerContext(Project project) {
        def context = project.extensions.create('gitHooksContext', GitHooksContext)

        context.prePush.remoteName.convention(project.providers.gradleProperty('git.prePush.remoteName').orElse(''))
        context.prePush.remoteUrl.convention(
            project.providers.gradleProperty('git.prePush.remoteUrl')
                .orElse('')
                .map { value ->
                    if (!value) return null
                    try {
                        new URI(value)
                    } catch (URISyntaxException e) {
                        throw new org.gradle.api.GradleException(
                            "Invalid git.prePush.remoteUrl: '${value}' — ${e.message}", e)
                    }
                }
        )

        context.prePush.pushedRefs.convention(project.providers.provider {
            def stdinFile = project.rootProject.file("${CACHE_ROOT}/pre-push-stdin.txt")
            if (!stdinFile.exists()) return [] as List<GitPrePushRef>
            def lines = stdinFile.text.trim().readLines().findAll { it }
            lines.collect { line ->
                def parts = line.split(' ', 4)
                if (parts.length != 4) return null
                def ref = project.objects.newInstance(GitPrePushRef)
                ref.localRef.set(parts[0])
                ref.localObjectName.set(parts[1])
                ref.remoteRef.set(parts[2])
                ref.remoteObjectName.set(parts[3])
                ref
            }.findAll { it }
        })

        def commitMsgFileProvider = project.providers.gradleProperty('git.commitMsg.messageFile')
            .map { project.rootProject.file(it) }
        context.commitMsg.messageFile.set(commitMsgFileProvider)

        def prepareMsgFileProvider = project.providers.gradleProperty('git.prepareCommitMsg.messageFile')
            .map { project.rootProject.file(it) }
        context.prepareCommitMsg.messageFile.set(prepareMsgFileProvider)

        context.prepareCommitMsg.messageSource.convention(
            project.providers.gradleProperty('git.prepareCommitMsg.messageSource')
                .orElse('message')
                .map { GitPrepareMessageSource.fromValue(it) }
        )
        context.prepareCommitMsg.objectName.convention(
            project.providers.gradleProperty('git.prepareCommitMsg.objectName').orElse('')
        )

        context.preRebase.upstream.convention(
            project.providers.gradleProperty('git.preRebase.upstream').orElse('')
        )
        context.preRebase.branch.convention(
            project.providers.gradleProperty('git.preRebase.branch').orElse('')
        )

        context.postCheckout.from.convention(
            project.providers.gradleProperty('git.postCheckout.from').orElse('')
        )
        context.postCheckout.to.convention(
            project.providers.gradleProperty('git.postCheckout.to').orElse('')
        )
        context.postCheckout.flag.convention(
            project.providers.gradleProperty('git.postCheckout.flag')
                .orElse('1')
                .map { GitPostCheckoutFlag.fromValue(it) }
        )

        context.postMerge.isSquashMerge.convention(
            project.providers.gradleProperty('git.postMerge.isSquashMerge')
                .orElse('0')
                .map { GitPostMergeFlag.fromValue(it) }
        )

        context.postRewrite.command.convention(
            project.providers.gradleProperty('git.postRewrite.command')
                .orElse('amend')
                .map { GitPostRewriteCommand.fromValue(it) }
        )

        context.preCommit.stagedFiles.setFrom(project.providers.provider {
            def manifest = project.rootProject.file("${CACHE_ROOT}/pre-commit-staged.txt")
            if (!manifest.exists()) return [] as File[]
            manifest.text.trim().readLines().findAll { it }
                .collect { path -> project.rootProject.file(path) } as File[]
        })

        context.preMergeCommit.stagedFiles.setFrom(project.providers.provider {
            def manifest = project.rootProject.file("${CACHE_ROOT}/pre-merge-commit-staged.txt")
            if (!manifest.exists()) return [] as File[]
            manifest.text.trim().readLines().findAll { it }
                .collect { path -> project.rootProject.file(path) } as File[]
        })
    }

    private static void registerPrepareTasks(Project project) {
        project.tasks.register('gitPreCommitPrepare', GitPreCommitPrepareTask) {
            description = 'Snapshot staged files (with stash) for pre-commit checks'
            outputFile.set(project.rootProject.file("${CACHE_ROOT}/pre-commit-staged.txt"))
            finalizedBy project.tasks.named('gitPreCommitCleanup')
        }
        project.tasks.register('gitPreMergeCommitPrepare', GitPreMergeCommitPrepareTask) {
            description = 'Snapshot staged files for pre-merge-commit checks'
            outputFile.set(project.rootProject.file("${CACHE_ROOT}/pre-merge-commit-staged.txt"))
        }
    }

    private static void registerHookTasks(Project project) {
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
    }

    private static void registerInstallGitHooks(Project project) {
        project.tasks.register('installGitHooks', GitHooksInstallTask) {
            group = 'git hooks'
            description = 'Generate .githooks config-based hooks file'
        }
    }

}
