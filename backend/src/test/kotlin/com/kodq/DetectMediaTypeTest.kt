package com.kodq

import com.kodq.routes.detectMediaType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DetectMediaTypeTest {

    private fun paddedBytes(header: ByteArray, totalSize: Int = 16): ByteArray {
        return header + ByteArray(totalSize - header.size)
    }

    @Test
    fun `JPEG magic bytes returns image jpeg`() {
        val bytes = paddedBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte()))
        assertEquals("image/jpeg", detectMediaType(bytes))
    }

    @Test
    fun `PNG magic bytes returns image png`() {
        val bytes = paddedBytes(byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4E.toByte()))
        assertEquals("image/png", detectMediaType(bytes))
    }

    @Test
    fun `GIF magic bytes returns image gif`() {
        val bytes = paddedBytes(byteArrayOf(0x47.toByte(), 0x49.toByte(), 0x46.toByte()))
        assertEquals("image/gif", detectMediaType(bytes))
    }

    @Test
    fun `WebP magic bytes returns image webp`() {
        // RIFF????WEBP: bytes[0..3] = RIFF, bytes[4..7] = size (anything), bytes[8..11] = WEBP
        val bytes = ByteArray(16).also { buf ->
            buf[0] = 0x52.toByte(); buf[1] = 0x49.toByte(); buf[2] = 0x46.toByte(); buf[3] = 0x46.toByte()
            buf[8] = 0x57.toByte(); buf[9] = 0x45.toByte(); buf[10] = 0x42.toByte(); buf[11] = 0x50.toByte()
        }
        assertEquals("image/webp", detectMediaType(bytes))
    }

    @Test
    fun `unrecognized bytes returns null`() {
        val bytes = ByteArray(16) { 0x00.toByte() }
        assertNull(detectMediaType(bytes))
    }

    @Test
    fun `fewer than 12 bytes returns null`() {
        val bytes = ByteArray(8) { 0xFF.toByte() }
        assertNull(detectMediaType(bytes))
    }
}
