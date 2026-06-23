package com.verdy.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.verdy.BuildConfig
import com.verdy.domain.model.PlantHealthEvaluationResult
import com.verdy.domain.model.PlantIdentificationResult
import com.verdy.domain.model.enums.IdentificationConfidence
import com.verdy.domain.model.enums.PlantStatus
import com.verdy.domain.model.enums.SunExposure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAiClient @Inject constructor() {

    private val apiKey: String by lazy { decodeKey() }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val endpoint = "https://api.openai.com/v1/chat/completions"

    fun isConfigured(): Boolean = apiKey.isNotBlank()

    suspend fun identifyPlant(imagePath: String): Result<PlantIdentificationResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (!isConfigured()) error("API key de OpenAI no configurada")

                val imageBase64 = encodeImageToJpegBase64(imagePath)

                val systemPrompt = """
                    Eres un botánico experto. Analiza la imagen y responde ÚNICAMENTE con un JSON válido, sin markdown ni texto adicional.

                    Reglas:
                    1. Examina hojas, tallo, flores, textura, color y forma general.
                    2. Propón el nombre más probable aunque la certeza sea media.
                    3. Usa confidence LOW solo si no hay señales botánicas claras; en ese caso incluye alternativeNames (2-3 opciones) y suggestions (cómo mejorar la foto).
                    4. NO devuelvas "Planta desconocida" sin alternativeNames y sin describir rasgos observables en careSummary.
                    5. Todos los textos descriptivos en español.

                    Estructura JSON exacta:
                    {
                      "commonName": "nombre común en español",
                      "scientificName": "nombre científico o vacío",
                      "confidence": "HIGH|MEDIUM|LOW",
                      "alternativeNames": ["opción 1", "opción 2"],
                      "sunExposure": "INTERIOR|SEMI_SHADE|EXTERIOR",
                      "wateringFrequencyDays": número_entero,
                      "fertilizingFrequencyDays": número_entero_o_null,
                      "fertilizerType": "tipo de abono sugerido o vacío",
                      "hasFlowers": true_o_false_o_null,
                      "careSummary": "resumen corto y amigable de cuidados (máx 80 palabras)",
                      "curiosities": "2-3 curiosidades en español",
                      "regions": "regiones nativas o de cultivo",
                      "suggestions": "consejos si la foto no es clara o hay duda, o vacío"
                    }
                """.trimIndent()

                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", JSONArray().apply {
                            put(JSONObject().apply {
                                put("type", "text")
                                put("text", "Identifica esta planta y devuelve el JSON con todos los campos solicitados.")
                            })
                            put(JSONObject().apply {
                                put("type", "image_url")
                                put("image_url", JSONObject().apply {
                                    put("url", "data:image/jpeg;base64,$imageBase64")
                                    put("detail", "high")
                                })
                            })
                        })
                    })
                }

                val body = JSONObject().apply {
                    put("model", "gpt-4o")
                    put("messages", messages)
                    put("max_tokens", 1000)
                }

                val response = sendRequest(body.toString())
                parseIdentificationResponse(response)
            }
        }

    suspend fun evaluatePlantHealth(
        imagePath: String,
        plantName: String,
        scientificName: String?,
        currentStatus: PlantStatus
    ): Result<PlantHealthEvaluationResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (!isConfigured()) error("API key de OpenAI no configurada")

                val imageBase64 = encodeImageToJpegBase64(imagePath)
                val statusLabel = when (currentStatus) {
                    PlantStatus.HEALTHY -> "Saludable"
                    PlantStatus.NEEDS_ATTENTION -> "Necesita atención"
                    PlantStatus.RECOVERING -> "En recuperación"
                }
                val scientific = scientificName?.takeIf { it.isNotBlank() } ?: "desconocido"

                val systemPrompt = """
                    Eres un botánico experto en diagnóstico de salud de plantas. Analiza la imagen y responde ÚNICAMENTE con un JSON válido, sin markdown ni texto adicional.

                    Contexto de la planta:
                    - Nombre: $plantName
                    - Nombre científico: $scientific
                    - Estado actual registrado: $statusLabel

                    Evalúa la salud visual: color de hojas, manchas, amarilleo, marchitamiento, plagas visibles, sequedad, exceso de agua, daño mecánico, crecimiento.

                    Reglas:
                    1. Todos los textos en español, lenguaje amigable para usuarios sin conocimientos técnicos.
                    2. suggestedStatus debe ser HEALTHY, NEEDS_ATTENTION o RECOVERING según lo observado.
                    3. Si la foto es borrosa, oscura o no muestra bien la planta, usa confidence LOW y explica en photoTips cómo mejorarla.
                    4. Incluye observaciones concretas de lo que ves, aunque la confianza sea baja.

                    Estructura JSON exacta:
                    {
                      "suggestedStatus": "HEALTHY|NEEDS_ATTENTION|RECOVERING",
                      "confidence": "HIGH|MEDIUM|LOW",
                      "observations": "qué se observa en la planta",
                      "possibleIssues": ["problema 1", "problema 2"],
                      "recommendedActions": ["acción 1", "acción 2"],
                      "photoTips": "cómo tomar una mejor foto para evaluar (luz, enfoque, ángulo)",
                      "summary": "resumen amigable del estado de salud (máx 80 palabras)"
                    }
                """.trimIndent()

                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", JSONArray().apply {
                            put(JSONObject().apply {
                                put("type", "text")
                                put("text", "Evalúa el estado de salud de esta planta y devuelve el JSON solicitado.")
                            })
                            put(JSONObject().apply {
                                put("type", "image_url")
                                put("image_url", JSONObject().apply {
                                    put("url", "data:image/jpeg;base64,$imageBase64")
                                    put("detail", "high")
                                })
                            })
                        })
                    })
                }

                val body = JSONObject().apply {
                    put("model", "gpt-4o")
                    put("messages", messages)
                    put("max_tokens", 800)
                }

                val response = sendRequest(body.toString())
                parseHealthEvaluationResponse(response)
            }
        }

    suspend fun getCuriosities(plantName: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (!isConfigured()) error("API key de OpenAI no configurada")

                val prompt = """
                    Eres un experto en plantas. Dame 3 curiosidades o consejos de cuidado para "$plantName".
                    Responde en español, de forma amigable y concisa, en un solo párrafo de máximo 200 palabras.
                    No uses listas ni markdown, solo texto fluido.
                """.trimIndent()

                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                }

                val body = JSONObject().apply {
                    put("model", "gpt-4o")
                    put("messages", messages)
                    put("max_tokens", 300)
                }

                val response = sendRequest(body.toString())
                extractContent(response)
            }
        }

    private fun sendRequest(bodyJson: String): String {
        val request = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(bodyJson.toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                error("Error OpenAI ${response.code}: $errorBody")
            }
            return response.body?.string() ?: error("Respuesta vacía de OpenAI")
        }
    }

    private fun extractContent(responseJson: String): String {
        val json = JSONObject(responseJson)
        return json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
    }

    private fun parseIdentificationResponse(responseJson: String): PlantIdentificationResult {
        val content = extractContent(responseJson)
        val cleaned = content
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```").trim()

        val json = JSONObject(cleaned)
        val sunExposureStr = json.optString("sunExposure", "SEMI_SHADE")
        val sunExposure = runCatching { SunExposure.valueOf(sunExposureStr) }
            .getOrDefault(SunExposure.SEMI_SHADE)

        val confidenceStr = json.optString("confidence", "MEDIUM")
        val confidence = runCatching { IdentificationConfidence.valueOf(confidenceStr) }
            .getOrDefault(IdentificationConfidence.MEDIUM)

        val alternativeNames = json.optJSONArray("alternativeNames")?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                arr.optString(i).takeIf { it.isNotBlank() }
            }
        } ?: emptyList()

        val fertilizingDays = if (json.has("fertilizingFrequencyDays") && !json.isNull("fertilizingFrequencyDays")) {
            json.optInt("fertilizingFrequencyDays").takeIf { it > 0 }
        } else null

        val hasFlowers = when {
            json.has("hasFlowers") && !json.isNull("hasFlowers") -> json.optBoolean("hasFlowers")
            else -> null
        }

        return PlantIdentificationResult(
            commonName = json.optString("commonName", "Planta no identificada"),
            scientificName = json.optString("scientificName", ""),
            sunExposure = sunExposure,
            wateringFrequencyDays = json.optInt("wateringFrequencyDays", 7).coerceAtLeast(1),
            curiosities = json.optString("curiosities", ""),
            regions = json.optString("regions", ""),
            confidence = confidence,
            alternativeNames = alternativeNames,
            fertilizingFrequencyDays = fertilizingDays,
            fertilizerType = json.optString("fertilizerType", "").takeIf { it.isNotBlank() },
            hasFlowers = hasFlowers,
            careSummary = json.optString("careSummary", ""),
            suggestions = json.optString("suggestions", "")
        )
    }

    private fun parseHealthEvaluationResponse(responseJson: String): PlantHealthEvaluationResult {
        val content = extractContent(responseJson)
        val cleaned = content
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```").trim()

        val json = JSONObject(cleaned)

        val statusStr = json.optString("suggestedStatus", "HEALTHY")
        val suggestedStatus = runCatching { PlantStatus.valueOf(statusStr) }
            .getOrDefault(PlantStatus.HEALTHY)

        val confidenceStr = json.optString("confidence", "MEDIUM")
        val confidence = runCatching { IdentificationConfidence.valueOf(confidenceStr) }
            .getOrDefault(IdentificationConfidence.MEDIUM)

        fun parseStringList(key: String): List<String> =
            json.optJSONArray(key)?.let { arr ->
                (0 until arr.length()).mapNotNull { i ->
                    arr.optString(i).takeIf { it.isNotBlank() }
                }
            } ?: emptyList()

        return PlantHealthEvaluationResult(
            suggestedStatus = suggestedStatus,
            confidence = confidence,
            observations = json.optString("observations", ""),
            possibleIssues = parseStringList("possibleIssues"),
            recommendedActions = parseStringList("recommendedActions"),
            photoTips = json.optString("photoTips", ""),
            summary = json.optString("summary", "")
        )
    }

    private fun encodeImageToJpegBase64(imagePath: String): String {
        val bitmap = BitmapFactory.decodeFile(imagePath)
            ?: error("No se pudo leer la imagen")
        val jpegBytes = java.io.ByteArrayOutputStream().also { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }.toByteArray()
        return Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
    }

    private fun decodeKey(): String {
        val encoded = BuildConfig.OAK_ENC
        if (encoded.isBlank()) return ""
        val seed = 63
        return encoded.split(",")
            .filter { it.isNotBlank() }
            .map { it.trim().toInt().xor(seed).toChar() }
            .joinToString("")
    }
}
