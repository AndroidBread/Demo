package com.duyh.sudoku.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.QuickViewHolder
import com.duyh.sudoku.R
import com.duyh.sudoku.entity.SudokuEntity

class ItemAdapter: BaseQuickAdapter<SudokuEntity , QuickViewHolder>() {

    override fun onBindViewHolder(
        holder: QuickViewHolder,
        position: Int,
        item: SudokuEntity?
    ) {
        val edtNum = holder.getView<EditText>(R.id.tv_item)
        edtNum.setText(item?.num.toString())
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sudoku_num, parent, false)
        return QuickViewHolder(view)
    }
}