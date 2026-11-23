package com.infinum.sentinel.ui.deeplinks

/**
 * One-off events of the Deep links screen.
 */
internal sealed class DeepLinksEvent {
    /** A variant change could not be saved. */
    data object ChangeFailed : DeepLinksEvent()
}
