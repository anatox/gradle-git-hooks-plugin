// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = 'Staged files detection depends on current index state')
abstract class GitPreMergeCommitPrepareTask extends AbstractGitHookPrepareStagedFilesTask {
}
