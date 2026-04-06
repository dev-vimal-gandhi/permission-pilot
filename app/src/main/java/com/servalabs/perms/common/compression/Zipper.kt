package com.servalabs.perms.common.compression

import com.servalabs.perms.common.debug.logging.Logging.Priority.VERBOSE
import com.servalabs.perms.common.debug.logging.log
import com.servalabs.perms.common.debug.logging.logTag
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

// https://stackoverflow.com/a/48598099/1251958
class Zipper {

    @Throws(Exception::class)
    fun zip(files: List<String>, zipFile: String) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { out ->
            for (filePath in files) {
                log(TAG, VERBOSE) { "Compressing $filePath into $zipFile" }
                val origin = BufferedInputStream(FileInputStream(filePath), BUFFER)

                val entry = ZipEntry(filePath.substring(filePath.lastIndexOf("/") + 1))
                out.putNextEntry(entry)

                origin.use { input -> input.copyTo(out) }
            }
            out.finish()
        }
    }

    companion object {
        internal val TAG = logTag("Zipper")
        const val BUFFER = 2048
    }
}