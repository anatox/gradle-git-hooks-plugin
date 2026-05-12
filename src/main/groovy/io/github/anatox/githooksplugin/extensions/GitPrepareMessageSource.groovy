// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.extensions

enum GitPrepareMessageSource {
    MESSAGE, TEMPLATE, MERGE, SQUASH, COMMIT

    static GitPrepareMessageSource fromValue(String value) {
        switch (value) {
            case 'message':  return MESSAGE
            case 'template': return TEMPLATE
            case 'merge':    return MERGE
            case 'squash':   return SQUASH
            case 'commit':   return COMMIT
            default: throw new IllegalArgumentException("Unknown GitPrepareMessageSource: $value")
        }
    }
}
