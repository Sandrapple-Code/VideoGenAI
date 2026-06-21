package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.text.TextPaint
import android.text.StaticLayout
import android.text.Layout
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID

object ImageStorageHelper {

    /**
     * Decode a base64 string to a Bitmap.
     */
    fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Encode a Bitmap to a base64 string.
     */
    fun encodeBitmapToBase64(bitmap: Bitmap, quality: Int = 90): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * Save base64 string or Bitmap as an app-local file.
     * Storing files rather than raw blobs in SQlite prevents CursorWindow memory issues.
     * Returns the absolute path of the saved file.
     */
    fun saveImageLocally(context: Context, base64Data: String): String? {
        val bitmap = decodeBase64ToBitmap(base64Data) ?: return null
        return saveBitmapLocally(context, bitmap)
    }

    fun saveVideoBytesLocally(context: Context, base64Data: String): String? {
        return try {
            val directory = File(context.filesDir, "generated_videos")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = "vid_${UUID.randomUUID()}.mp4"
            val file = File(directory, fileName)
            val videoBytes = Base64.decode(base64Data, Base64.DEFAULT)
            FileOutputStream(file).use { out ->
                out.write(videoBytes)
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("ImageStorageHelper", "Failed to save video bytes locally", e)
            null
        }
    }

    fun saveBitmapLocally(context: Context, bitmap: Bitmap): String? {
        return try {
            val directory = File(context.filesDir, "generated_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = "img_${UUID.randomUUID()}.jpg"
            val file = File(directory, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Save a bitmap into the device's public photo gallery / Pictures folder.
     * Returns true if successful.
     */
    fun saveImageToGallery(context: Context, bitmap: Bitmap, displayName: String = "AI_Image"): Boolean {
        val nameToSave = "${displayName.replace(" ", "_")}_${System.currentTimeMillis()}.jpg"
        val mimeType = "image/jpeg"
        
        return try {
            val contentResolver = context.contentResolver
            val imageUri: Uri?
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, nameToSave)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AI Studio Generated")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                
                imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    contentResolver.openOutputStream(imageUri).use { outStream ->
                        if (outStream != null) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    contentResolver.update(imageUri, contentValues, null, null)
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                val studioDir = File(imagesDir, "AI Studio Generated")
                if (!studioDir.exists()) {
                    studioDir.mkdirs()
                }
                val file = File(studioDir, nameToSave)
                FileOutputStream(file).use { outStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                }
                
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATA, file.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                    put(MediaStore.Images.Media.DISPLAY_NAME, nameToSave)
                }
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Download any remote image or video from an external URL and persist it to internal storage.
     */
    suspend fun downloadAndSaveFile(context: Context, fileUrl: String, isVideo: Boolean): String? {
        return withContext(Dispatchers.IO) {
            try {
                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val request = Request.Builder().url(fileUrl).build()
                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    val body = response.body ?: return@withContext null
                    
                    val directory = File(context.filesDir, if (isVideo) "generated_videos" else "generated_images")
                    if (!directory.exists()) {
                        directory.mkdirs()
                    }
                    val extension = if (isVideo) "mp4" else "jpg"
                    val fileName = "gen_${System.currentTimeMillis()}_${UUID.randomUUID()}.$extension"
                    val file = File(directory, fileName)
                    
                    body.byteStream().use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                    file.absolutePath
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Completely Offline Procedural Art Generator!
     * If the API fails or is offline, this generates a highly polished, stylized gradient card
     * with the user's prompt beautifully typeset and icons representing the creative theme.
     */
    fun generateProceduralStyledArt(context: Context, prompt: String, styleName: String, isVideo: Boolean): String? {
        try {
            val width = 1024
            val height = 1024
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Define gorgeous theme gradients based on selected creative style
            val (colorStart, colorEnd) = when (styleName) {
                "Cyberpunk" -> Pair(0xFF4A0E4E.toInt(), 0xFF00F0FF.toInt())  // Deep Purple to Cyber Neon Blue
                "Anime"     -> Pair(0xFFFF85A1.toInt(), 0xFFFCDDEC.toInt())  // Vibrant Cherry Blossom pinks
                "Cinematic" -> Pair(0xFF111827.toInt(), 0xFFF59E0B.toInt())  // Cinematic Slate to Golden Amber
                "3D Render" -> Pair(0xFF8B5CF6.toInt(), 0xFFEC4899.toInt())  // Indigo Violet to Pastel Pink
                "Watercolor"-> Pair(0xFFE0F2FE.toInt(), 0xFFFDA4AF.toInt())  // Soft watercolor Wash sky
                "Pixel Art" -> Pair(0xFF0F172A.toInt(), 0xFF22C55E.toInt())  // Dark Terminal Grid to Retro Pixel Green
                "Comic Book"-> Pair(0xFFEF4444.toInt(), 0xFFF59E0B.toInt())  // Energetic Pop-Art Red and Amber
                "Oil Painting"-> Pair(0xFF78350F.toInt(), 0xFFFCD34D.toInt()) // Rich Umber and Golden ochre canvas
                else        -> Pair(0xFF1E293B.toInt(), 0xFF64748B.toInt())  // Cosmic Slate fallback
            }

            // Draw rich linear background gradient
            val paintBg = Paint().apply {
                shader = LinearGradient(0f, 0f, 0f, height.toFloat(), colorStart, colorEnd, Shader.TileMode.CLAMP)
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintBg)

            // Draw some stylized artistic background noise or visual patterns
            val paintPats = Paint().apply {
                color = 0x1AFFFFFF
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }

            if (styleName == "Cyberpunk" || styleName == "Pixel Art") {
                // Draw a retro glowing digital grid
                val gridGap = 64f
                for (i in 0..(width / gridGap.toInt())) {
                    canvas.drawLine(i * gridGap, 0f, i * gridGap, height.toFloat(), paintPats)
                    canvas.drawLine(0f, i * gridGap, width.toFloat(), i * gridGap, paintPats)
                }
            } else if (styleName == "Comic Book" || styleName == "3D Render") {
                // Draw dynamic graphic starburst rays from center
                val centerX = width / 2f
                val centerY = height / 2f
                val rayCount = 18
                for (i in 0 until rayCount) {
                    val angle = (i * (2 * Math.PI / rayCount)).toFloat()
                    val stopX = centerX + (Math.cos(angle.toDouble()) * width).toFloat()
                    val stopY = centerY + (Math.sin(angle.toDouble()) * height).toFloat()
                    canvas.drawLine(centerX, centerY, stopX, stopY, paintPats)
                }
            } else {
                // Draw elegant overlay arcs/circles representing smooth organic ambient lines
                for (i in 1..4) {
                    val radius = (width * 0.2f * i)
                    canvas.drawCircle(width / 2f, height / 2f, radius, paintPats)
                }
            }

            // High Contrast Text Paint for headers
            val titlePaint = TextPaint().apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 52f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
                setShadowLayer(8f, 0f, 4f, 0x80000000.toInt())
            }

            val badgePaint = Paint().apply {
                color = 0x33000000
                style = Paint.Style.FILL
            }

            // Render Style Badge background
            canvas.drawRoundRect(80f, 80f, 440f, 160f, 40f, 40f, badgePaint)
            
            // Render Style Badge Text
            val badgeTextPaint = Paint().apply {
                color = 0xFAFFFFFF.toInt()
                textSize = 32f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                isAntiAlias = true
            }
            val styleIcon = when (styleName) {
                "Cyberpunk" -> "🌆"
                "Anime"      -> "🌸"
                "Cinematic" -> "🎬"
                "3D Render" -> "🧸"
                "Watercolor"-> "🖌️"
                "Pixel Art" -> "👾"
                "Comic Book"-> "💥"
                "Oil Painting"-> "🖼️"
                else        -> "📸"
            }
            canvas.drawText("$styleIcon $styleName Mode", 110f, 132f, badgeTextPaint)

            // Render Prompt text in neat vertical wrapping
            val promptPaint = TextPaint().apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 36f
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                isAntiAlias = true
                setShadowLayer(4f, 0f, 2f, 0x90000000.toInt())
            }

            val textWidth = width - 160
            val staticLayout = StaticLayout.Builder.obtain(prompt, 0, prompt.length, promptPaint, textWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.15f)
                .build()

            canvas.save()
            // Center the wrapped prompt vertically
            val textY = (height / 2f) - (staticLayout.height / 2f) + 80f
            canvas.translate(80f, textY)
            staticLayout.draw(canvas)
            canvas.restore()

            // Draw Logo Footer Brand watermark
            val footerPaint = Paint().apply {
                color = 0x80FFFFFF.toInt()
                textSize = 28f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
                letterSpacing = 0.2f
            }
            canvas.drawText("VisionGen AI • Local Synthesized Masterpiece", 80f, height - 120f, footerPaint)
            canvas.drawText("RESOLUTION: 1024x1024 px", 80f, height - 80f, footerPaint)

            return saveBitmapLocally(context, bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Save an MP4 video file into the device's public movies folder/gallery.
     */
    fun saveVideoToGallery(context: Context, videoPath: String, displayName: String = "AI_Video"): Boolean {
        val nameToSave = "${displayName.replace(" ", "_").take(30)}_${System.currentTimeMillis()}.mp4"
        val mimeType = "video/mp4"
        
        return try {
            val srcFile = File(videoPath)
            if (!srcFile.exists()) return false
            
            val contentResolver = context.contentResolver
            val videoUri: Uri?
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, nameToSave)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/AI Studio Generated")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                
                videoUri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (videoUri != null) {
                    contentResolver.openOutputStream(videoUri).use { outStream ->
                        if (outStream != null) {
                            srcFile.inputStream().use { inputStream ->
                                inputStream.copyTo(outStream)
                            }
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    contentResolver.update(videoUri, contentValues, null, null)
                }
            } else {
                val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString()
                val studioDir = File(moviesDir, "AI Studio Generated")
                if (!studioDir.exists()) {
                    studioDir.mkdirs()
                }
                val destFile = File(studioDir, nameToSave)
                srcFile.inputStream().use { inputStream ->
                    destFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DATA, destFile.absolutePath)
                    put(MediaStore.Video.Media.MIME_TYPE, mimeType)
                    put(MediaStore.Video.Media.DISPLAY_NAME, nameToSave)
                }
                contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
