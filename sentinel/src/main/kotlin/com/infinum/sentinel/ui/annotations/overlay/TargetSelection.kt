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
     * already the outermost one — the caller then wraps back to the deepest target.
     */
    fun stepUp(node: TargetNode): TargetNode? {
        var current = node.parent
        while (current != null) {
            if (current.broadenable) return current
            current = current.parent
        }
        return null
    }

    /** The chain of annotatable types from the outermost ancestor down to [node]. */
    fun pathOf(node: TargetNode): String {
        val types = mutableListOf<String>()
        var current: TargetNode? = node
        while (current != null) {
            if (current.annotatable) types.add(current.type)
            current = current.parent
        }
        return types.reversed().joinToString(">")
    }

    /**
     * Position of [node] among same-type nodes under [root], as `(ordinal, count)`, ordered top
     * to bottom then left to right. `(1, count)` when [node] cannot be located among the matches.
     */
    /**
     * The root of the tree [node] belongs to.
     *
     * [ordinalOf] locates its target by node identity, so it must be counted against the very tree
     * the target came from — a freshly built one holds different node objects for the same views
     * and the target would never be found in it.
     */
    fun rootOf(node: TargetNode): TargetNode {
        var current = node
        while (true) {
            current = current.parent ?: return current
        }
    }

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
