package com.infinum.sentinel.domain.deeplinks.models

/**
 * A manifest problem that can stop a declared link from working from outside the app.
 */
internal sealed interface ConfigurationWarning {
    /** The filter lacks `android.intent.category.DEFAULT`, so implicit intents never match it. */
    data object MissingDefault : ConfigurationWarning

    /** The filter lacks `android.intent.category.BROWSABLE`, so browsers and other apps cannot open it. */
    data object MissingBrowsable : ConfigurationWarning

    /**
     * The screen is disabled, by `android:enabled="false"` in the manifest or at runtime, so nothing can open the
     * link until the app enables it.
     */
    data object Disabled : ConfigurationWarning

    /** The same link is declared by the screens named in [otherScreens] too. */
    data class DuplicateDeclaration(val otherScreens: List<String>) : ConfigurationWarning
}
