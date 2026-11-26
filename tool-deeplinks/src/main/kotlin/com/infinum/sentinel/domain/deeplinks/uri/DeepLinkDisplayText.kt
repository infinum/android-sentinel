package com.infinum.sentinel.domain.deeplinks.uri

import com.infinum.sentinel.domain.deeplinks.models.DeepLinkUri
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction

/**
 * A readable form of a link, for display only. Copy, share and launch use the link [DeepLinkUriBuilder] builds.
 *
 * Percent escapes in the path and the query are decoded, except where the decoded character would change how the link
 * reads or would hide in the text:
 * - in the path `/ ? # %`, and in query keys and values `& = # % +` (Android reads a `+` in a value as a space);
 * - a space at the start or end of the path, a key or a value;
 * - control, format (bidi and zero-width marks), separator, private-use and unassigned characters, and letters that
 *   render blank, such as the Hangul fillers and the Braille blank;
 * - a run of escapes that isn't valid UTF-8, which stays exactly as it was.
 *
 * User info and the fragment are shown as stored.
 */
internal object DeepLinkDisplayText {
    private const val PATH_KEPT = "/?#%"
    private const val QUERY_KEPT = "&=#%+"
    private const val SPACE = ' '.code
    private const val ESCAPE_LENGTH = 3
    private const val HEX_RADIX = 16

    /** Characters outside the hidden categories that still render blank: Hangul fillers, Braille blank, grapheme joiner. */
    private const val BLANK_LOOKING = "\u115F\u1160\u3164\uFFA0\u2800\u034F"

    /** Builds the readable link of [uri]. */
    fun format(uri: DeepLinkUri): String =
        buildString {
            append(uri.scheme).append(':')
            if (uri.hasAuthority) {
                append("//")
                uri.userInfo?.let { append(it).append('@') }
                append(uri.host.orEmpty())
                uri.port?.let { append(':').append(it) }
            }
            append(render(pathTokens(uri.path), PATH_KEPT))
            if (uri.params.isNotEmpty()) {
                append('?')
                append(uri.params.joinToString("&") { "${readableQuery(it.key)}=${readableQuery(it.value)}" })
            }
            uri.fragment?.let { append('#').append(it) }
        }

    /** A piece of the text: a character shown decoded unless it is kept encoded, or raw text shown as it is. */
    private sealed interface Token {
        data class Decoded(
            val codePoint: Int,
        ) : Token

        data class Raw(
            val text: String,
        ) : Token
    }

    private fun readableQuery(plain: String): String = render(plain.codePoints().toArray().map { Token.Decoded(it) }, QUERY_KEPT)

    /**
     * Splits an encoded path into tokens: each run of escapes is decoded as UTF-8 when it is valid. Other characters are
     * already in their encoded form and stay raw, unless they would hide in the text.
     */
    private fun pathTokens(encoded: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var index = 0
        while (index < encoded.length) {
            val runEnd = escapeRunEnd(encoded, index)
            if (runEnd > index) {
                tokens += escapeRunTokens(encoded.substring(index, runEnd))
                index = runEnd
            } else {
                val codePoint = encoded.codePointAt(index)
                tokens += if (isHidden(codePoint)) Token.Decoded(codePoint) else Token.Raw(String(Character.toChars(codePoint)))
                index += Character.charCount(codePoint)
            }
        }
        return tokens
    }

    /** The decoded characters of a run of escapes, or the run itself when it isn't valid UTF-8. */
    private fun escapeRunTokens(run: String): List<Token> =
        decodeStrict(run)?.codePoints()?.toArray()?.map { Token.Decoded(it) } ?: listOf(Token.Raw(run))

    private fun render(
        tokens: List<Token>,
        kept: String,
    ): String =
        buildString {
            tokens.forEachIndexed { index, token ->
                when (token) {
                    is Token.Raw -> append(token.text)
                    is Token.Decoded -> {
                        val isEdge = index == 0 || index == tokens.lastIndex
                        if (isKept(token.codePoint, kept) || (isEdge && token.codePoint == SPACE)) {
                            append(DeepLinkUriEncoder.encodeQueryComponent(String(Character.toChars(token.codePoint))))
                        } else {
                            appendCodePoint(token.codePoint)
                        }
                    }
                }
            }
        }

    private fun isKept(
        codePoint: Int,
        kept: String,
    ): Boolean = (codePoint < Char.MIN_SURROGATE.code && kept.indexOf(codePoint.toChar()) >= 0) || isHidden(codePoint)

    /** True for a character that doesn't show, shows as something else, or reorders the text around it. */
    private fun isHidden(codePoint: Int): Boolean =
        (Character.isBmpCodePoint(codePoint) && BLANK_LOOKING.indexOf(codePoint.toChar()) >= 0) ||
            isHiddenCategory(codePoint)

    private fun isHiddenCategory(codePoint: Int): Boolean =
        when (Character.getType(codePoint).toByte()) {
            Character.CONTROL,
            Character.FORMAT,
            Character.PRIVATE_USE,
            Character.SURROGATE,
            Character.UNASSIGNED,
            Character.LINE_SEPARATOR,
            Character.PARAGRAPH_SEPARATOR,
            -> true
            Character.SPACE_SEPARATOR -> codePoint != SPACE
            else -> false
        }

    /** The end of the run of `%XX` escapes that starts at [start], or [start] when there is none. */
    private fun escapeRunEnd(
        text: String,
        start: Int,
    ): Int {
        var end = start
        while (isEscapeAt(text, end)) {
            end += ESCAPE_LENGTH
        }
        return end
    }

    private fun isEscapeAt(
        text: String,
        index: Int,
    ): Boolean =
        if (index + ESCAPE_LENGTH > text.length || text[index] != '%') {
            false
        } else {
            isHex(text[index + 1]) && isHex(text[index + 2])
        }

    /** ASCII hex digits only: `Character.digit` would also take full-width and other Unicode digits. */
    private fun isHex(char: Char): Boolean = char in '0'..'9' || char in 'a'..'f' || char in 'A'..'F'

    /** Decodes a run of escapes as UTF-8, or returns null when the bytes aren't valid UTF-8. */
    private fun decodeStrict(run: String): String? {
        val bytes =
            ByteArray(run.length / ESCAPE_LENGTH) { byteIndex ->
                val start = byteIndex * ESCAPE_LENGTH + 1
                run.substring(start, start + 2).toInt(HEX_RADIX).toByte()
            }
        return try {
            Charsets.UTF_8
                .newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString()
        } catch (ignored: CharacterCodingException) {
            null
        }
    }
}
