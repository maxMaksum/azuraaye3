package com.azura.azuratime.model

data class ProcessResult(
    val studentId: String,
    val name: String,
    val status: String,
    val photoSize: Long = 0,
    val error: String? = null
)
