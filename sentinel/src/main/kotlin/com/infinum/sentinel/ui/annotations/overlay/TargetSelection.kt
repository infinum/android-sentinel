package com.infinum.sentinel.ui.annotations.overlay

/**
 * The rules that turn a tap into a selection. Shared by both resolvers.
 */
internal object TargetSelection {
    /**
     * The innermost node containing ([x], [y]), or `null` when the point misses [root].
     * Children are tested last-first because the last child draws on top.
     */
    fun deepestAt(root: TargetNode, x: Int, y: Int): TargetNode? {
        if (!root.bounds.contains(x, y)) return null
        for (index in root.children.indices.reversed()) {
            val hit = deepestAt(root.children[index], x, y)
            if (hit != null) return hit
        }
        return root
    }

    /** The deepest annotatable node at or above [node], or `null` when none qualifies. */
    fun nearestAnnotatable(node: TargetNode): TargetNode? {
        var current: TargetNode? = node
        while (current != null) {
            if (current.annotatable) return current
            current = current.parent
        }
        return null
    }

    /**
     * The selection broadened one annotatable node toward the root, or `null` when [node] is
     * already the outermost one. The caller then wraps back to the deepest target.
     */
    fun stepUp(node: TargetNode): TargetNode? {
        var current = node.parent
        while (current != null) {
            if (current.broadenable) return current
            current = current.parent
        }
        return null
    }

    /**
     * The chain of annotatable types from the outermost ancestor down to [node]. [node] itself is
     * always included, even when it was reached by broadening to a container that is not
     * annotatable on its own.
     */
    fun pathOf(node: TargetNode): String {
        val types = mutableListOf<String>()
        var current: TargetNode? = node
        while (current != null) {
            if (current === node || current.annotatable) types.add(current.type)
            current = current.parent
        }
        return types.reversed().joinToString(">")
    }

    /**
     * The first node, from the deepest hit at ([x], [y]) up through its ancestors, that satisfies
     * [matches]. A broadened note captured the container's centre, where a child may now be the
     * deepest hit, so re-binding has to look above the hit, not only at it.
     */
    fun ancestorAt(root: TargetNode, x: Int, y: Int, matches: (TargetNode) -> Boolean): TargetNode? {
        var current = deepestAt(root, x, y)
        while (current != null) {
            if (matches(current)) return current
            current = current.parent
        }
        return null
    }

    /**
     * The root of the tree [node] belongs to.
     *
     * [ordinalOf] locates its target by node identity, so it must count against the tree the target
     * came from. A freshly built tree holds different node objects for the same views.
     */
    fun rootOf(node: TargetNode): TargetNode {
        var current = node
        while (true) {
            current = current.parent ?: return current
        }
    }

    /**
     * Position of [node] among same-type nodes under [root], as `(ordinal, count)`, ordered top
     * to bottom then left to right. `(1, count)` when [node] cannot be located among the matches.
     */
    fun ordinalOf(root: TargetNode, node: TargetNode): Pair<Int, Int> {
        val matches = mutableListOf<TargetNode>()
        collectByType(root, node.type, matches)
        matches.sortWith(compareBy({ it.bounds.top }, { it.bounds.left }))

        val index = matches.indexOfFirst { it === node }
        if (index < 0) return 1 to matches.size.coerceAtLeast(1)
        return index + 1 to matches.size
    }

    private fun collectByType(node: TargetNode, type: String, into: MutableList<TargetNode>) {
        if (node.type == type && !node.bounds.isEmpty) into.add(node)
        node.children.forEach { collectByType(it, type, into) }
    }
}
