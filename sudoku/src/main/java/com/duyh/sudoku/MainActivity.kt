package com.duyh.sudoku

import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.ACTION_PICK_IMAGES
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.loader.content.CursorLoader
import androidx.recyclerview.widget.GridLayoutManager
import com.duyh.sudoku.adapter.SecondAdapter
import com.duyh.sudoku.config.Config
import com.duyh.sudoku.databinding.ActivityMainBinding
import com.duyh.sudoku.decoration.CustomDecoration
import com.duyh.sudoku.decoration.CustomDecoration.Companion.HORIZONTAL_LIST
import com.duyh.sudoku.decoration.CustomDecoration.Companion.VERTICAL_LIST
import com.duyh.sudoku.entity.SudokuEntity
import com.duyh.sudoku.utils.Assets
import com.duyh.sudoku.view_model.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import java.io.File

class MainActivity : AppCompatActivity() {

    private val mViewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private val secondAdapter: SecondAdapter by lazy {
        SecondAdapter()
    }

    private val bottomSheetDialog by lazy {
        BottomSheetDialog(this)
    }

    private lateinit var registerForActivityResult: ActivityResultLauncher<Intent>
    private lateinit var registerForActivityResult1: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(
            this, R.layout.activity_main)
        mViewModel.initTesseract(Assets.getTessDataPath(baseContext), Config.TESS_LANG, Config.TESS_ENGINE)
        initView(binding)
        initClick(binding)
    }

    private fun initView(binding: ActivityMainBinding) {
        binding.rvSudokuList.adapter = secondAdapter
        binding.rvSudokuList.layoutManager = GridLayoutManager(this , 3)
        val decoration1 = CustomDecoration(this, VERTICAL_LIST, R.drawable.shape_decoration_v)
        val decoration = CustomDecoration(this, HORIZONTAL_LIST, R.drawable.shape_decoration)
        binding.rvSudokuList.addItemDecoration(decoration1)
        binding.rvSudokuList.addItemDecoration(decoration)
        secondAdapter.submitList(initData())


        val inflate = LayoutInflater.from(this).inflate(R.layout.sheet_pic_select, null)

        inflate.findViewById<TextView>(R.id.tv_select_pic).setOnClickListener {
            checkPermission{
                if(it){
                    toSelectPic()
                } else {
                    Log.e("Sudoku", "权限被拒绝了")
                }
            }
        }

        inflate.findViewById<TextView>(R.id.tv_take_photo).setOnClickListener {
            checkPermission{
                if(it){
                    takePhoto()
                } else {
                    Log.e("Sudoku", "权限被拒绝了")
                }
            }
        }

        inflate.findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.setContentView(inflate)
    }

    private fun checkPermission(callback: (Boolean)-> Unit) {
        val checkPermissions = arrayListOf(Permission.READ_MEDIA_IMAGES, Permission.CAMERA)
        val haveRead = XXPermissions.isGranted(this, checkPermissions)
        if (haveRead) {
            callback.invoke(true)
        } else {
            XXPermissions.with(this).permission(checkPermissions)
                .request { permissions, allGranted ->
                    callback.invoke(allGranted)
                }
        }
    }

    private fun toSelectPic(){
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent(ACTION_PICK_IMAGES)
        } else {
            Intent(ACTION_PICK)
        }
        intent.type = "image/*"
        registerForActivityResult.launch(intent)
        bottomSheetDialog.dismiss()
    }

    private fun takePhoto(){
//        val intent = Intent(this, TakePhotoActivity::class.java)
//        registerForActivityResult1.launch(intent)
        val intent = Intent(this, RecyclerViewLoadMoreActivity::class.java)
        startActivity(intent)
        bottomSheetDialog.dismiss()
    }

    private fun initClick(binding: ActivityMainBinding){
        binding.btnImportPic.setOnClickListener {
            showSheet()
        }

        binding.ivShowPic.setOnClickListener {
            binding.ivShowPic.visibility = View.GONE
        }

        registerForActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val data = it.data
                    val oldPath = data?.data?.path?:""
                    val path = if(oldPath.contains("/raw/")){
                        oldPath.replace("/raw/", "")
                    } else {
                        oldPath
                    }
                    Log.e("Sudoku", "path : $path")
                    Log.e("Sudoku", "data : $data")
                    binding.ivShowPic.visibility = View.VISIBLE
                    binding.ivShowPic.setImageBitmap(BitmapFactory.decodeFile(path))
                    mViewModel.recognizeImage(File(path))
                }
            }

        registerForActivityResult1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val data = it.data
                if(data?.data != null){
                    binding.ivShowPic.visibility = View.VISIBLE
                    val picPath = getRealPathFromURI(data.data!!)
                    Log.e("Sudoku", "path : $picPath")
                    Log.e("Sudoku", "data : $data")
                    binding.ivShowPic.setImageBitmap(BitmapFactory.decodeFile(picPath))
                    mViewModel.recognizeImage(File(picPath.toString()))
                }
            }
        }

        mViewModel.getProcessing().observe(this) { processing ->
            Log.e("Sudoku", "processing: $processing")
        }
        mViewModel.getProgress().observe(this) { progress ->
            Log.e("Sudoku", "progress: $progress")
        }
        mViewModel.getResult().observe(this) { result ->
            Log.e("Sudoku", "result: $result")
        }
    }

    private fun showSheet(){
        bottomSheetDialog.show()
    }


    private fun initData(): MutableList<MutableList<SudokuEntity>>{
        val countData: MutableList<SudokuEntity> = arrayListOf()
        var num = 1;
        for (i in 0 .. 8){
            for (k in 0 .. 8){
                countData.add(SudokuEntity(i, k, num))
                num++
            }
        }
        val allData: MutableList<MutableList<SudokuEntity>> = arrayListOf()

        for (i in 0 .. 8){
            val itemData: MutableList<SudokuEntity> = arrayListOf()
            val minY = i % 3 * 3
            val maxY = i % 3 * 3 + 2
            val minX = i / 3 * 3
            val maxX = i / 3 * 3 + 2

            countData.forEach {
                if(it.x in minX .. maxX && it.y in minY..maxY){
                    itemData.add(it)
                }
            }
            allData.add(itemData)
        }
        Log.e("Sudoku" , allData.toString())
        return allData
    }

    private fun getRealPathFromURI(contentUri: Uri): String? {
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