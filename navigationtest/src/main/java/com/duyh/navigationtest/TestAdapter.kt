package com.duyh.navigationtest

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.duyh.navigationtest.databinding.ItemTestBinding

class TestAdapter : BaseQuickAdapter<String, TestAdapter.VH>() {


    class VH(
        parent: ViewGroup,
        val binding: ItemTestBinding = ItemTestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: String?) {
        holder.binding.tvTest.text = item.toString()
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH = VH(parent)

}