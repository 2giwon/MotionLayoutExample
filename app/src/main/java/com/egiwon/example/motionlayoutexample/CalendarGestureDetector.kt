package com.egiwon.example.motionlayoutexample

import android.animation.ValueAnimator
import android.content.Context
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView

class CalendarGestureDetector(
    context: Context,
    private val recyclerView: RecyclerView,
): SimpleOnGestureListener() {

    private var initialHeight: Int = 0

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (distanceY > 0) { // 위 방향으로 스크롤
            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                val linearLayout = child.findViewById<LinearLayout>(R.id.layout_month)
                val params = linearLayout.layoutParams

                initialHeight = linearLayout.height
                var newHeight = initialHeight - distanceY.toInt()
                newHeight = newHeight.coerceAtLeast(100) // 최소 높이 설정
                params.height = newHeight
                linearLayout.layoutParams = params
            }
        }
        return true
    }

    fun handleTouchEvent(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                val linearLayout = child.findViewById<LinearLayout>(R.id.layout_month)
                val params = linearLayout.layoutParams
                val animator = ValueAnimator.ofInt(linearLayout.height, initialHeight / 2)
                animator.addUpdateListener { animation ->
                    params.height = animation.animatedValue as Int
                    linearLayout.layoutParams = params
                }
                animator.duration = 300
                animator.start()
            }
        }
    }
}
