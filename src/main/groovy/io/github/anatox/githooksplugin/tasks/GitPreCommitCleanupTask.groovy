// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = 'Git hooks depend on repository state')
abstract class GitPreCommitCleanupTask extends DefaultTask {

    @TaskAction
    void cleanup() {
        def marker = project.rootProject.file(GitPreCommitPrepareTask.STASH_MARKER)
        if (!marker.exists()) return

        def stashRef = marker.text.trim()
        marker.delete()
        if (!stashRef) return

        try {
            project.exec {
                it.workingDir = project.rootDir
                it.commandLine('git',
                    '--config', 'core.hooksPath=/dev/null',
                    'stash', 'apply', stashRef,
                    '--quiet')
            }
        } catch (Exception e) {
            logger.warn("""[git hooks] Stashed changes conflicted with hook fixes.
  Resolve any conflicts, then remove stash: git stash drop ${stashRef}.""")
            return
        }
        
        logger.lifecycle("[git hooks] Restored changes from ${stashRef}.")

        try {
            project.exec {
                it.workingDir = project.rootDir
                it.commandLine('git',
                    '--config', 'core.hooksPath=/dev/null',
                    'stash', 'drop', stashRef,
                    '--quiet')
            }
        } catch (Exception e) {
            logger.lifecycle("[git hooks] Stash drop failed. Remove manually: git stash drop ${stashRef}")
        }
    }
}
