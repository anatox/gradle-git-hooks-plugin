// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.extensions

enum GitPostCheckoutFlag {
    FILE,
    BRANCH

    static GitPostCheckoutFlag fromValue(String value) {
        switch (value) {
            case '0': return FILE
            case '1': return BRANCH
            default: throw new IllegalArgumentException("Unknown GitPostCheckoutFlag: $value")
        }
    }
}
