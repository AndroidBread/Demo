package com.duyh.sudoku.view_model

import android.app.Application
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.googlecode.tesseract.android.TessBaseAPI
import com.googlecode.tesseract.android.TessBaseAPI.ProgressValues
import java.io.File
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val tessApi: TessBaseAPI
    private val processing = MutableLiveData(false)
    private val progress = MutableLiveData<String>()
    private val result = MutableLiveData<String>()
    var isInitialized = false
        private set

    @Volatile
    private var stopped = false

    @Volatile
    private var tessProcessing = false

    @Volatile
    private var recycleAfterProcessing = false
    private val recycleLock = Any()

    init {
        tessApi =
            TessBaseAPI { progressValues: ProgressValues -> progress.postValue("Progress: " + progressValues.percent + " %") }

        // Show Tesseract version and library flavor at startup
        progress.value = String.format(
            Locale.ENGLISH, "Tesseract %s (%s)",
            tessApi.version, tessApi.libraryFlavor
        )
    }

    override fun onCleared() {
        synchronized(recycleLock) {
            if (tessProcessing) {
                // Processing is active, set flag to recycle tessApi after processing is completed
                recycleAfterProcessing = true
                // Stop the processing as we don't care about the result anymore
                tessApi.stop()
            } else {
                // No ongoing processing, we must recycle it here
                tessApi.recycle()
            }
        }
    }

    fun initTesseract(dataPath: String, language: String, engineMode: Int) {

        try {
            Log.e(TAG, "dataPath: $dataPath${File.separator}")
            val file = File("$dataPath${File.separator}tessdata")
            if(!file.exists()){
               file.mkdirs()
            }
            this.isInitialized = tessApi.init("$dataPath${File.separator}", language, engineMode)
            Log.e(TAG, "this.isInitialized: ${this.isInitialized}")
        } catch (e: IllegalArgumentException) {
            this.isInitialized = false
            Log.e(TAG, "Cannot initialize Tesseract:", e)
        }
    }

    fun recognizeImage(imagePath: File) {
        if (!this.isInitialized) {
            Log.e(TAG, "recognizeImage: Tesseract is not initialized")
            return
        }
        if (tessProcessing) {
            Log.e(TAG, "recognizeImage: Processing is in progress")
            return
        }
        tessProcessing = true
        result.value = ""
        processing.value = true
        progress.value = "Processing..."
        stopped = false

        // Start process in another thread
        Thread {
            tessApi.setImage(imagePath)
            val startTime = SystemClock.uptimeMillis()

            tessApi.getHOCRText(0)

            val text = tessApi.utF8Text

            tessApi.clear()

            result.postValue(text)
            processing.postValue(false)
            if (stopped) {
                progress.postValue("Stopped.")
            } else {
                val duration = SystemClock.uptimeMillis() - startTime
                progress.postValue(
                    String.format(
                        Locale.ENGLISH,
                        "Completed in %.3fs.", duration / 1000f
                    )
                )
            }
            synchronized(recycleLock) {
                tessProcessing = false

                // Recycle the instance here if the view model is already destroyed
                if (recycleAfterProcessing) {
                    tessApi.recycle()
                }
            }
        }.start()
    }

    fun stop() {
        if (!tessProcessing) {
            return
        }
        progress.value = "Stopping..."
        stopped = true
        tessApi.stop()
    }

    fun getProcessing(): LiveData<Boolean> {
        return processing
    }

    fun getProgress(): LiveData<String> {
        return progress
    }

    fun getResult(): LiveData<String> {
        return result
    }

    companion object {
        private const val TAG = "MainViewModel"
    }


    fun autoFitNum(){

    }

}