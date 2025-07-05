package com.azura.azuratime.utils

/**
 * Computes the cosine distance between two float arrays (embeddings).
 * Returns a value between 0 (identical) and 2 (opposite).
 */
fun cosineDistance(a: FloatArray, b: FloatArray): Float {
    require(a.size == b.size) { "Arrays must be of the same length" }
    var dot = 0f
    var normA = 0f
    var normB = 0f
    for (i in a.indices) {
        dot += a[i] * b[i]
        normA += a[i] * a[i]
        normB += b[i] * b[i]
    }
    val denominator = (Math.sqrt(normA.toDouble()) * Math.sqrt(normB.toDouble())).toFloat()
    return if (denominator == 0f) 1f else 1f - (dot / denominator)
}
