package ru.hippo.M2.music

data class TrackItem(
    val id: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val filePath: String,
    val artworkPath: String?,
    val addedAt: Long,
    val isFavorite: Boolean
)

data class ImportResult(
    val added: Int,
    val failed: Int
)
