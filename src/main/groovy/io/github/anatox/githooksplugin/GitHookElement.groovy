// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin

import org.gradle.api.Named
import org.gradle.api.provider.Property

abstract class GitHookElement implements Named {

    private final String name

    @javax.inject.Inject
    GitHookElement(String name) {
        this.name = name
    }

    abstract Property<Boolean> getEnable()

    @Override
    String getName() { name }

}
