package com.duyh.navigationtest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.fullspan.FullSpanAdapterType
import com.chad.library.adapter.base.loadState.LoadState
import com.chad.library.adapter.base.loadState.trailing.TrailingLoadStateAdapter
import com.duyh.navigationtest.databinding.ViewLoadMoreBinding

class CustomLoadMoreAdapter : TrailingLoadStateAdapter<CustomLoadMoreAdapter.CustomVH>(isLoadEndDisplay = true),
    FullSpanAdapterType {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): CustomVH {
        // 创建你自己的 UI 布局
        val viewBinding = ViewLoadMoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomVH(viewBinding).apply {
            viewBinding.tvFail.setOnClickListener {
                // 失败重试点击事件
                invokeFailRetry()
            }
            viewBinding.tvSuccess.setOnClickListener {
                // 加载更多，手动点击事件
                invokeLoadMore()
            }
        }
    }

    override fun onBindViewHolder(holder: CustomVH, loadState: LoadState) {
        // 根据加载状态，来自定义你的 UI 界面
        when (loadState) {
            is LoadState.NotLoading -> {
                if (loadState.endOfPaginationReached) {
                    holder.viewBinding.tvSuccess.visibility = View.GONE
                    holder.viewBinding.tvLoading.visibility = View.GONE
                    holder.viewBinding.tvFail.visibility = View.GONE
                    holder.viewBinding.tvEnd.visibility = View.VISIBLE
                } else {
                    holder.viewBinding.tvSuccess.visibility = View.VISIBLE
                    holder.viewBinding.tvLoading.visibility = View.GONE
                    holder.viewBinding.tvFail.visibility = View.GONE
                    holder.viewBinding.tvEnd.visibility = View.GONE
                }
            }
            is LoadState.Loading -> {
                holder.viewBinding.tvSuccess.visibility = View.GONE
                holder.viewBinding.tvLoading.visibility = View.VISIBLE
                holder.viewBinding.tvFail.visibility = View.GONE
                holder.viewBinding.tvEnd.visibility = View.GONE
            }
            is LoadState.Error -> {
                holder.viewBinding.tvSuccess.visibility = View.GONE
                holder.viewBinding.tvLoading.visibility = View.GONE
                holder.viewBinding.tvFail.visibility = View.VISIBLE
                holder.viewBinding.tvEnd.visibility = View.GONE
            }
            is LoadState.None -> {
                holder.viewBinding.tvSuccess.visibility = View.GONE
                holder.viewBinding.tvLoading.visibility = View.GONE
                holder.viewBinding.tvFail.visibility = View.GONE
                holder.viewBinding.tvEnd.visibility = View.GONE
            }
        }
    }


    class CustomVH(val viewBinding: ViewLoadMoreBinding) : RecyclerView.ViewHolder(viewBinding.root)
}