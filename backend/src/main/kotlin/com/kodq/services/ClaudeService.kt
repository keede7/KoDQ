package com.kodq.services

import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.util.Base64

@Serializable
data class AnalyzeResponse(
    val result: String,
    val score: Int,
    val details: String
)

interface IClaudeService {
    suspend fun analyzeOutfit(imageBytes: ByteArray, mediaType: String): AnalyzeResponse
}

class ClaudeService : IClaudeService {
    private val dotenv = dotenv { ignoreIfMissing = true }
    private val apiKey = dotenv["ANTHROPIC_API_KEY"]
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    override suspend fun analyzeOutfit(imageBytes: ByteArray, mediaType: String): AnalyzeResponse {
        val base64Image = Base64.getEncoder().encodeToString(imageBytes)
        val prompt = """
            이 사진에서 상의와 하의의 코디 궁합을 분석해주세요. 만약 신발, 양말, 악세서리까지 있다면 포함하여 분석해주세요. 
            그리고 어울리지 않다면 보완점을 추가로 설명해주세요.

            다음 형식의 JSON만 반환해주세요 (다른 텍스트 없이):
            {
              "result": "어울림" 또는 "안어울림",
              "score": 0~100 사이 숫자,
              "details": "색상, 재질, 스타일 조합에 대한 상세 설명 (2~5문장)"
            }
        """.trimIndent()

        val requestBody = buildJsonObject {
            put("model", "claude-sonnet-4-6")
            put("max_tokens", 1024)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "user")
                    putJsonArray("content") {
                        addJsonObject {
                            put("type", "image")
                            putJsonObject("source") {
                                put("type", "base64")
                                put("media_type", mediaType)
                                put("data", base64Image)
                            }
                        }
                        addJsonObject {
                            put("type", "text")
                            put("text", prompt)
                        }
                    }
                }
            }
        }

        val response = client.post("https://api.anthropic.com/v1/messages") {
            header("x-api-key", apiKey)
            header("anthropic-version", "2023-06-01")
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.body<String>()
            println("Claude API error ${response.status}: $errorBody")
            throw RuntimeException("Claude API error: ${response.status} - $errorBody")
        }

        val responseJson = response.body<JsonObject>()
        println("responseJson :$responseJson")
        val contentArray = responseJson["content"]?.jsonArray
        val textBlock = contentArray?.firstOrNull {
            it.jsonObject["type"]?.jsonPrimitive?.content == "text"
        }
        val rawText = textBlock?.jsonObject?.get("text")?.jsonPrimitive?.content ?: "{}"
        val text = rawText.trim()
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```")
            .trim()

        return try {
            val parsed = json.parseToJsonElement(text).jsonObject
            AnalyzeResponse(
                result = parsed["result"]?.jsonPrimitive?.content ?: "분석 실패",
                score = parsed["score"]?.jsonPrimitive?.int ?: 0,
                details = parsed["details"]?.jsonPrimitive?.content ?: ""
            )
        } catch (e: Exception) {
            println("JSON parse error: $text")
            throw RuntimeException("응답 파싱 실패: ${e.message}")
        }
    }
}
