// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

@GitHook(event = 'pre-merge-commit', command = './gradlew gitPreMergeCommit')
abstract class GitPreMergeCommitTask extends AbstractGitHookTask {

    static TaskProvider<GitPreMergeCommitTask> register(Project project) {
        return project.tasks.register('gitPreMergeCommit', GitPreMergeCommitTask) {
            description = 'Run pre-merge-commit checks'
            doFirst {
                def proc = 'git diff --cached --name-only'.execute([], project.rootDir)
                proc.waitFor()
                files.set(proc.text.trim().readLines().findAll { it })
            }
        }
    }

}
