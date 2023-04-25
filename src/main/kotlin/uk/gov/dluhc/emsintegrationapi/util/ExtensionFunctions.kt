@file:JvmName("Extensions")
package uk.gov.dluhc.emsintegrationapi.util

import org.apache.commons.codec.binary.Base64

fun ByteArray.toBase64String(): String {
    if (Base64.isBase64(this)) {
        return String(this)
    }
    return Base64.encodeBase64String(this)
}
