// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.extensions

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class GitPrePushContext {

    abstract Property<String> getRemoteName()
    abstract Property<URI> getRemoteUrl()
    abstract ListProperty<GitPrePushRef> getPushedRefs()

}
