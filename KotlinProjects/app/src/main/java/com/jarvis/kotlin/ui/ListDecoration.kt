package com.jarvis.kotlin.ui

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.support.v7.widget.RecyclerView
import android.view.View


class ListDecoration : RecyclerView.ItemDecoration {

    private var mDrawable: Drawable? = null

    constructor(listDrawable: Drawable) {
        mDrawable = listDrawable
    }


//    override fun onDraw(canvas: Canvas?, parent: RecyclerView?, state: RecyclerView.State?) {
//        val dividerLeft: Int = parent?.paddingLeft!!
//        val dividerRight: Int = parent?.width?.minus(parent?.paddingRight);
//
//        val childCount: Int = parent!!.childCount
//        for (i in 0 until childCount?.minus(1)) {
//            val child = parent.getChildAt(i)
//
//            val params = child.layoutParams as RecyclerView.LayoutParams
//
//            val dividerTop = child.bottom + params.bottomMargin
//            val dividerBottom = dividerTop + mDrawable?.intrinsicHeight!!;
//            mDrawable?.setBounds(dividerLeft!!, dividerTop, dividerRight!!, dividerBottom)
//            mDrawable?.draw(canvas)
//        }
//
//    }

    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
        super.getItemOffsets(outRect, view, parent, state)
        if (parent?.getChildAdapterPosition(view)!! < 1) {
            return
        }

        outRect?.top = DIVIDER_HEIGHT
    }

    companion object {
        const val DIVIDER_HEIGHT = 50
    }
}
