package com.infinum.sentinel.domain.deeplinks.uri

import java.io.ByteArrayOutputStream

/**
 * Percent-encoding and decoding of deep link components, working on UTF-8 bytes.
 */
internal object DeepLinkUriEncoder {
    private const val HEX_RADIX = 16
    private const val HEX_DIGITS = "0123456789ABCDEF"
    private const val BYTE_MASK = 0xFF
    private const val NIBBLE_BITS = 4
    private const val NIBBLE_MASK = 0x0F
    private const val ESCAPE_LENGTH = 3
    private const val ESCAPE_CHAR = '%'
    private const val UNRESERVED_MARKS = "-._~"
    private const val PATH_MARKS = "/:@!\$&'()*+,;="

    /**
     * Encodes a plain query key or value.
     *
     * Every UTF-8 byte outside `A-Z a-z 0-9 - . _ ~` becomes `%XX` with uppercase hex.
     * A space becomes `%20`, never `+`: Android's `Uri.getQueryParameter` decodes `+` as a space,
     * so using `%20` for spaces and `%2B` for a literal plus keeps the value unambiguous.
     */
    internal fun encodeQueryComponent(value: String): String = encode(value, "")

    /**
     * Encodes a plain path with the same rule as [encodeQueryComponent], but leaves
     * `/ : @ ! $ & ' ( ) * + , ; =` unchanged (RFC 3986 pchar plus `/`).
     */
    internal fun encodePath(plain: String): String = encode(plain, PATH_MARKS)

    /**
     * Decodes `%XX` sequences as UTF-8 bytes. Invalid or truncated sequences stay literal.
     * When [plusAsSpace] is true, `+` becomes a space.
     */
    internal fun decodeComponent(
        encoded: String,
        plusAsSpace: Boolean,
    ): String {
        val result = StringBuilder()
        val bytes = ByteArrayOutputStream()
        var index = 0
        while (index < encoded.length) {
            val decoded = escapedByteAt(encoded, index)
            if (decoded != null) {
                bytes.write(decoded)
                index += ESCAPE_LENGTH
            } else {
                flush(bytes, result)
                val char = encoded[index]
                result.append(if (plusAsSpace && char == '+') ' ' else char)
                index++
            }
        }
        flush(bytes, result)
        return result.toString()
    }

    /**
     * Decodes an encoded path. `+` is kept as a plus sign.
     */
    internal fun decodePath(encoded: String): String = decodeComponent(encoded, plusAsSpace = false)

    private fun encode(
        value: String,
        extraAllowed: String,
    ): String {
        val builder = StringBuilder()
        value.toByteArray(Charsets.UTF_8).forEach { byte ->
            val unsigned = byte.toInt() and BYTE_MASK
            val char = unsigned.toChar()
            if (isUnreserved(char) || extraAllowed.indexOf(char) >= 0) {
                builder.append(char)
            } else {
                builder.append(ESCAPE_CHAR)
                builder.append(HEX_DIGITS[unsigned shr NIBBLE_BITS])
                builder.append(HEX_DIGITS[unsigned and NIBBLE_MASK])
            }
        }
        return builder.toString()
    }

    private fun isUnreserved(char: Char): Boolean =
        char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' || UNRESERVED_MARKS.indexOf(char) >= 0

    private fun escapedByteAt(
        text: String,
        index: Int,
    ): Int? {
        if (text[index] != ESCAPE_CHAR || index + ESCAPE_LENGTH > text.length) return null
        val high = hexValue(text[index + 1])
        val low = hexValue(text[index + 2])
        return if (high < 0 || low < 0) null else high * HEX_RADIX + low
    }

    private fun hexValue(char: Char): Int = HEX_DIGITS.indexOf(char.uppercaseChar())

    private fun flush(
        bytes: ByteArrayOutputStream,
        result: StringBuilder,
    ) {
        if (bytes.size() > 0) {
            result.append(bytes.toString(Charsets.UTF_8.name()))
            bytes.reset()
        }
    }
}
