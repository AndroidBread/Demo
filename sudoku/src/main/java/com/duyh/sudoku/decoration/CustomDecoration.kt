package com.duyh.sudoku.decoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duyh.sudoku.R

class CustomDecoration(
    context: Context, orientation: Int, drawable: Int,
    /**
     * 分割线缩进值
     */
    private val inset: Int = 0
) : RecyclerView.ItemDecoration() {
    private val mDivider: Drawable?
    private var mOrientation = 0
    private val paint: Paint


    init {
        mDivider = AppCompatResources.getDrawable(context, drawable)
        paint = Paint()
        paint.color = context.getColor(R.color.white)
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        setOrientation(orientation)
    }

    fun setOrientation(orientation: Int) {
        require(!(orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST)) { "invalid orientation" }
        mOrientation = orientation
    }

    override fun onDraw(c: Canvas, parent: RecyclerView) {
        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent)
        } else {
            drawHorizontal(c, parent)
        }
    }

    private fun drawVertical(c: Canvas, parent: RecyclerView) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount
        val lastNum = if (parent.layoutManager is GridLayoutManager) {
            val gridLayoutManager = parent.layoutManager as GridLayoutManager
            if (childCount % gridLayoutManager.spanCount == 0) {
                childCount - gridLayoutManager.spanCount
            } else {
                childCount - childCount % gridLayoutManager.spanCount
            }
        } else {
            childCount - 1
        }
        //最后一个item不画分割线
        for (i in 0 until lastNum) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + mDivider!!.intrinsicHeight
            if (inset > 0) {
                c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
                mDivider.setBounds(left + inset, top, right - inset, bottom)
            } else {
                mDivider.setBounds(left, top, right, bottom)
            }
            mDivider.draw(c)
        }
    }

    private fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        val top = parent.paddingTop
        val bottom = parent.height - parent.paddingBottom
        val childCount = parent.childCount
        val spanCount = if (parent.layoutManager is GridLayoutManager) {
            val gridLayoutManager = parent.layoutManager as GridLayoutManager
            gridLayoutManager.spanCount
        } else {
            -1
        }
        for (i in 0 until childCount) {
            if (i  % spanCount != spanCount - 1) {
                val child = parent.getChildAt(i)
                val params = child.layoutParams as RecyclerView.LayoutParams
                val left = child.right + params.rightMargin
                val right = left + mDivider!!.intrinsicWidth
                mDivider.setBounds(left, top, right, bottom)
                mDivider.draw(c)
            }
        }
    }

    //由于Divider也有宽高，每一个Item需要向下或者向右偏移
    override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {
        if (mOrientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, mDivider!!.intrinsicHeight)
        } else {
            outRect.set(0, 0, mDivider!!.intrinsicWidth, 0)
        }
    }

    companion object {
        const val HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL
        const val VERTICAL_LIST = LinearLayoutManager.VERTICAL
    }
}