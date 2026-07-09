package com.atlas.agent.files

import android.content.Context
import java.io.File

object LocalFileStore {
    data class LocalFileEntry(
        val name: String,
        val sizeBytes: Long,
        val isDirectory: Boolean
    )

    fun listDownloads(context: Context): List<LocalFileEntry> {
        val directory = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            ?: return emptyList()
        return directory.listFiles()
            ?.sortedBy { it.name }
            ?.map { file ->
                LocalFileEntry(
                    name = file.name,
                    sizeBytes = file.length(),
                    isDirectory = file.isDirectory
                )
            }
            ?: emptyList()
    }

    fun downloadFile(context: Context): File {
        val directory = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            ?: context.filesDir
        val file = File(directory, "atlas-download-${System.currentTimeMillis()}.txt")
        file.writeText("Atlas demo download created at ${System.currentTimeMillis()}")
        return file
    }

    fun uploadFile(context: Context): File {
        val directory = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            ?: context.filesDir
        val file = File(directory, "atlas-upload-${System.currentTimeMillis()}.txt")
        file.writeText("Atlas demo upload created at ${System.currentTimeMillis()}")
        return file
    }
}
