package com.infinum.sentinel.ui.deeplinks.launch

import com.infinum.sentinel.domain.deeplinks.models.ConfigurationWarning
import com.infinum.sentinel.domain.deeplinks.models.LaunchMode
import com.infinum.sentinel.domain.deeplinks.models.LinkType

/**
 * How a Launch starts: with the launch mode menu, or straight away in one mode.
 */
internal sealed interface LaunchChoice {
    /** Show the Real tap and Open in this app menu. */
    data object Ask : LaunchChoice

    /** Launch in [mode] without asking. */
    data class Direct(
        val mode: LaunchMode,
    ) : LaunchChoice

    companion object {
        /**
         * The choice for [link], given the [warnings] of its base link (empty for an example link or a variant
         * whose base link is gone).
         *
         * A declaration without BROWSABLE but with DEFAULT, on an enabled screen, can't take a Real tap, which is
         * how browsers and other apps open links, but it can take Open in this app, so such links open in this app
         * straight away; they are often meant for in-app navigation only. Other web links ask, because App Links
         * verification decides where a Real tap opens them. Any other link opens with Real tap, the way a user's tap
         * would. If another app or a saved "Always" choice takes it, the launch counts as successful and Open in this
         * app isn't offered: an accepted trade-off.
         */
        fun forLink(
            link: String,
            warnings: Set<ConfigurationWarning>,
        ): LaunchChoice {
            val canOpenInApp = ConfigurationWarning.MissingDefault !in warnings && ConfigurationWarning.Disabled !in warnings
            val isInAppOnly = ConfigurationWarning.MissingBrowsable in warnings && canOpenInApp
            val isWeb = LinkType.from(link.substringBefore(':', missingDelimiterValue = "")) == LinkType.WEB
            return when {
                isInAppOnly -> Direct(LaunchMode.OPEN_IN_APP)
                isWeb -> Ask
                else -> Direct(LaunchMode.REAL_TAP)
            }
        }
    }
}
