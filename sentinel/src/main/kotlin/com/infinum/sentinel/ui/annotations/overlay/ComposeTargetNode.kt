package com.infinum.sentinel.ui.annotations.overlay

import androidx.compose.ui.tooling.data.Group
import androidx.compose.ui.tooling.data.UiToolingDataApi

/**
 * Adapts a Compose tooling [Group] to [TargetNode].
 *
 * A group is annotatable when it has both a name and a source location — that is, it is a
 * composable the developer wrote rather than an anonymous framework group.
 */
@OptIn(UiToolingDataApi::class)
internal class ComposeTargetNode(
    val group: Group,
    override val parent: TargetNode?,
) : TargetNode {
    override val type: String = group.name.orEmpty()

    override val viewId: String? = null

    override val testTag: String? =
        group.modifierInfo
            .firstNotNullOfOrNull { info -> TEST_TAG_REGEX.find(info.modifier.toString())?.groupValues?.get(1) }

    override val sourceLocation: String? =
        ComposeSupport.formatSourceLocation(group.location?.sourceFile, group.location?.lineNumber ?: -1)

    override val text: String? = group.data.filterIsInstance<String>().firstOrNull()

    override val bounds: Bounds =
        Bounds(group.box.left, group.box.top, group.box.right, group.box.bottom)

    override val annotatable: Boolean = type.isNotEmpty() && sourceLocation != null

    override val children: List<TargetNode> =
        group.children.filter { !it.box.isEmpty() }.map { ComposeTargetNode(it, this) }

    private companion object {
        private val TEST_TAG_REGEX = Regex("""testTag=([A-Za-z0-9_\-.]+)""")

        private fun androidx.compose.ui.unit.IntRect.isEmpty(): Boolean = width <= 0 || height <= 0
    }
}
