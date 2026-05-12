// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = 'Staged files detection depends on current index state')
abstract class AbstractGitHookPrepareStagedFilesTask extends DefaultTask {

    @OutputFile
    abstract RegularFileProperty getOutputFile()

    @Internal
    protected String getDiffFilter() { 'ACMR' }

    @TaskAction
    void prepare() {
        def outFile = outputFile.get().asFile
        outFile.delete()
        outFile.parentFile.mkdirs()

        def stdout = new ByteArrayOutputStream()
        project.exec {
            it.workingDir = project.rootDir
            it.commandLine('git', 'diff', '--cached', '--name-only', "--diff-filter=${diffFilter}")
            it.standardOutput = stdout
        }.assertNormalExitValue()

        outFile.text = stdout.toString().trim()
    }

}
