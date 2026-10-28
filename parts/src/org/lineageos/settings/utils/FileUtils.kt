package org.lineageos.settings.utils

import android.util.Log
import java.io.File

object FileUtils {
    private const val TAG = "FileUtils"

    fun readOneLine(fileName: String): String? = try {
        File(fileName).bufferedReader().use { it.readLine() }
    } catch (e: Exception) {
        Log.w(TAG, "Could not read $fileName", e)
        null
    }

    fun writeLine(fileName: String, value: String): Boolean = try {
        File(fileName).bufferedWriter().use { it.write(value) }
        true
    } catch (e: Exception) {
        Log.w(TAG, "Could not write $fileName", e)
        false
    }

    fun fileExists(fileName: String) = File(fileName).exists()
    fun isFileReadable(fileName: String) = File(fileName).let { it.exists() && it.canRead() }
    fun isFileWritable(fileName: String) = File(fileName).let { it.exists() && it.canWrite() }
    fun delete(fileName: String) = runCatching { File(fileName).delete() }.onFailure { Log.w(TAG, "Could not delete $fileName", it) }.getOrDefault(false)
    fun rename(srcPath: String, dstPath: String) = runCatching { File(srcPath).renameTo(File(dstPath)) }.onFailure { Log.w(TAG, "Could not rename $srcPath to $dstPath", it) }.getOrDefault(false)
}
