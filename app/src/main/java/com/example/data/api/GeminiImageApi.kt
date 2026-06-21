package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini Image Generation Req/Resp Models ---

data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null
)

data class Content(
    @Json(name = "parts") val parts: List<Part>
)

data class Part(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: InlineData? = null
)

data class InlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String // Base64 representation
)

data class GenerationConfig(
    @Json(name = "imageConfig") val imageConfig: ImageConfig? = null,
    @Json(name = "responseModalities") val responseModalities: List<String>? = null,
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "candidateCount") val candidateCount: Int? = null
)

data class ImageConfig(
    @Json(name = "aspectRatio") val aspectRatio: String, // "1:1", "3:4", "4:3", "16:9", "9:16"
    @Json(name = "imageSize") val imageSize: String = "1K" // "1K", "2K", "4K"
)

data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

data class Candidate(
    @Json(name = "content") val content: Content?
)

// --- Retrofit Interface ---

interface GeminiImageService {
    @POST("v1beta/models/imagen-3.0-generate-002:generateContent")
    suspend fun generateImage(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST("v1beta/models/{model}:generateVideos")
    suspend fun generateVideos(
        @retrofit2.http.Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateVideosRequest
    ): VeoOperationResponse

    @retrofit2.http.GET("v1beta/{name}")
    suspend fun getOperation(
        @retrofit2.http.Path(value = "name", encoded = true) name: String,
        @Query("key") apiKey: String
    ): VeoOperationResponse
}

// --- Veo Video Generation Req/Resp Models ---

data class GenerateVideosRequest(
    @Json(name = "prompt") val prompt: String,
    @Json(name = "config") val config: VeoConfig? = null
)

data class VeoConfig(
    @Json(name = "numberOfVideos") val numberOfVideos: Int = 1,
    @Json(name = "resolution") val resolution: String = "1080p", // "720p", "1080p"
    @Json(name = "aspectRatio") val aspectRatio: String = "16:9" // "1:1", "16:9", "3:4" etc.
)

data class VeoOperationResponse(
    @Json(name = "name") val name: String,
    @Json(name = "done") val done: Boolean = false,
    @Json(name = "error") val error: VeoOperationError? = null,
    @Json(name = "response") val response: VeoOperationResult? = null
)

data class VeoOperationError(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "message") val message: String? = null
)

data class VeoOperationResult(
    @Json(name = "generatedVideos") val generatedVideos: List<VeoGeneratedVideo>? = null
)

data class VeoGeneratedVideo(
    @Json(name = "video") val video: VeoVideoData? = null
)

data class VeoVideoData(
    @Json(name = "uri") val uri: String? = null,
    @Json(name = "videoBytes") val videoBytes: String? = null
)

// --- Retrofit Builder ---

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    val service: GeminiImageService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiImageService::class.java)
    }
}
