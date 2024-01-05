package com.duyh.sudoku

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recording
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.loader.content.CursorLoader
import com.duyh.sudoku.databinding.ActivityTakePhotoBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class TakePhotoActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var recording: Recording? = null
    private lateinit var binding: ActivityTakePhotoBinding
    private var photoPath: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_take_photo)
        showHide(false)
        startCamera()
        cameraExecutor = Executors.newSingleThreadExecutor()
        initClick()
    }

    private fun initClick() {
        binding.btnTakePhoto.setOnClickListener {
            takePhoto()
        }

        binding.btnReTake.setOnClickListener {
            showHide(false)
        }

        binding.btnBackPhoto.setOnClickListener {
            setResult(RESULT_OK , Intent().apply {
                data = photoPath
            })
            finish()
        }
    }

    private fun showHide(takeOver: Boolean){
        if(takeOver){
            binding.btnTakePhoto.visibility = View.GONE
            binding.pvFinder.visibility = View.GONE
            binding.btnReTake.visibility = View.VISIBLE
            binding.btnBackPhoto.visibility = View.VISIBLE
            binding.ivTakeResult.visibility = View.VISIBLE
        } else {
            binding.btnTakePhoto.visibility = View.VISIBLE
            binding.pvFinder.visibility = View.VISIBLE
            binding.btnReTake.visibility = View.GONE
            binding.btnBackPhoto.visibility = View.GONE
            binding.ivTakeResult.visibility = View.GONE
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)

        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        binding.pvFinder.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        binding.pvFinder.scaleType = PreviewView.ScaleType.FIT_CENTER
        val preview = Preview.Builder()
            .build()

        if (imageCapture == null) {
            imageCapture = ImageCapture.Builder()
                .setTargetRotation(binding.pvFinder.display.rotation)
                .build()
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(720, 720))
            .build()

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(binding.pvFinder.surfaceProvider)


        val viewPort =  ViewPort.Builder(Rational(720, 720), binding.pvFinder.display.rotation).build()
        val useCaseGroup = UseCaseGroup.Builder()
            .addUseCase(preview)
            .addUseCase(imageAnalysis)
            .addUseCase(imageCapture!!)
            .setViewPort(viewPort)
            .build()

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, useCaseGroup)

    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


    private fun takePhoto() {
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                @RequiresApi(Build.VERSION_CODES.R)
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Log.e(TAG, msg)
                    photoPath = output.savedUri

                    val file = photoPath?.let { getRealPathFromURI(it) }
                    showHide(true)
                    binding.ivTakeResult.setImageBitmap(BitmapFactory.decodeFile(file))
                }
            }
        )
    }

    fun getRealPathFromURI(contentUri: Uri): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val loader = CursorLoader(baseContext, contentUri, proj, null, null, null)
        val cursor: Cursor? = loader.loadInBackground()
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)?:0
        cursor?.moveToFirst()
        val result = cursor?.getString(columnIndex)
        cursor?.close()
        return result
    }
}