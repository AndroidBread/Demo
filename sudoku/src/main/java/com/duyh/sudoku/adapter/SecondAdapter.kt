package com.duyh.sudoku.adapter

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.QuickViewHolder
import com.duyh.sudoku.R
import com.duyh.sudoku.decoration.CustomDecoration
import com.duyh.sudoku.decoration.CustomDecoration.Companion.HORIZONTAL_LIST
import com.duyh.sudoku.entity.SudokuEntity

class SecondAdapter: BaseQuickAdapter<MutableList<SudokuEntity> , QuickViewHolder>() {



    override fun onBindViewHolder(
        holder: QuickViewHolder,
        position: Int,
        item: MutableList<SudokuEntity>?
    ) {
        val rvSecond = holder.getView<RecyclerView>(R.id.rv_second)
        val mAdapter = ItemAdapter()
        rvSecond.adapter = mAdapter
        rvSecond.layoutManager = GridLayoutManager(context , 3)
        val decoration1 = CustomDecoration(context, CustomDecoration.VERTICAL_LIST, R.drawable.shape_item_decoration_v)
        val decoration = CustomDecoration(context, HORIZONTAL_LIST, R.drawable.shape_item_decoration)
        rvSecond.addItemDecoration(decoration)
        rvSecond.addItemDecoration(decoration1)
        Log.e("Sudoku" , "item: ${item.toString()}")
        mAdapter.submitList(item)
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_second_list , parent)
    }
}