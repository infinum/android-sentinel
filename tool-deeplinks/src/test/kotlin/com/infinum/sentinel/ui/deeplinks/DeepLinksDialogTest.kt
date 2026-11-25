package com.infinum.sentinel.ui.deeplinks

import androidx.compose.runtime.saveable.SaverScope
import com.infinum.sentinel.domain.deeplinks.models.VerificationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [DeepLinksDialog.Saver].
 */
internal class DeepLinksDialogTest {
    private val scope = SaverScope { true }

    @Test
    fun `a delete variant dialog survives saving`() {
        val dialog = DeepLinksDialog.DeleteVariant(variantId = "v1", link = "myapp://shop/orders/42?promo=SUMMER")

        assertEquals(dialog, roundTrip(dialog))
    }

    @Test
    fun `a delete all dialog survives saving`() {
        assertEquals(DeepLinksDialog.DeleteAll, roundTrip(DeepLinksDialog.DeleteAll))
    }

    @Test
    fun `a status dialog survives saving`() {
        val dialog = DeepLinksDialog.Status(VerificationStatus.NOT_VERIFIED)

        assertEquals(dialog, roundTrip(dialog))
    }

    @Test
    fun `no dialog restores as no dialog`() {
        assertNull(roundTrip(null))
    }

    @Test
    fun `a status that no longer exists restores as no dialog`() {
        assertNull(DeepLinksDialog.Saver.restore(listOf("status", "RETIRED")))
    }

    private fun roundTrip(dialog: DeepLinksDialog?): DeepLinksDialog? {
        val saved = with(DeepLinksDialog.Saver) { scope.save(dialog) } ?: return null
        return DeepLinksDialog.Saver.restore(saved)
    }
}
