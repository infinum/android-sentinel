package com.infinum.sentinel.domain.deeplinks.overview

import com.infinum.sentinel.data.models.raw.deeplinks.DomainVerificationData
import com.infinum.sentinel.domain.deeplinks.models.BaseLink
import com.infinum.sentinel.domain.deeplinks.models.BaseLinkOrigin
import com.infinum.sentinel.domain.deeplinks.models.DeclaredScreen
import com.infinum.sentinel.domain.deeplinks.models.DeepLinkUri
import com.infinum.sentinel.domain.deeplinks.models.DeepLinksDiscovery
import com.infinum.sentinel.domain.deeplinks.models.HostSpec
import com.infinum.sentinel.domain.deeplinks.models.LinkSpec
import com.infinum.sentinel.domain.deeplinks.models.PathSpec
import com.infinum.sentinel.domain.deeplinks.models.Variant
import com.infinum.sentinel.domain.deeplinks.models.VerificationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [DeepLinksOverviewAssembler].
 */
internal class DeepLinksOverviewAssemblerTest {
    private val mainScreen = "com.example.MainActivity"
    private val promoScreen = "com.example.PromoActivity"
    private val saleScreen = "com.example.SaleActivity"
    private val removedScreen = "com.example.RemovedActivity"

    private val ordersKey = "declared|myapp|lit:shop||lit:/orders"
    private val cartKey = "declared|myapp|lit:shop||lit:/cart"
    private val goneKey = "declared|myapp|lit:shop||lit:/gone"
    private val exampleOrderKey = "example|myapp://shop/orders/42"
    private val exampleCartKey = "example|myapp://shop/cart"
    private val oldExampleKey = "example|myapp://shop/old"
    private val exampleWebKey = "example|https://example.com/offers"

    private val exampleOrder =
        BaseLink(exampleOrderKey, BaseLinkOrigin.Example("Order", "myapp://shop/orders/42"), null, null)
    private val exampleCart = BaseLink(exampleCartKey, BaseLinkOrigin.Example(null, "myapp://shop/cart"), null, null)

    private val uri = DeepLinkUri("myapp", true, "shop", null, "/orders", listOf(), null)

    private fun declared(
        componentName: String,
        key: String,
    ): BaseLink = BaseLink(key, BaseLinkOrigin.Declared(componentName), null, null)

    private fun screen(
        componentName: String,
        title: String,
        vararg links: BaseLink,
    ): DeclaredScreen = DeclaredScreen(componentName, title, links.toList())

    private fun variant(
        id: String,
        baseKey: String,
        componentName: String?,
    ): Variant = Variant(id, null, baseKey, componentName, uri)

    private fun discovery(
        examples: List<BaseLink> = listOf(),
        screens: List<DeclaredScreen> = listOf(),
        isFailed: Boolean = false,
    ): DeepLinksDiscovery = DeepLinksDiscovery(examples, screens, isFailed)

    private fun entry(
        link: BaseLink,
        vararg variants: Variant,
    ): BaseLinkEntry = BaseLinkEntry(link, variants.map { VariantEntry(it) })

    private fun entries(vararg variants: Variant): List<VariantEntry> = variants.map { VariantEntry(it) }

    @Test
    fun `sections follow examples then screens in manifest order then no longer declared`() {
        val v1 = variant("v1", goneKey, mainScreen)

        val overview =
            DeepLinksOverviewAssembler.assemble(
                discovery(
                    examples = listOf(exampleOrder),
                    screens =
                    listOf(
                        screen(promoScreen, "Promo", declared(promoScreen, cartKey)),
                        screen(mainScreen, "Main", declared(mainScreen, ordersKey)),
                    ),
                ),
                listOf(v1),
            )

        assertEquals(
            DeepLinksOverview(
                notices = listOf(),
                sections =
                listOf(
                    DeepLinksSection.Examples(listOf(entry(exampleOrder))),
                    DeepLinksSection.Screen(promoScreen, "Promo", listOf(entry(declared(promoScreen, cartKey)))),
                    DeepLinksSection.Screen(mainScreen, "Main", listOf(entry(declared(mainScreen, ordersKey)))),
                    DeepLinksSection.NoLongerDeclared(entries(v1)),
                ),
                isEmpty = false,
            ),
            overview,
        )
    }

    @Test
    fun `examples are left out when there are no example links`() {
        val overview =
            DeepLinksOverviewAssembler.assemble(
                discovery(screens = listOf(screen(mainScreen, "Main", declared(mainScreen, ordersKey)))),
                listOf(),
            )

        assertEquals(
            DeepLinksOverview(
                notices = listOf(),
                sections = listOf(DeepLinksSection.Screen(mainScreen, "Main", listOf(entry(declared(mainScreen, ordersKey))))),
                isEmpty = false,
            ),
            overview,
        )
    }

    @Test
    fun `a variant attaches to the link with its key in the screen it was saved from`() {
        val v1 = variant("v1", cartKey, mainScreen)

        val overview =
            DeepLinksOverviewAssembler.assemble(
                discovery(
                    screens = listOf(screen(mainScreen, "Main", declared(mainScreen, ordersKey), declared(mainScreen, cartKey))),
                ),
                listOf(v1),
            )

        assertEquals(
            DeepLinksOverview(
                notices = listOf(),
                sections =
                listOf(
                    DeepLinksSection.Screen(
                        mainScreen,
                        "Main",
                        listOf(entry(declared(mainScreen, ordersKey)), entry(declared(mainScreen, cartKey), v1)),
                    ),
                ),
                isEmpty = false,
            ),
            overview,
        )
    }

    @Test
    fun `a variant whose screen no longer declares its link attaches to the first screen that does`() {
        val v1 = variant("v1", ordersKey, mainScreen)
        val v2 = variant("v2", ordersKey, removedScreen)

        val overview =
            DeepLinksOverviewAssembler.assemble(
                discovery(
                    screens =
                    listOf(
                        screen(mainScreen, "Main", declared(mainScreen, cartKey)),
                        screen(promoScreen, "Promo", declared(promoScreen, ordersKey)),
                        screen(saleScreen, "Sale", declared(saleScreen, ordersKey)),
                    ),
                ),
                listOf(v1, v2),
            )

        assertEquals(
            DeepLinksOverview(
                notices = listOf(),
                sections =
                listOf(
                    DeepLinksSection.Screen(mainScreen, "Main", listOf(entry(declared(mainScreen, cartKey)))),
                    DeepLinksSection.Screen(promoScreen, "Promo", listOf(entry(declared(promoScreen, ordersKey), v1, v2))),
                    DeepLinksSection.Screen(saleScreen, "Sale", listOf(entry(declared(saleScreen, ordersKey)))),
                ),
                isEmpty = false,
            ),
            overview,
        )
    }

    @Test
    fun `an example variant attaches to the example with its key`() {
        val v1 = variant("v1", exampleCartKey, null)

        val overview = DeepLinksOverviewAssembler.assemble(discovery(examples = listOf(exampleOrder, exampleCart)), listOf(v1))

        assertEquals(
            DeepLinksOverview(
                notices = listOf(),
                sections = listOf(DeepLinksSection.Examples(listOf(entry(exampleOrder), entry(exampleCart, v1)))),
                isEmpty = false,
            ),
            overview,
        )
    }

    @Test
    fun `variants whose link is gone are no longer declared in saved order`() {
        val v1 = variant("v1", goneKey, mainScreen)
        val v2 = variant("v2", oldExampleKey, null)
        val v3 = variant("v3", ordersKey, mainScreen)

        val overview =
            DeepLinksOverviewAssembler.assemble(
                discovery(screens = listOf(screen(mainScreen, "Main", declared(mainScreen, ordersKey)))),
                listOf(v1, v2, v3),
            )

        assertEquals(
            DeepLinksOverview(
                notices = listOf(),
                sections =
                listOf(
                    DeepLinksSection.Screen(mainScreen, "Main", listOf(entry(declared(mainScreen, ordersKey), v3))),
                    DeepLinksSection.NoLongerDeclared(entries(v1, v2)),
                ),
                isEmpty = false,
            ),
            overview,
        )
    }

    @Test
    fun `a link declared in two screens keeps each variant under the screen it was saved from`() {
        val v1 = variant("v1", ordersKey, promoScreen)
        val v2 = variant("v2", ordersKey, mainScreen)

        val overview =
            DeepLinksOverviewAssembler.assemble(
                discovery(
                    screens =
                    listOf(
                        screen(mainScreen, "Main", declared(mainScreen, ordersKey)),
                        screen(promoScreen, "Promo", declared(promoScreen, ordersKey)),
                    ),
                ),
                listOf(v1, v2),
            )

        assertEquals(
            DeepLinksOverview(
                notices = listOf(),
                sections =
                listOf(
                    DeepLinksSection.Screen(mainScreen, "Main", listOf(entry(declared(mainScreen, ordersKey), v2))),
                    DeepLinksSection.Screen(promoScreen, "Promo", listOf(entry(declared(promoScreen, ordersKey), v1))),
                ),
                isEmpty = false,
            ),
            overview,
        )
    }

    @Test
    fun `variants follow their base link in saved order and the count matches`() {
        val v1 = variant("v1", ordersKey, mainScreen)
        val v2 = variant("v2", exampleOrderKey, null)
        val v3 = variant("v3", cartKey, mainScreen)
        val v4 = variant("v4", ordersKey, mainScreen)

        val overview =
            DeepLinksOverviewAssembler.assemble(
                discovery(
                    examples = listOf(exampleOrder),
                    screens = listOf(screen(mainScreen, "Main", declared(mainScreen, ordersKey), declared(mainScreen, cartKey))),
                ),
                listOf(v1, v2, v3, v4),
            )

        assertEquals(
            DeepLinksOverview(
                notices = listOf(),
                sections =
                listOf(
                    DeepLinksSection.Examples(listOf(entry(exampleOrder, v2))),
                    DeepLinksSection.Screen(
                        mainScreen,
                        "Main",
                        listOf(entry(declared(mainScreen, ordersKey), v1, v4), entry(declared(mainScreen, cartKey), v3)),
                    ),
                ),
                isEmpty = false,
            ),
            overview,
        )
        assertEquals(listOf(1), (overview.sections[0] as DeepLinksSection.Examples).entries.map { it.variantCount })
        assertEquals(listOf(2, 1), (overview.sections[1] as DeepLinksSection.Screen).entries.map { it.variantCount })
    }

    @Test
    fun `failed discovery shows the notice and lists declared variants as saved variants while example variants attach`() {
        val v1 = variant("v1", ordersKey, mainScreen)
        val v2 = variant("v2", exampleOrderKey, null)
        val v3 = variant("v3", cartKey, promoScreen)

        val overview =
            DeepLinksOverviewAssembler.assemble(
                discovery(examples = listOf(exampleOrder), isFailed = true),
                listOf(v1, v2, v3),
            )

        assertEquals(
            DeepLinksOverview(
                notices = listOf(DeepLinksNotice.DISCOVERY_FAILED),
                sections =
                listOf(
                    DeepLinksSection.Examples(listOf(entry(exampleOrder, v2))),
                    DeepLinksSection.SavedVariants(entries(v1, v3)),
                ),
                isEmpty = false,
            ),
            overview,
        )
    }

    @Test
    fun `failed discovery lists the variant of a gone example as no longer declared before saved variants`() {
        val v1 = variant("v1", oldExampleKey, null)
        val v2 = variant("v2", ordersKey, mainScreen)

        val overview =
            DeepLinksOverviewAssembler.assemble(
                discovery(examples = listOf(exampleOrder), isFailed = true),
                listOf(v1, v2),
            )

        assertEquals(
            DeepLinksOverview(
                notices = listOf(DeepLinksNotice.DISCOVERY_FAILED),
                sections =
                listOf(
                    DeepLinksSection.Examples(listOf(entry(exampleOrder))),
                    DeepLinksSection.NoLongerDeclared(entries(v1)),
                    DeepLinksSection.SavedVariants(entries(v2)),
                ),
                isEmpty = false,
            ),
            overview,
        )
    }

    @Test
    fun `the overview is empty when nothing is declared given or saved`() {
        assertEquals(
            DeepLinksOverview(notices = listOf(), sections = listOf(), isEmpty = true),
            DeepLinksOverviewAssembler.assemble(discovery(), listOf()),
        )
    }

    @Test
    fun `a saved variant alone makes the overview non-empty`() {
        val v1 = variant("v1", goneKey, mainScreen)

        assertEquals(
            DeepLinksOverview(
                notices = listOf(),
                sections = listOf(DeepLinksSection.NoLongerDeclared(entries(v1))),
                isEmpty = false,
            ),
            DeepLinksOverviewAssembler.assemble(discovery(), listOf(v1)),
        )
    }

    @Test
    fun `failed discovery with nothing to list is not empty and shows the notice`() {
        assertEquals(
            DeepLinksOverview(notices = listOf(DeepLinksNotice.DISCOVERY_FAILED), sections = listOf(), isEmpty = false),
            DeepLinksOverviewAssembler.assemble(discovery(isFailed = true), listOf()),
        )
    }

    private fun verification(
        isAllowed: Boolean = true,
        vararg hostStates: Pair<String, Int>,
    ): DomainVerificationData = DomainVerificationData(isLinkHandlingAllowed = isAllowed, hostStates = mapOf(*hostStates))

    private fun webVariant(
        id: String,
        baseKey: String,
        componentName: String?,
        host: String,
    ): Variant = Variant(id, null, baseKey, componentName, DeepLinkUri("https", true, host, null, "/orders/1", listOf(), null))

    @Test
    fun `link handling off comes first and is shown only when it is off`() {
        val off =
            DeepLinksOverviewAssembler.assemble(discovery(isFailed = true), listOf(), verification(isAllowed = false))
        val on =
            DeepLinksOverviewAssembler.assemble(discovery(isFailed = true), listOf(), verification(isAllowed = true))

        assertEquals(listOf(DeepLinksNotice.LINK_HANDLING_OFF, DeepLinksNotice.DISCOVERY_FAILED), off.notices)
        assertEquals(listOf(DeepLinksNotice.DISCOVERY_FAILED), on.notices)
    }

    @Test
    fun `a wildcard host link and its variant take the status of the wildcard key`() {
        val key = "declared|https|wild:.example.com||pattern:/orders/*"
        val link =
            BaseLink(
                key,
                BaseLinkOrigin.Declared(mainScreen),
                LinkSpec("https", HostSpec.Wildcard(".example.com"), null, PathSpec.Pattern("/orders/*")),
                null,
            )
        val v1 = webVariant("v1", key, mainScreen, "shop.example.com")

        val overview =
            DeepLinksOverviewAssembler.assemble(
                discovery(screens = listOf(screen(mainScreen, "Main", link))),
                listOf(v1),
                verification(true, "*.example.com" to 2),
            )

        assertEquals(
            listOf(
                DeepLinksSection.Screen(
                    mainScreen,
                    "Main",
                    listOf(
                        BaseLinkEntry(link, listOf(VariantEntry(v1, VerificationStatus.VERIFIED)), VerificationStatus.VERIFIED),
                    ),
                ),
            ),
            overview.sections,
        )
    }

    @Test
    fun `with link handling off verified and allowed hosts are off in settings and the others keep their status`() {
        fun webLink(host: String) =
            BaseLink(
                "declared|https|lit:$host||lit:/offers",
                BaseLinkOrigin.Declared(mainScreen),
                LinkSpec("https", HostSpec.Literal(host), null, PathSpec.Literal("/offers")),
                null,
            )
        val verified = webLink("verified.example.com")
        val allowed = webLink("allowed.example.com")
        val unverified = webLink("unverified.example.com")
        val v1 = webVariant("v1", verified.key, mainScreen, "verified.example.com")
        val v2 = webVariant("v2", verified.key, mainScreen, "other.example.org")

        val overview =
            DeepLinksOverviewAssembler.assemble(
                discovery(screens = listOf(screen(mainScreen, "Main", verified, allowed, unverified))),
                listOf(v1, v2),
                verification(false, "verified.example.com" to 2, "allowed.example.com" to 1, "unverified.example.com" to 0),
            )

        assertEquals(
            listOf(
                DeepLinksSection.Screen(
                    mainScreen,
                    "Main",
                    listOf(
                        BaseLinkEntry(
                            verified,
                            listOf(
                                VariantEntry(v1, VerificationStatus.VERIFIED_HANDLING_OFF),
                                VariantEntry(v2, VerificationStatus.NOT_REGISTERED),
                            ),
                            VerificationStatus.VERIFIED_HANDLING_OFF,
                        ),
                        BaseLinkEntry(allowed, listOf(), VerificationStatus.ENABLED_HANDLING_OFF),
                        BaseLinkEntry(unverified, listOf(), VerificationStatus.NOT_VERIFIED),
                    ),
                ),
            ),
            overview.sections,
        )
    }

    @Test
    fun `a custom scheme link and its variant have no status`() {
        val key = "declared|myapp|lit:home||none"
        val link =
            BaseLink(key, BaseLinkOrigin.Declared(mainScreen), LinkSpec("myapp", HostSpec.Literal("home"), null, PathSpec.None), null)
        val v1 = variant("v1", key, mainScreen)

        val overview =
            DeepLinksOverviewAssembler.assemble(
                discovery(screens = listOf(screen(mainScreen, "Main", link))),
                listOf(v1),
                verification(true, "home" to 2),
            )

        assertEquals(
            listOf(DeepLinksSection.Screen(mainScreen, "Main", listOf(BaseLinkEntry(link, listOf(VariantEntry(v1, null)), null)))),
            overview.sections,
        )
    }

    @Test
    fun `a web example is not verified when its host is state 0 and not registered when it is missing`() {
        val link =
            BaseLink(
                exampleWebKey,
                BaseLinkOrigin.Example(null, "https://example.com/offers"),
                LinkSpec("https", HostSpec.Literal("example.com"), null, PathSpec.Literal("/offers")),
                null,
            )

        val notVerified =
            DeepLinksOverviewAssembler.assemble(discovery(examples = listOf(link)), listOf(), verification(true, "example.com" to 0))
        val notRegistered =
            DeepLinksOverviewAssembler.assemble(discovery(examples = listOf(link)), listOf(), verification(true))

        assertEquals(
            listOf(DeepLinksSection.Examples(listOf(BaseLinkEntry(link, listOf(), VerificationStatus.NOT_VERIFIED)))),
            notVerified.sections,
        )
        assertEquals(
            listOf(DeepLinksSection.Examples(listOf(BaseLinkEntry(link, listOf(), VerificationStatus.NOT_REGISTERED)))),
            notRegistered.sections,
        )
    }

    @Test
    fun `without a verification snapshot there are no statuses and no link handling notice`() {
        val key = "declared|https|lit:example.com||lit:/offers"
        val link =
            BaseLink(
                key,
                BaseLinkOrigin.Declared(mainScreen),
                LinkSpec("https", HostSpec.Literal("example.com"), null, PathSpec.Literal("/offers")),
                null,
            )
        val v1 = webVariant("v1", key, mainScreen, "example.com")

        val overview =
            DeepLinksOverviewAssembler.assemble(discovery(screens = listOf(screen(mainScreen, "Main", link))), listOf(v1), null)

        val entry = (overview.sections.single() as DeepLinksSection.Screen).entries.single()
        assertNull(entry.status)
        assertNull(entry.variants.single().status)
        assertEquals(listOf<DeepLinksNotice>(), overview.notices)
    }
}
