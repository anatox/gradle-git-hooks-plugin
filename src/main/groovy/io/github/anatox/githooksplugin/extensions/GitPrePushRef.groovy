// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.extensions

import org.gradle.api.provider.Property

abstract class GitPrePushRef {

    abstract Property<String> getLocalRef()
    abstract Property<String> getLocalObjectName()
    abstract Property<String> getRemoteRef()
    abstract Property<String> getRemoteObjectName()

}
