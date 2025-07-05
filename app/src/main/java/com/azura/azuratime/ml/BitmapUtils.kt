package com.azura.azuratime.ml

import android.graphics.*
import android.media.Image
import androidx.core.graphics.scale
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.io.ByteArrayOutputStream

object BitmapUtils {
    private const val INPUT_SIZE = 160  // or your FaceNet input
    private const val BYTES_PER_CHANNEL = 4

    /** Convert YUV Image → RGB Bitmap → crop→rotate→resize→FloatArray */
    fun preprocessFace(image: Image, boundingBox: Rect, rotation: Int): FloatArray {
        // 1) Convert to Bitmap (YUV→RGB)
        val bitmap = yuvToRgb(image)

        // 2) Rotate if needed
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // 3) Crop the face
        val left   = boundingBox.left.coerceAtLeast(0)
        val top    = boundingBox.top.coerceAtLeast(0)
        val width  = boundingBox.width().coerceAtMost(rotated.width  - left)
        val height = boundingBox.height().coerceAtMost(rotated.height - top)
        val cropped = Bitmap.createBitmap(rotated, left, top, width, height)

        // 4) Resize to model input size
        val inputSize = INPUT_SIZE
        val resized = Bitmap.createScaledBitmap(cropped, inputSize, inputSize, true)

        // 5) Convert to normalized FloatArray (0..1 for uint8 quant, or -1..1 for int8 quant)
        val floatArray = FloatArray(inputSize * inputSize * 3)
        val intVals = IntArray(inputSize * inputSize)
        resized.getPixels(intVals, 0, inputSize, 0, 0, inputSize, inputSize)
        for (i in intVals.indices) {
            val pixel = intVals[i]
            // Normalize to [0,1] for uint8 quantized model
            floatArray[i * 3 + 0] = ((pixel shr 16 and 0xFF) / 255.0f)
            floatArray[i * 3 + 1] = ((pixel shr 8 and 0xFF) / 255.0f)
            floatArray[i * 3 + 2] = ((pixel and 0xFF) / 255.0f)
        }
        return floatArray
    }

    /** Extract face bitmap from image with rotation and cropping */
    fun extractFaceBitmap(image: Image, boundingBox: Rect, rotation: Int): Bitmap {
        // 1) Convert YUV image to RGB bitmap
        val bitmap = yuvToRgb(image)

        // 2) Rotate the image by the given angle
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // 3) Crop the face area using the bounding box
        val left   = boundingBox.left.coerceAtLeast(0)
        val top    = boundingBox.top.coerceAtLeast(0)
        val width  = boundingBox.width().coerceAtMost(rotated.width - left)
        val height = boundingBox.height().coerceAtMost(rotated.height - top)

        // 4) Return the cropped face bitmap
        return Bitmap.createBitmap(rotated, left, top, width, height)
    }

    /** Simple YUV to RGB conversion via ScriptIntrinsicYuvToRGB */
    fun yuvToRgb(image: Image): Bitmap {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)

        // U and V are swapped
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val jpg = out.toByteArray()
        return BitmapFactory.decodeByteArray(jpg, 0, jpg.size)
    }

    // For best performance, consider moving image preprocessing (resize, normalization) to native C++ via JNI if not already done.
}
