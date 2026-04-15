package com.kodq

import com.kodq.plugins.configureSerialization
import com.kodq.routes.styleRoutes
import com.kodq.services.AnalyzeResponse
import com.kodq.services.IClaudeService
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeClaudeService(
    private val response: AnalyzeResponse? = null,
    private val shouldThrow: Boolean = false
) : IClaudeService {
    override suspend fun analyzeOutfit(imageBytes: ByteArray, mediaType: String): AnalyzeResponse {
        if (shouldThrow) throw RuntimeException("Test ClaudeService error")
        return response ?: throw RuntimeException("No response configured")
    }
}

private val minimalJpeg: ByteArray = byteArrayOf(
    0xFF.toByte(), 0xD8.toByte(),
    *ByteArray(14)
)

class StyleRoutesTest {

    @Test
    fun `image part missing returns 400 with error json`() = testApplication {
        application {
            configureSerialization()
            routing {
                styleRoutes(FakeClaudeService(response = AnalyzeResponse("어울림", 85, "테스트 피드백")))
            }
        }
        val response = client.post("/api/analyze") {
            setBody(MultiPartFormDataContent(formData { }))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.bodyAsText()
        val json = Json.parseToJsonElement(body).jsonObject
        assertTrue(json.containsKey("error"), "Response should contain 'error' key, got: $body")
    }

    @Test
    fun `ClaudeService throws returns 500 with error json`() = testApplication {
        application {
            configureSerialization()
            routing {
                styleRoutes(FakeClaudeService(shouldThrow = true))
            }
        }
        val response = client.post("/api/analyze") {
            setBody(MultiPartFormDataContent(
                formData {
                    append("image", minimalJpeg, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "form-data; name=\"image\"; filename=\"test.jpg\"")
                    })
                }
            ))
        }
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        val body = response.bodyAsText()
        val json = Json.parseToJsonElement(body).jsonObject
        assertTrue(json.containsKey("error"), "Response should contain 'error' key, got: $body")
    }

    @Test
    fun `valid image returns 200 with result score details`() = testApplication {
        application {
            configureSerialization()
            routing {
                styleRoutes(FakeClaudeService(response = AnalyzeResponse("어울림", 85, "테스트 피드백")))
            }
        }
        val response = client.post("/api/analyze") {
            setBody(MultiPartFormDataContent(
                formData {
                    append("image", minimalJpeg, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "form-data; name=\"image\"; filename=\"test.jpg\"")
                    })
                }
            ))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        val json = Json.parseToJsonElement(body).jsonObject
        assertTrue(json.containsKey("result"), "Response should contain 'result' key, got: $body")
        assertTrue(json.containsKey("score"), "Response should contain 'score' key, got: $body")
        assertTrue(json.containsKey("details"), "Response should contain 'details' key, got: $body")
    }
}
