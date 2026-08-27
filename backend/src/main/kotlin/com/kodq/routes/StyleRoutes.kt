package com.kodq.routes

import com.kodq.services.ClaudeService
import com.kodq.services.IClaudeService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

internal fun detectMediaType(bytes: ByteArray): String? {
    if (bytes.size < 12) return null
    return when {
        bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() -> "image/jpeg"
        bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() -> "image/png"
        bytes[0] == 0x47.toByte() && bytes[1] == 0x49.toByte() && bytes[2] == 0x46.toByte() -> "image/gif"
        bytes[0] == 0x52.toByte() && bytes[1] == 0x49.toByte() && bytes[2] == 0x46.toByte() && bytes[3] == 0x46.toByte() &&
        bytes[8] == 0x57.toByte() && bytes[9] == 0x45.toByte() && bytes[10] == 0x42.toByte() && bytes[11] == 0x50.toByte() -> "image/webp"
        else -> null
    }
}

fun Route.styleRoutes(claudeService: IClaudeService = ClaudeService()) {

    post("/api/analyze") {
        val multipart = call.receiveMultipart()
        var imageBytes: ByteArray? = null
        var imageMediaType = "image/jpeg"

        multipart.forEachPart { part ->
            if (part is PartData.FileItem) {
                imageBytes = part.streamProvider().readBytes()
                imageMediaType = part.contentType?.toString() ?: "image/jpeg"
            }
            part.dispose()
        }

        if (imageBytes == null) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "이미지를 업로드해주세요."))
            return@post
        }

        val detectedMediaType = detectMediaType(imageBytes!!) ?: imageMediaType

        try {
            val result = claudeService.analyzeOutfit(imageBytes!!, detectedMediaType)
            call.respond(result)
        } catch (e: Exception) {
            println("analyzeOutfit failed: ${e.message}")
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "분석 실패")))
        }
    }
}
