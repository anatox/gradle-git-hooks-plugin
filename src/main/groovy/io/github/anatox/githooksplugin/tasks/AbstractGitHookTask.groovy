// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = 'Git hooks depend on repository state')
abstract class AbstractGitHookTask extends DefaultTask {

    AbstractGitHookTask() {
        group = 'git hooks'
    }

}
