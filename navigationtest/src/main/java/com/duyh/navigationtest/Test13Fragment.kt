package com.duyh.navigationtest

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.QuickAdapterHelper
import com.chad.library.adapter.base.layoutmanager.QuickGridLayoutManager
import com.chad.library.adapter.base.loadState.LoadState
import com.chad.library.adapter.base.loadState.trailing.TrailingLoadStateAdapter
import com.duyh.navigationtest.databinding.FragmentTest13Binding

class Test13Fragment : Fragment() {

    internal class PageInfo {
        var page = 0
        fun nextPage() {
            page++
        }

        fun reset() {
            page = 0
        }

        val isFirstPage: Boolean
            get() = page == 0
    }

    private val pageInfo = PageInfo()
    val binding by lazy {  FragmentTest13Binding.inflate(layoutInflater) }


    private var num = 0
    private lateinit var helper :QuickAdapterHelper

    private val mAdapter: TestAdapter by lazy {
        TestAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.rvTest.layoutManager = QuickGridLayoutManager(requireContext(), 2)
        // 实例化自定义"加载更多"的类
        val loadMoreAdapter = CustomLoadMoreAdapter()

        loadMoreAdapter.isAutoLoadMore = false
        loadMoreAdapter.setOnLoadMoreListener(object : TrailingLoadStateAdapter.OnTrailingListener {
            override fun onLoad() {
                request()
            }

            override fun onFailRetry() {
                request()
            }

            override fun isAllowLoading(): Boolean {
                // 是否允许触发“加载更多”，通常情况下，下拉刷新的时候不允许进行加载更多
                return !binding.refreshLayout.isRefreshing
            }
        })
        helper = QuickAdapterHelper.Builder(mAdapter)
            .setTrailingLoadStateAdapter(loadMoreAdapter) // 传递自定义的“加载跟多”
            .build()
        binding.rvTest.adapter = helper.adapter
        initRefreshLayout()
        addHeadView()
        refresh()
        return binding.root
    }

    private fun addHeadView() {
        val headerAdapter = HeaderAdapter()
        headerAdapter.setOnItemClickListener { _, _, _ ->
            addHeadView()
        }
        helper.addBeforeAdapter(headerAdapter)
    }

    private fun initRefreshLayout() {
        binding.refreshLayout.setColorSchemeColors(Color.rgb(47, 223, 189))
        binding.refreshLayout.setOnRefreshListener { refresh() }
    }


    /**
     * 刷新
     */
    private fun refresh() {
        // 下拉刷新，需要重置页数
        pageInfo.reset()
        binding.refreshLayout.isRefreshing = true
        request()
    }

    /**
     * 请求数据
     */
    private fun request() {

        Request(pageInfo.page, object : RequestCallBack {
            override fun success(data: MutableList<String>) {
                binding.refreshLayout.isRefreshing = false
                if (pageInfo.isFirstPage) {
                    // 如果是加载的第一页数据，用 submitList()
                    // If it is the first page of data loaded, use submitList().
                    mAdapter.submitList(data)
                } else {
                    //不是第一页，则用add
                    mAdapter.addAll(data)
                }
                if (pageInfo.page >= PAGE_SIZE) {
                    /*
                    Set the status to not loaded, and there is no paging data.
                    设置状态为未加载，并且没有分页数据了
                     */
                    helper.trailingLoadState = LoadState.NotLoading(true)
                } else {
                    /*
                    Set the state to not loaded, and there is also paginated data
                    设置状态为未加载，并且还有分页数据
                    */
                    helper.trailingLoadState = LoadState.NotLoading(false)
                }

                // page加一
                pageInfo.nextPage()
            }


            override fun fail(e: Exception) {
                binding.refreshLayout.isRefreshing = false
                helper.trailingLoadState = LoadState.Error(e)
            }
        }).start()
    }

    /**
     * 模拟加载数据的类，不用特别关注
     */
    internal class Request(private val mPage: Int, private val mCallBack: RequestCallBack) :
        Thread() {
        private val mHandler: Handler = Handler(Looper.getMainLooper())

        override fun run() {
            try {
                sleep(1000) // 模拟网络延迟
            } catch (ignored: InterruptedException) {
            }
            if (mPage == 2 && mFirstError) {
                mFirstError = false
                mHandler.post { mCallBack.fail(RuntimeException("load fail")) }
            } else {
                var size = PAGE_SIZE
                if (mPage == 1) {
                    if (mFirstPageNoMore) {
                        size = 1
                    }
                    mFirstPageNoMore = !mFirstPageNoMore
                    if (!mFirstError) {
                        mFirstError = true
                    }
                } else if (mPage == 4) {
                    size = 1
                }
                val dataSize = size
                mHandler.post { mCallBack.success(createData()) }
            }
        }

        fun createData(): MutableList<String> {
            val list = mutableListOf<String>()
            for (i in 0..20) {
                list.add("这是第${i}条数据")
            }

            return list
        }

        companion object {
            private var mFirstPageNoMore = false
            private var mFirstError = true
        }
    }

    internal interface RequestCallBack {
        /**
         * 模拟加载成功
         *
         * @param data 数据
         */
        fun success(data: MutableList<String>)

        /**
         * 模拟加载失败
         *
         * @param e 错误信息
         */
        fun fail(e: Exception)
    }

    companion object {
        private const val PAGE_SIZE = 5
    }

}