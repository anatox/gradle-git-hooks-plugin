// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.util

class PluginProperties extends Properties {

    private static final String PROPERTIES_FILE = 'plugin.properties'

    PluginProperties(Class<?> caller) {
        def path = caller.package.name.replace('.', '/') + '/' + PROPERTIES_FILE
        def resource = PluginProperties.classLoader.getResourceAsStream(path)
        if (!resource) {
            throw new IllegalStateException("Required plugin resource not found on classpath: ${path}")
        }
        try { load(resource) } finally { resource.close() }
    }

    @Override
    String getProperty(String key) {
        def value = super.getProperty(key)
        if (value == null) {
            throw new IllegalStateException("Required property not found in plugin.properties: ${key}")
        }
        return value
    }

}
