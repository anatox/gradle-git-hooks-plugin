// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin

import io.github.anatox.githooksplugin.extensions.GitHooksContext
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

class GitHooksContextTest extends Specification {

    File testProjectDir

    def setup() {
        testProjectDir = File.createTempDir('gradle-test', '.tmp')
        testProjectDir.deleteOnExit()

        def settingsFile = new File(testProjectDir, 'settings.gradle')
        settingsFile << """
            pluginManagement {
                repositories {
                    mavenLocal()
                }
            }
        """.stripIndent()

        def buildFile = new File(testProjectDir, 'build.gradle')
        buildFile << """
            plugins {
                id 'io.github.anatox.git-hooks'
            }
        """.stripIndent()
    }

    def 'context extension is registered'() {
        given:
        new File(testProjectDir, 'build.gradle') << """
            tasks.register('verifyContext') {
                doLast {
                    assert project.extensions.findByType(${GitHooksContext.name}) != null
                }
            }
        """.stripIndent()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments('verifyContext')
            .withPluginClasspath()
            .build()

        then:
        result.output.contains('BUILD SUCCESSFUL')
    }

    def 'sub-contexts are accessible'() {
        given:
        new File(testProjectDir, 'build.gradle') << """
            tasks.register('verifySubContexts') {
                doLast {
                    def ctx = project.extensions.getByType(${GitHooksContext.name})
                    assert ctx.preCommit != null
                    assert ctx.preMergeCommit != null
                    assert ctx.prePush != null
                    assert ctx.commitMsg != null
                    assert ctx.prepareCommitMsg != null
                    assert ctx.preRebase != null
                    assert ctx.postCheckout != null
                    assert ctx.postMerge != null
                    assert ctx.postRewrite != null
                }
            }
        """.stripIndent()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments('verifySubContexts')
            .withPluginClasspath()
            .build()

        then:
        result.output.contains('BUILD SUCCESSFUL')
    }

    def 'external properties are wired to context'() {
        given:
        new File(testProjectDir, 'build.gradle') << """
            tasks.register('verifyProperties') {
                doLast {
                    def ctx = project.extensions.getByType(${GitHooksContext.name})
                    def remoteName = ctx.prePush.remoteName.get()
                    assert remoteName == 'origin'
                }
            }
        """.stripIndent()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments('verifyProperties', '-Pgit.prePush.remoteName=origin')
            .withPluginClasspath()
            .build()

        then:
        result.output.contains('BUILD SUCCESSFUL')
    }

    def cleanup() {
        testProjectDir?.deleteDir()
    }

}
