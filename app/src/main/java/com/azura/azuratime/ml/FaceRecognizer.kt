package com.azura.azuratime.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import com.azura.azuratime.utils.ModelUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

object FaceRecognizer {
    private const val MODEL_NAME = "facenet.tflite" // use the float facenet model in assets
    private const val EMBEDDING_SIZE = 512 // facenet.tflite is usually 512-dim
    private lateinit var interpreter: Interpreter

    fun initialize(context: Context) {
        try {
            Log.d("FaceRecognizer", "[DEBUG] Entering initialize() for FaceRecognizer...")
            val modelBuffer = ModelUtils.loadModelFile(context, MODEL_NAME)
            Log.d("FaceRecognizer", "[DEBUG] Model file loaded, size: ${modelBuffer.limit()} bytes")
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            Log.d("FaceRecognizer", "[DEBUG] Creating TFLite Interpreter...")
            interpreter = Interpreter(modelBuffer, options)
            Log.d("FaceRecognizer", "[DEBUG] Interpreter created, allocating tensors...")
            interpreter.allocateTensors()
            val outputTensor = interpreter.getOutputTensor(0)
            val shape = outputTensor.shape()
            if (shape.contentEquals(intArrayOf(1, EMBEDDING_SIZE)).not()) {
                throw IllegalStateException("Unexpected model output shape: "+shape.joinToString())
            }
            Log.d("FaceRecognizer", "FaceRecognizer initialized successfully. Model output shape: ${shape.joinToString()}")
        } catch (e: Exception) {
            Log.e("FaceRecognizer", "Failed to initialize FaceRecognizer", e)
            throw e
        }
    }

    // Accepts normalized float input [0,1] and runs inference
    fun recognizeFace(input: FloatArray): FloatArray {
        if (!::interpreter.isInitialized) {
            Log.w("FaceRecognizer", "Interpreter not initialized, returning dummy embedding")
            return FloatArray(EMBEDDING_SIZE) { kotlin.random.Random.nextFloat() }
        }
        try {
            val inputBuffer = ByteBuffer.allocateDirect(input.size * 4).order(ByteOrder.nativeOrder())
            input.forEach { inputBuffer.putFloat(it) }
            inputBuffer.rewind()
            val outputBuffer = ByteBuffer.allocateDirect(EMBEDDING_SIZE * 4).order(ByteOrder.nativeOrder())
            outputBuffer.rewind()
            interpreter.run(inputBuffer, outputBuffer)
            outputBuffer.rewind()
            val emb = FloatArray(EMBEDDING_SIZE) { outputBuffer.float }
            // Optionally normalize
            val norm = sqrt(emb.fold(0f) { acc, v -> acc + v * v } + 1e-6f)
            val normalizedEmb = emb.map { it / norm }.toFloatArray()
            Log.d("FaceRecognizer", "[DEBUG] Embedding (first 10): ${normalizedEmb.take(10).joinToString(", ")}")
            return normalizedEmb
        } catch (e: Exception) {
            Log.e("FaceRecognizer", "Error during face recognition (float)", e)
            return FloatArray(EMBEDDING_SIZE) { kotlin.random.Random.nextFloat() }
        }
    }

    /**
     * Exports a list of face embeddings (with optional labels) to a CSV file.
     * Each row: label,embedding_1,embedding_2,...,embedding_n
     * @param context Context for file operations
     * @param embeddings List of Pair<label, embedding> to export
     * @param fileName Name of the CSV file to create (in app's files dir)
     * @return Absolute path to the exported file
     */
    fun exportEmbeddingsToCsv(
        context: Context,
        embeddings: List<Pair<String, FloatArray>>,
        fileName: String = "face_embeddings_export.csv"
    ): String {
        val file = context.getExternalFilesDir(null)?.resolve(fileName)
            ?: throw IllegalStateException("Cannot access external files directory")
        file.bufferedWriter().use { writer ->
            // Write header
            writer.write("label," + (1..EMBEDDING_SIZE).joinToString(",") { "emb$it" })
            writer.newLine()
            // Write each embedding
            for ((label, emb) in embeddings) {
                val line = buildString {
                    append(label)
                    append(",")
                    append(emb.joinToString(","))
                }
                writer.write(line)
                writer.newLine()
            }
        }
        Log.d("FaceRecognizer", "Exported ${embeddings.size} embeddings to ${file.absolutePath}")
        return file.absolutePath
    }

    fun isInitialized(): Boolean {
        return ::interpreter.isInitialized
    }

    fun close() {
        if (::interpreter.isInitialized) interpreter.close()
    }
}