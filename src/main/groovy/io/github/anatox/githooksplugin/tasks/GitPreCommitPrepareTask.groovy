// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import io.github.anatox.githooksplugin.GitHooksPlugin
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = 'Staged files detection depends on current index state')
abstract class GitPreCommitPrepareTask extends AbstractGitHookPrepareStagedFilesTask {

    static final String STASH_PREFIX = 'pre-commit'
    static final String STASH_MARKER = GitHooksPlugin.CACHE_ROOT + '/pre-commit-stashed.txt'

    @Override
    @TaskAction
    void prepare() {
        project.rootProject.file(STASH_MARKER).delete()

        def stashName = "${STASH_PREFIX}-${UUID.randomUUID()}"

        project.exec {
            it.workingDir = project.rootDir
            it.commandLine('git',
                '--config', 'core.hooksPath=/dev/null',
                'stash', 'push',
                '--keep-index',
                '--include-untracked',
                '--message', stashName,
                '--quiet')
        }.assertNormalExitValue()

        def stdout = new ByteArrayOutputStream()
        project.exec {
            it.workingDir = project.rootDir
            it.commandLine('git', 'stash', 'list',
                '--grep', stashName,
                '--format=%gd')
            it.standardOutput = stdout
        }.assertNormalExitValue()

        def stashRef = stdout.toString().trim()
        if (stashRef) {
            logger.warn("[git hooks] Unstaged files detected.")
            logger.lifecycle("[git hooks] Stashing unstaged files to ${stashRef}.")
            project.rootProject.file(STASH_MARKER).text = stashRef
        }

        super.prepare()
    }

}
