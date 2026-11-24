package com.infinum.sentinel.ui.deeplinks

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.navigation3.ui.NavDisplay
import com.infinum.sentinel.ui.deeplinks.breakdown.LinkBreakdownEntry
import com.infinum.sentinel.ui.deeplinks.editor.VariantEditorEntry

/** Short, so a closing screen doesn't take the list's taps for long; the old activities closed without animation. */
private const val TRANSITION_DURATION_MILLIS = 150

/**
 * The Deep links tool's screens in one activity: the list, a link's breakdown and the variant editor.
 *
 * Each entry has its own view model, saved state and lifecycle, which the decorators provide. With only the list
 * left, back isn't handled here and closes the activity. Opening, closing and gesture back all use the same short
 * cross-fade.
 *
 * The back stack is saved with the generated serializer of the sealed [DeepLinksKey]. The default
 * `rememberNavBackStack` looks serializers up by reflection, which R8 can break in a minified host app.
 */
@Composable
internal fun DeepLinksNavDisplay(host: DeepLinksHostActions) {
    val backStack =
        rememberSerializable(serializer = NavBackStackSerializer(DeepLinksKey.serializer())) {
            NavBackStack<DeepLinksKey>(DeepLinksKey.Links)
        }
    val decorators =
        listOf(rememberSaveableStateHolderNavEntryDecorator<DeepLinksKey>(), rememberViewModelStoreNavEntryDecorator())
    val entries =
        entryProvider<DeepLinksKey> {
            entry<DeepLinksKey.Links> {
                DeepLinksEntry(host = host, onOpen = { key -> backStack.addAbove(top = DeepLinksKey.Links, key = key) })
            }
            entry<DeepLinksKey.Breakdown> { key ->
                LinkBreakdownEntry(link = key.link, onBack = { backStack.remove(key) }, onCopy = host.copy)
            }
            entry<DeepLinksKey.Editor> { key ->
                VariantEditorEntry(key = key, onClose = { backStack.remove(key) })
            }
        }
    NavDisplay(
        backStack = backStack,
        // A back gesture can complete after a screen already closed itself; never pop the list.
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        entryDecorators = decorators,
        transitionSpec = { crossFade() },
        popTransitionSpec = { crossFade() },
        predictivePopTransitionSpec = { _ -> crossFade() },
        entryProvider = entries,
    )
}

private fun crossFade(): ContentTransform {
    val enter = fadeIn(animationSpec = tween(TRANSITION_DURATION_MILLIS))
    val exit = fadeOut(animationSpec = tween(TRANSITION_DURATION_MILLIS))
    return enter togetherWith exit
}

/** Adds [key] only while [top] is the last key, so a double tap on the list can't open a screen twice. */
private fun MutableList<DeepLinksKey>.addAbove(
    top: DeepLinksKey,
    key: DeepLinksKey,
) {
    if (lastOrNull() == top) add(key)
}
