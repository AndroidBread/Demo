package com.duyh.sudoku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.QuickAdapterHelper
import com.chad.library.adapter.base.loadState.LoadState
import com.chad.library.adapter.base.loadState.trailing.TrailingLoadStateAdapter
import com.duyh.sudoku.adapter.ItemAdapter
import com.duyh.sudoku.databinding.ActivityRecyclerViewLoadMoreAcivityBinding
import com.duyh.sudoku.decoration.CustomDecoration
import com.duyh.sudoku.entity.SudokuEntity

class RecyclerViewLoadMoreActivity : AppCompatActivity() {

    lateinit var binding: ActivityRecyclerViewLoadMoreAcivityBinding

    val adapter by lazy {
        ItemAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_recycler_view_load_more_acivity)


        val helper = QuickAdapterHelper.Builder(adapter)
            // 使用默认样式的尾部"加载更多"
            .setTrailingLoadStateAdapter(object : TrailingLoadStateAdapter.OnTrailingListener {
                override fun onLoad() {
                    // 执行加载更多的操作，通常都是网络请求
                    adapter.addAll(initData())
                }

                override fun onFailRetry() {
                    // 加载失败后，点击重试的操作，通常都是网络请求

                }

                override fun isAllowLoading(): Boolean {
                    // 是否允许触发“加载更多”，通常情况下，下拉刷新的时候不允许进行加载更多
                    return false
                }
            }).build()
        helper.trailingLoadState = LoadState.NotLoading(false)
        binding.rvTest.adapter = helper.adapter
        binding.rvTest.layoutManager = GridLayoutManager(baseContext, 3)
        val decoration1 = CustomDecoration(
            baseContext,
            CustomDecoration.VERTICAL_LIST,
            R.drawable.shape_item_decoration_v
        )
        val decoration = CustomDecoration(
            baseContext,
            CustomDecoration.HORIZONTAL_LIST,
            R.drawable.shape_item_decoration
        )
        binding.rvTest.addItemDecoration(decoration)
        binding.rvTest.addItemDecoration(decoration1)
        adapter.submitList(initData())
    }

    fun initData(): MutableList<SudokuEntity> {
        val countData: MutableList<SudokuEntity> = arrayListOf()
        var num = 1;
        for (i in 0..6) {
            for (k in 0..6) {
                countData.add(SudokuEntity(i, k, num))
                num++
            }
        }
        return countData
    }
}