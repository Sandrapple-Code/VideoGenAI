package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.io.File
import java.nio.ByteBuffer

object VideoCreator {

    private const val TAG = "VideoCreator"

    /**
     * Creates a high-fidelity cinematic MP4 video of the given Bitmap,
     * applying slow kinetic zoom and pan transformations over totalFrames.
     */
    fun createVideoFromBitmap(originalBitmap: Bitmap, outputFile: File, ratio: String = "1:1"): Boolean {
        // Dynamic target video size - must be multiples of 16 for standard hardware encoders!
        val (targetWidth, targetHeight) = when (ratio) {
            "16:9" -> Pair(1024, 576) // 1024 / 16 = 64, 576 / 16 = 36
            "9:16" -> Pair(576, 1024)
            "4:3"  -> Pair(960, 720)  // 960 / 16 = 60, 720 / 16 = 45
            "3:4"  -> Pair(720, 960)
            else   -> Pair(768, 768)  // 1:1, 768 / 16 = 48
        }

        val frameRate = 30
        val totalSeconds = 3
        val totalFrames = frameRate * totalSeconds // 90 frames total
        val bitRate = 2500000 // 2.5 Mbps
        
        var codec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        
        return try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, targetWidth, targetHeight).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) // NV12
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()

            // Prepare directory
            outputFile.parentFile?.mkdirs()
            if (outputFile.exists()) {
                outputFile.delete()
            }

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var trackIndex = -1
            var muxerStarted = false
            
            val bufferInfo = MediaCodec.BufferInfo()
            var frameIndex = 0

            while (frameIndex < totalFrames) {
                // Dequeue input buffer
                val inputBufferIndex = codec.dequeueInputBuffer(15000)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = codec.getInputBuffer(inputBufferIndex)!!
                    inputBuffer.clear()

                    // Generate cinematic pan/zoom bitmap frame
                    val frameBitmap = generateCinematicFrame(originalBitmap, targetWidth, targetHeight, frameIndex, totalFrames)
                    val nv12Bytes = getNV12(targetWidth, targetHeight, frameBitmap)
                    frameBitmap.recycle()

                    inputBuffer.put(nv12Bytes)

                    val presentationTimeUs = (frameIndex * 1000000L) / frameRate
                    codec.queueInputBuffer(
                        inputBufferIndex,
                        0,
                        nv12Bytes.size,
                        presentationTimeUs,
                        if (frameIndex == totalFrames - 1) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0
                    )
                    frameIndex++
                }

                // Dequeue and write output
                var outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 2000)
                while (outputBufferIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputBufferIndex)!!
                    
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                        bufferInfo.size = 0
                    }

                    if (bufferInfo.size > 0) {
                        if (!muxerStarted) {
                            val newFormat = codec.outputFormat
                            trackIndex = muxer.addTrack(newFormat)
                            muxer.start()
                            muxerStarted = true
                        }
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, outputBuffer, bufferInfo)
                    }

                    codec.releaseOutputBuffer(outputBufferIndex, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        break
                    }
                    outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 2000)
                }
            }

            // Drain remaining frames
            var endOfStream = false
            var tryCount = 0
            while (!endOfStream && tryCount < 30) {
                val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 5000)
                if (outputBufferIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputBufferIndex)!!
                    if (bufferInfo.size > 0 && muxerStarted) {
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, outputBuffer, bufferInfo)
                    }
                    codec.releaseOutputBuffer(outputBufferIndex, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        endOfStream = true
                    }
                } else if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    tryCount++
                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // Format changed midway, process
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error encoding video: ${e.message}", e)
            false
        } finally {
            try {
                codec?.stop()
                codec?.release()
            } catch (e: Exception) { e.printStackTrace() }
            
            try {
                if (muxer != null) {
                    muxer.stop()
                    muxer.release()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun generateCinematicFrame(src: Bitmap, targetW: Int, targetH: Int, index: Int, total: Int): Bitmap {
        // 1. Create a base cropped centered frame bitmap
        val baseFrame = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val baseCanvas = Canvas(baseFrame)

        val srcRatio = src.width.toFloat() / src.height.toFloat()
        val targetRatio = targetW.toFloat() / targetH.toFloat()
        
        var baseScale = 1.0f
        var dx = 0f
        var dy = 0f
        
        if (srcRatio > targetRatio) {
            baseScale = targetH.toFloat() / src.height.toFloat()
            dx = (targetW - src.width * baseScale) / 2f
        } else {
            baseScale = targetW.toFloat() / src.width.toFloat()
            dy = (targetH - src.height * baseScale) / 2f
        }
        
        val matrix = Matrix()
        matrix.postScale(baseScale, baseScale)
        matrix.postTranslate(dx, dy)

        val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }
        baseCanvas.drawBitmap(src, matrix, paint)

        // 2. Prepare destination warped frame
        val dest = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(dest)

        // 3. Define mesh parameters
        val meshW = 40
        val meshH = 40
        val count = (meshW + 1) * (meshH + 1)
        val verts = FloatArray(count * 2)

        val stepX = targetW.toFloat() / meshW
        val stepY = targetH.toFloat() / meshH

        val progress = index.toFloat() / total.toFloat()
        
        // Stride / Trot harmonic arguments (multiple strides over the video length)
        val strideCycles = 3.5f
        val strideAngle = progress * 2f * Math.PI.toFloat() * strideCycles
        
        // Fluid ripple oscillation for wind, water or sky elements
        val fluidCycles = 1.2f
        val fluidAngle = progress * 2f * Math.PI.toFloat() * fluidCycles

        var vIdx = 0
        for (row in 0..meshH) {
            val origY = row * stepY
            for (col in 0..meshW) {
                val origX = col * stepX

                // Calculate center-weighted radial factor for primary subjects (like animals or people)
                val cx = targetW / 2f
                val cy = targetH / 2f
                val rx = origX - cx
                val ry = origY - cy
                val dist = Math.sqrt((rx * rx + ry * ry).toDouble()).toFloat()
                
                // Active radius for subjects: drops smoothly outside the focal circle
                val maxRadius = Math.min(targetW, targetH) * 0.44f
                val subjectWeight = Math.max(0f, 1f - (dist / maxRadius)).let { it * it }

                // Determine relative vertical offset (-1.0 to +1.0) relative to focal center
                val relativeY = (origY - cy) / (targetH / 2f)
                
                // 1. Kinetic Stride Engine (horizontal shearing based on height mimics running strides/legs bending)
                val strideX = Math.sin((strideAngle + relativeY * Math.PI.toFloat() * 1.3f).toDouble()).toFloat() * 26f * subjectWeight
                
                // Vertical muscular cycle oscillation (up and down body trot motion)
                val strideY = Math.abs(Math.sin((strideAngle * 2f).toDouble())).toFloat() * -12f * subjectWeight + 
                              Math.sin((strideAngle + (origX / targetW) * Math.PI.toFloat()).toDouble()).toFloat() * 6f * subjectWeight

                // 2. Continuous Fluid Flow Engine (gentle dynamic warp waves on scenery)
                val backgroundWeight = 1f - subjectWeight * 0.6f
                val fluidX = Math.sin((fluidAngle + (origY * 0.016f)).toDouble()).toFloat() * 12f * backgroundWeight
                val fluidY = Math.cos((fluidAngle + (origX * 0.016f)).toDouble()).toFloat() * 12f * backgroundWeight

                // Combined spatial translation
                val warpedX = origX + strideX + fluidX
                val warpedY = origY + strideY + fluidY

                // Snug border protection: keep edges perfectly pinned to avoid visual black frame slivers
                val edgeBuffer = 14f
                val finalX = when {
                    origX < edgeBuffer -> origX
                    origX > targetW - edgeBuffer -> origX
                    else -> Math.max(0f, Math.min(targetW.toFloat(), warpedX))
                }
                val finalY = when {
                    origY < edgeBuffer -> origY
                    origY > targetH - edgeBuffer -> origY
                    else -> Math.max(0f, Math.min(targetH.toFloat(), warpedY))
                }

                verts[vIdx++] = finalX
                verts[vIdx++] = finalY
            }
        }

        // Draw beautifully warped mesh onto destination!
        canvas.drawBitmapMesh(baseFrame, meshW, meshH, verts, 0, null, 0, paint)

        // Clean up intermediate unwarped frame
        baseFrame.recycle()

        return dest
    }

    private fun getNV12(width: Int, height: Int, bitmap: Bitmap): ByteArray {
        val size = width * height
        val yuv = ByteArray(size * 3 / 2)

        val argb = IntArray(size)
        bitmap.getPixels(argb, 0, width, 0, 0, width, height)

        var yIndex = 0
        var uvIndex = size

        for (j in 0 until height) {
            for (i in 0 until width) {
                val color = argb[j * width + i]
                val r = (color shr 16) and 0xff
                val g = (color shr 8) and 0xff
                val b = color and 0xff

                // YUV Formula coefficients mapped precisely
                var y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                var u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                var v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128

                y = Math.max(16, Math.min(y, 235))
                u = Math.max(16, Math.min(u, 240))
                v = Math.max(16, Math.min(v, 240))

                yuv[yIndex++] = y.toByte()

                // NV12 format requires V first then U interlaced on even lines and even pixels
                if (j % 2 == 0 && i % 2 == 0) {
                    yuv[uvIndex++] = v.toByte()
                    yuv[uvIndex++] = u.toByte()
                }
            }
        }
        return yuv
    }
}
