package com.infinum.sentinel.extensions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Array<StackTraceElement> extension functions.
 * Tests stack trace formatting.
 */
internal class ArrayExtensionsTest {
    @Test
    fun `asStringArray formats single stack trace element`() {
        val element =
            StackTraceElement(
                "com.example.MyClass",
                "myMethod",
                "MyClass.kt",
                42,
            )
        val input = arrayOf(element)

        val result = input.asStringArray()

        assertEquals(1, result.size)
        assertEquals("com.example.MyClass.myMethod(MyClass.kt:42)", result[0])
    }

    @Test
    fun `asStringArray formats multiple stack trace elements`() {
        val elements =
            arrayOf(
                StackTraceElement("com.example.ClassA", "methodA", "ClassA.kt", 10),
                StackTraceElement("com.example.ClassB", "methodB", "ClassB.kt", 20),
                StackTraceElement("com.example.ClassC", "methodC", "ClassC.kt", 30),
            )

        val result = elements.asStringArray()

        assertEquals(3, result.size)
        assertEquals("com.example.ClassA.methodA(ClassA.kt:10)", result[0])
        assertEquals("com.example.ClassB.methodB(ClassB.kt:20)", result[1])
        assertEquals("com.example.ClassC.methodC(ClassC.kt:30)", result[2])
    }

    @Test
    fun `asStringArray formats native method`() {
        val element =
            StackTraceElement(
                "java.lang.System",
                "nativeMethod",
                null,
                -2,
            )
        val input = arrayOf(element)

        val result = input.asStringArray()

        assertEquals(1, result.size)
        assertEquals("java.lang.System.nativeMethod[Native Method]", result[0])
    }

    @Test
    fun `asStringArray handles empty array`() {
        val input = emptyArray<StackTraceElement>()

        val result = input.asStringArray()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `asStringArray handles mixed native and regular methods`() {
        val elements =
            arrayOf(
                StackTraceElement("com.example.MyClass", "regularMethod", "MyClass.kt", 100),
                StackTraceElement("java.lang.System", "nativeMethod", null, -2),
                StackTraceElement("com.example.OtherClass", "anotherMethod", "OtherClass.java", 50),
            )

        val result = elements.asStringArray()

        assertEquals(3, result.size)
        assertEquals("com.example.MyClass.regularMethod(MyClass.kt:100)", result[0])
        assertEquals("java.lang.System.nativeMethod[Native Method]", result[1])
        assertEquals("com.example.OtherClass.anotherMethod(OtherClass.java:50)", result[2])
    }

    @Test
    fun `asStringArray handles Java files`() {
        val element =
            StackTraceElement(
                "com.example.JavaClass",
                "javaMethod",
                "JavaClass.java",
                123,
            )
        val input = arrayOf(element)

        val result = input.asStringArray()

        assertEquals("com.example.JavaClass.javaMethod(JavaClass.java:123)", result[0])
    }

    @Test
    fun `asStringArray handles line number zero`() {
        val element =
            StackTraceElement(
                "com.example.MyClass",
                "myMethod",
                "MyClass.kt",
                0,
            )
        val input = arrayOf(element)

        val result = input.asStringArray()

        assertEquals("com.example.MyClass.myMethod(MyClass.kt:0)", result[0])
    }

    @Test
    fun `asStringArray handles deeply nested package names`() {
        val element =
            StackTraceElement(
                "com.example.project.feature.subfeature.ui.MyClass",
                "myMethod",
                "MyClass.kt",
                999,
            )
        val input = arrayOf(element)

        val result = input.asStringArray()

        assertEquals(
            "com.example.project.feature.subfeature.ui.MyClass.myMethod(MyClass.kt:999)",
            result[0],
        )
    }
}
