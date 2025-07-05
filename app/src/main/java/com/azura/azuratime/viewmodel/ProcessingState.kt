package com.azura.azuratime.viewmodel

import com.azura.azuratime.model.ProcessResult

data class ProcessingState(
    val isProcessing: Boolean = false,
    val status: String = "",
    val estimatedTime: String = "",
    val progress: Float = 0f,
    val currentPhotoType: String = "",
    val currentPhotoSize: String = "",
    val results: List<ProcessResult> = emptyList(),
    val successCount: Int = 0,
    val duplicateCount: Int = 0,
    val errorCount: Int = 0
)
