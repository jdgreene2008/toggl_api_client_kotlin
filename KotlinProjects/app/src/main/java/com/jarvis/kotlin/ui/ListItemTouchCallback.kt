package com.jarvis.kotlin.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.TextView
import com.jarvis.kotlin.R

abstract class  ListItemTouchCallback(context: Context, dragDirs: Int, swipeDirs: Int)  : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

    private var mContext: Context = context
    private var mLeftColor: ColorDrawable
    private var mRightColor: ColorDrawable
    private val swipeDirection = swipeDirs

    init {
        mLeftColor = ColorDrawable(ContextCompat.getColor(mContext, R.color.list_item_odd))
        mRightColor = ColorDrawable(ContextCompat.getColor(mContext, R.color.list_item_even))
    }

    override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
        if (viewHolder?.adapterPosition == 0)
            return 0
        return super.getMovementFlags(recyclerView, viewHolder)
    }


    override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
        return false
    }

    override fun onChildDraw(canvas: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val itemView: View = TextView(mContext)


        if (swipeDirection == ItemTouchHelper.RIGHT) {
            mLeftColor.setBounds(
                    itemView.left,
                    itemView.top,
                    itemView.left + dX.toInt(),
                    itemView.bottom
            )
            mLeftColor.draw(canvas)

            if (Math.abs(dX).compareTo(itemView.width / 2) > 0) {
                mRightColor.setBounds(itemView.left,
                        itemView.top, (Math.abs(dX) - itemView.width / 2).toInt(), itemView.bottom)
                mRightColor.draw(canvas)
            }
        } else {
            mLeftColor.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
            )
            mLeftColor.draw(canvas)

            if (Math.abs(dX).compareTo(itemView.width / 2) > 0) {
                mRightColor.setBounds(itemView.right - (Math.abs(dX) - itemView.width / 2).toInt(),
                        itemView.top, itemView.right, itemView.bottom)
                mRightColor.draw(canvas)
            }
        }

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}
