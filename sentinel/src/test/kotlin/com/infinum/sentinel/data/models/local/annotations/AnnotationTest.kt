package com.infinum.sentinel.data.models.local.annotations

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class AnnotationTest {
    private fun viewAnnotation() =
        Annotation(
            id = "a3k9x2m1",
            note = "button too small",
            toolkit = AnnotationToolkit.VIEW,
            targetType = "MaterialButton",
            targetPath = "CoordinatorLayout>MaterialCardView>MaterialButton",
            viewId = "submitButton",
            testTag = null,
            sourceLocation = null,
            ordinal = 1,
            ordinalCount = 3,
            text = "Submit",
            screen = "MainActivity",
            fragment = "CheckoutFragment",
            route = null,
        )

    @Test
    fun `view annotation serializes every locator field`() {
        val expected =
            """{"id":"a3k9x2m1","note":"button too small","toolkit":"view","targetType":"MaterialButton",""" +
                """"targetPath":"CoordinatorLayout>MaterialCardView>MaterialButton","viewId":"submitButton",""" +
                """"testTag":null,"sourceLocation":null,"ordinal":1,"ordinalCount":3,"text":"Submit",""" +
                """"screen":"MainActivity","fragment":"CheckoutFragment","route":null,"isSent":false}"""

        val actual = viewAnnotation().toExportJson()

        assertEquals(expected, actual)
    }

    @Test
    fun `compose annotation carries composable name and source location`() {
        val annotation =
            viewAnnotation().copy(
                toolkit = AnnotationToolkit.COMPOSE,
                targetType = "PrimaryButton",
                viewId = null,
                testTag = "submit_button",
                sourceLocation = "CheckoutScreen.kt:142",
            )

        val actual = annotation.toExportJson()

        assertTrue(actual.contains(""""toolkit":"compose""""))
        assertTrue(actual.contains(""""sourceLocation":"CheckoutScreen.kt:142""""))
        assertTrue(actual.contains(""""testTag":"submit_button""""))
        assertTrue(actual.contains(""""viewId":null"""))
    }

    @Test
    fun `a list serializes as a json array`() {
        val actual = Annotation.toExportJson(listOf(viewAnnotation(), viewAnnotation().copy(id = "b4l0y3n2")))

        assertTrue(actual.startsWith("[{"))
        assertTrue(actual.endsWith("}]"))
        assertTrue(actual.contains(""""id":"b4l0y3n2""""))
    }

    @Test
    fun `an empty list serializes as an empty json array`() {
        assertEquals("[]", Annotation.toExportJson(emptyList()))
    }
}
