// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.extensions

enum GitPostMergeFlag {
    DEFAULT,
    SQUASH_MERGE

    static GitPostMergeFlag fromValue(String value) {
        switch (value) {
            case '0': return DEFAULT
            case '1': return SQUASH_MERGE
            default: throw new IllegalArgumentException("Unknown GitPostMergeFlag: $value")
        }
    }
}
