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
            당신은 10년 경력의 전문 패션 스타일리스트입니다. 제공된 의상 사진을 분석해 코디 궁합을 평가해주세요.

            ## 분석 전 확인
            사진에서 상의와 하의(또는 전신 코디)가 명확히 식별되지 않는 경우(풍경, 음식, 의상 없는 사진 등),
            다음 JSON을 반환하고 분석을 종료하세요:
            {"result": "분석불가", "score": 0, "details": "의상을 식별할 수 없습니다. 상의와 하의가 함께 나온 코디 사진을 업로드해주세요."}

            ## 분석 기준
            아래 세 가지 기준을 종합해 평가하세요:

            1. **색상 조화**: 보색 대비, 유사색 배합, 무채색 활용 여부를 판단하세요.
               - 색상 대비가 자연스럽고 의도적인가?
               - 색상 수가 과도하게 많지 않은가?

            2. **재질/텍스처 매치**: 계절감과 포멀/캐주얼 밸런스를 판단하세요.
               - 재질이 서로 어울리는가? (예: 린넨+린넨, 데님+면 등)
               - 격식 수준이 일치하는가? (예: 슈트 상의+트레이닝 하의는 부조화)

            3. **스타일 통일성**: 전체 무드와 방향성을 판단하세요.
               - 전체 코디가 하나의 스타일(캐주얼, 포멀, 스트릿 등)로 수렴하는가?
               - 아이템 간 실루엣 밸런스가 적절한가?

            ## 점수 기준 (score)
            - 90~100: 완벽한 조화. 전문 스타일리스트 수준의 코디
            - 70~89: 잘 어울림. 일상 착용에 적합하며 스타일감이 있음
            - 50~69: 무난함. 큰 문제는 없으나 개선 여지가 있음
            - 30~49: 어색한 조합. 수정이 필요한 수준
            - 0~29: 심각하게 어울리지 않음. 전면 재코디 필요

            ## 보완 제안 규칙
            score가 70 미만인 경우, details의 마지막 문장으로 반드시 아래 형식을 포함하세요:
            "개선 제안: [구체적인 대안 아이템이나 색상/스타일 변경 방향]"

            ## 출력 형식
            마크다운 코드블록 없이 순수 JSON만 반환하세요. 다른 텍스트는 절대 포함하지 마세요.
            result 값은 반드시 "어울림", "안어울림", "분석불가" 세 가지 중 하나여야 합니다.

            {
              "result": "어울림" | "안어울림" | "분석불가",
              "score": 0~100 사이 정수,
              "details": "색상·재질·스타일 조합에 대한 구체적 설명 (2~5문장, score < 70이면 마지막에 개선 제안 포함)"
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
