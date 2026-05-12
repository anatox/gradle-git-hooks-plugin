// SPDX-FileCopyrightText: 2026 Anatolii Sereda <3011745+anatox@users.noreply.github.com>
// SPDX-License-Identifier: Apache-2.0
package io.github.anatox.githooksplugin.extensions

import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class GitHooksContext {

    final GitPreCommitContext preCommit
    final GitPreMergeCommitContext preMergeCommit
    final GitPrePushContext prePush
    final GitCommitMsgContext commitMsg
    final GitPrepareCommitMsgContext prepareCommitMsg
    final GitPreRebaseContext preRebase
    final GitPostCheckoutContext postCheckout
    final GitPostMergeContext postMerge
    final GitPostRewriteContext postRewrite

    @Inject
    GitHooksContext(ObjectFactory objects) {
        preCommit = objects.newInstance(GitPreCommitContext)
        preMergeCommit = objects.newInstance(GitPreMergeCommitContext)
        prePush = objects.newInstance(GitPrePushContext)
        commitMsg = objects.newInstance(GitCommitMsgContext)
        prepareCommitMsg = objects.newInstance(GitPrepareCommitMsgContext)
        preRebase = objects.newInstance(GitPreRebaseContext)
        postCheckout = objects.newInstance(GitPostCheckoutContext)
        postMerge = objects.newInstance(GitPostMergeContext)
        postRewrite = objects.newInstance(GitPostRewriteContext)
    }

}
