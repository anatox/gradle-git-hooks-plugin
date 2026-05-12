// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.extensions

enum GitPostRewriteCommand {
    AMEND, REBASE

    static GitPostRewriteCommand fromValue(String value) {
        switch (value) {
            case 'amend':  return AMEND
            case 'rebase': return REBASE
            default: throw new IllegalArgumentException("Unknown GitPostRewriteCommand: $value")
        }
    }
}
