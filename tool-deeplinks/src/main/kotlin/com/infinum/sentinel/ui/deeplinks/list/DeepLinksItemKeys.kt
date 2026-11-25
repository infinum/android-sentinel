package com.infinum.sentinel.ui.deeplinks.list

/**
 * Pure helpers for the Compose Deep links list: stable lazy keys and the rule for dividers.
 *
 * A base link is identified by its origin and key, because one key can be declared by two screens. A divider
 * sits above each group of rows: a base link with its variants, or a variant listed on its own in No longer
 * declared or Saved variants. Rows inside a group and rows right below a header get none, so a base link and
 * its variants read as one block.
 */
internal object DeepLinksItemKeys {
    /**
     * One key per item, in the same order. A key that was already produced earlier in the list gets a `#n` suffix,
     * where n counts its repeats, so the result never holds duplicates, which a LazyColumn would crash on.
     */
    fun lazyKeys(items: List<DeepLinksItem>): List<String> {
        val seen = mutableMapOf<String, Int>()
        return items.map { item ->
            val key = keyOf(item)
            val repeats = seen[key] ?: 0
            seen[key] = repeats + 1
            if (repeats == 0) key else "$key#$repeats"
        }
    }

    /**
     * True when the item at [index] starts a group, a base link or a variant listed on its own, and the item
     * above it is a link row. False for an index out of range.
     */
    fun hasDividerAbove(
        items: List<DeepLinksItem>,
        index: Int,
    ): Boolean {
        val isGroupStart =
            when (val item = items.getOrNull(index)) {
                is DeepLinksItem.BaseLinkRow -> true
                is DeepLinksItem.VariantRow -> item.baseLink == null
                else -> false
            }
        return isGroupStart && index > 0 && items.getOrNull(index - 1) is DeepLinksItem.LinkRow
    }

    /**
     * The key of [row] before duplicates get a suffix: its base link's origin and key, or its variant id. It stays
     * the same when the row's status or count changes, so it can tell which row has a menu open.
     */
    fun rowKey(row: DeepLinksItem.LinkRow): String = keyOf(row)

    private fun keyOf(item: DeepLinksItem): String =
        when (item) {
            is DeepLinksItem.Notice -> "notice:${item.notice.name}"
            is DeepLinksItem.SectionHeader -> "section:${item.titleRes}"
            is DeepLinksItem.ScreenHeader -> "screen:${item.componentName}"
            is DeepLinksItem.BaseLinkRow -> "base:${item.baseLink.origin}:${item.baseLink.key}"
            is DeepLinksItem.VariantRow -> "variant:${item.variant.id}"
        }
}
