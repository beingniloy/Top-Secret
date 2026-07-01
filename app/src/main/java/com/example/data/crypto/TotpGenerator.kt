package com.example.data.crypto

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

object TotpGenerator {

    fun generateTOTP(secretBase32: String, timeMs: Long = System.currentTimeMillis(), periodSec: Int = 30, digits: Int = 6): String {
        try {
            val key = decodeBase32(secretBase32.trim().replace(" ", "").uppercase()) ?: return "000000"
            val counter = timeMs / 1000 / periodSec
            val data = ByteBuffer.allocate(8).putLong(counter).array()

            val mac = Mac.getInstance("HmacSHA1")
            mac.init(SecretKeySpec(key, "RAW"))
            val hash = mac.doFinal(data)

            val offset = (hash[hash.size - 1] and 0xF).toInt()
            var binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                    ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                    ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                    (hash[offset + 3].toInt() and 0xFF)

            var otp = binary % Math.pow(10.0, digits.toDouble()).toInt()
            var result = otp.toString()
            while (result.length < digits) {
                result = "0$result"
            }
            return result
        } catch (e: Exception) {
            e.printStackTrace()
            return "000000"
        }
    }

    private fun decodeBase32(base32: String): ByteArray? {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val bitsPerChar = 5
        var buffer = 0
        var bitsLeft = 0
        val out = mutableListOf<Byte>()

        for (c in base32) {
            val idx = allowedChars.indexOf(c)
            if (idx < 0) continue // Skip padding or invalid characters
            buffer = (buffer shl bitsPerChar) or idx
            bitsLeft += bitsPerChar
            if (bitsLeft >= 8) {
                out.add(((buffer ushr (bitsLeft - 8)) and 0xFF).toByte())
                bitsLeft -= 8
            }
        }
        return if (out.isEmpty()) null else out.toByteArray()
    }
}
