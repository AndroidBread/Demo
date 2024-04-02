package com.duyh.exoplayerdemo

import android.content.Context
import java.io.BufferedReader
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * 文件读取
 * date : 2020/11/05
 */
object AssetsFileUtils {

    fun getTermsString(context: Context, fileName: String?): String? {
        if (fileName.isNullOrEmpty()) return ""
        val reader: BufferedReader
        val content = StringBuilder() //文件内容字符串
        try {
            reader = BufferedReader(
                InputStreamReader(context.assets.open(fileName))
            )
            var line: String?
            //分行读取
            while (reader.readLine().also { line = it } != null) {
                content.append(line).append("\n")
            }
            reader.close()
            return content.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }


    /**
     * 将流拷贝到本地 然后返回本地file  默认拷贝到Files目录
     *
     * @param context
     * @param name
     * @return
     */
    fun getFile(context: Context, name: String?): File? {
        var `is`: InputStream? = null
        var os: FileOutputStream? = null
        try {
            val dir = context.filesDir
            val file = File(dir, name)
            return if (file.exists()) {
                file
            } else {
                file.createNewFile()
                os = FileOutputStream(file)
                `is` = context.assets.open(name!!)
                val buffer = ByteArray(1024)
                var bufferRead = 0
                while (`is`.read(buffer).also { bufferRead = it } != -1) {
                    os.write(buffer, 0, bufferRead)
                }
                os.flush()
                `is`.close()
                os.close()
                file
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            close(`is`, os)
        }
        return null
    }


    /**
     * 关闭流
     *
     * @param is
     */
    private fun close(vararg `is`: Closeable?) {
        for (i in `is`) {
            try {
                i?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}