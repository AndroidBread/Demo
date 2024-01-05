package com.duyh.navigationtest

import java.io.BufferedReader
import java.io.InputStreamReader

object AdbUtils {

    /**
     * 执行shell 命令， 命令中不必再带 adb shell
     * @param cmd
     * @return Sting  命令执行在控制台输出的结果
     */
    fun execByRuntime(cmd: String?): String? {
        var process: Process? = null
        var bufferedReader: BufferedReader? = null
        var inputStreamReader: InputStreamReader? = null
        return try {
            process = Runtime.getRuntime().exec(cmd)
            inputStreamReader = InputStreamReader(process.inputStream)
            bufferedReader = BufferedReader(inputStreamReader)
            var read: Int
            val buffer = CharArray(4096)
            val output = StringBuilder()
            while (bufferedReader.read(buffer).also { read = it } > 0) {
                output.appendRange(buffer, 0, read)
            }
            output.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            if (null != inputStreamReader) {
                try {
                    inputStreamReader.close()
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
            if (null != bufferedReader) {
                try {
                    bufferedReader.close()
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
            if (null != process) {
                try {
                    process.destroy()
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
    }
}