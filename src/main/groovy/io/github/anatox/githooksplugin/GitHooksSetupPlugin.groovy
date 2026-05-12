// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin

import io.github.anatox.githooksplugin.util.PluginProperties
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.slf4j.LoggerFactory

import java.security.MessageDigest

class GitHooksSetupPlugin implements Plugin<Settings> {

    private static final logger = LoggerFactory.getLogger(GitHooksSetupPlugin)

    static final PluginProperties PLUGIN_PROPS = new PluginProperties(GitHooksSetupPlugin)
    static final String PLUGIN_ID = PLUGIN_PROPS.getProperty('git-hooks-setup.plugin.id')
    static final String PLUGIN_VERSION = PLUGIN_PROPS.getProperty('git-hooks-setup.plugin.version')
    static final String MARKER_PATH = PLUGIN_PROPS.getProperty('git-hooks-setup.markerPath')

    @Override
    void apply(Settings settings) {
        if (System.getenv('CI') && !settings.startParameter.projectProperties.containsKey('setup')) {
            logger.lifecycle('[setup] CI detected, skipping setup. Use -Psetup to force.')
            return
        }

        def rootDir = settings.rootDir

        if (isUpToDate(rootDir)) {
            logger.lifecycle('[setup] Already up-to-date, skipping.')
            return
        }

        logger.lifecycle('[setup] Running repo setup...')
        gitConfig(settings, rootDir, '.gitconfig')
        gitConfig(settings, rootDir, '.githooks')
        gitLfs(settings, rootDir)
        gitSubmodules(settings, rootDir)

        writeMarker(rootDir)
        logger.lifecycle('[setup] Done.')
    }

    private static boolean isUpToDate(File rootDir) {
        def markerFile = new File(rootDir, MARKER_PATH)
        if (!markerFile.exists()) return false

        def props = new Properties()
        markerFile.withInputStream { props.load(it) }

        return props.getProperty('version') == PLUGIN_VERSION &&
               props.getProperty('hash') == computeHash(rootDir)
    }

    private static void writeMarker(File rootDir) {
        def markerFile = new File(rootDir, MARKER_PATH)
        markerFile.parentFile.mkdirs()

        def props = new Properties()
        props.setProperty('version', PLUGIN_VERSION)
        props.setProperty('hash', computeHash(rootDir))
        markerFile.withOutputStream { props.store(it, 'Setup plugin cache') }
    }

    private static String computeHash(File rootDir) {
        def digest = MessageDigest.getInstance('SHA-256')
        ['.gitconfig', '.githooks'].each { name ->
            def f = new File(rootDir, name)
            digest.update((byte)(f.exists() ? 1 : 0))
        }
        def gitmodules = new File(rootDir, '.gitmodules')
        if (gitmodules.exists()) {
            digest.update(gitmodules.bytes)
        } else {
            digest.update((byte) 0)
        }
        return digest.digest().encodeHex().toString()
    }

    private static void exec(Settings settings, File workingDir, String... command) {
        def result = settings.providers.exec {
            it.workingDir = workingDir
            it.commandLine(command as List<String>)
            it.ignoreExitValue = true
        }.result.get()
        if (result.exitValue != 0) {
            throw new RuntimeException("Command failed (exit ${result.exitValue}): ${command.join(' ')}")
        }
    }

    private static void gitConfig(Settings settings, File rootDir, String fileName) {
        logger.lifecycle("[setup] Configure git options: ${fileName}...")
        def configFile = new File(rootDir, fileName)
        if (configFile.exists()) {
            exec(settings, rootDir, 'git', 'config', '--local', 'include.path', "../${fileName}")
        }
    }

    private static void gitLfs(Settings settings, File rootDir) {
        logger.lifecycle('[setup] Configure git LFS...')
        exec(settings, rootDir, 'git', 'lfs', 'install', '--force')
        exec(settings, rootDir, 'git', 'lfs', 'pull')
    }

    private static void gitSubmodules(Settings settings, File rootDir) {
        logger.lifecycle('[setup] Configure submodules...')
        exec(settings, rootDir, 'git', 'submodule', 'update', '--init')
        exec(settings, rootDir, 'git', 'submodule', 'foreach', '[ -f ./gradlew ] && ./gradlew -Psetup help > /dev/null || true')
    }

}
