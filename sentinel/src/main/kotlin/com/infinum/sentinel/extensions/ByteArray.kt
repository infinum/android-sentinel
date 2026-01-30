package com.infinum.sentinel.extensions

import java.math.BigInteger
import java.util.Locale

internal fun ByteArray.asHexString(): String =
    String.format(
        Locale.ENGLISH,
        "%0${this.size shl 1}X",
        BigInteger(1, this),
    )
