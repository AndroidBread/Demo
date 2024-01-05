package com.duyh.navigationtest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseSingleItemAdapter
import com.chad.library.adapter.base.fullspan.FullSpanAdapterType

class HeaderAdapter: BaseSingleItemAdapter<Any, HeaderAdapter.VH>(), FullSpanAdapterType {

    class VH(view: View): RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.head_view, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, item: Any?) {

    }
}