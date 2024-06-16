package com.egiwon.example.motionlayoutexample

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.NestedScrollingParent2

class TouchFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), NestedScrollingParent2 {

    private fun getMotionLayout(): NestedScrollingParent2 {
        return parent as NestedScrollingParent2
    }

    override fun onStartNestedScroll(child: android.view.View, target: android.view.View, axes: Int, type: Int): Boolean {
        return getMotionLayout().onStartNestedScroll(child, target, axes, type)
    }

    override fun onNestedScrollAccepted(child: android.view.View, target: android.view.View, axes: Int, type: Int) {
        return getMotionLayout().onNestedScrollAccepted(child, target, axes, type)
    }

    override fun onStopNestedScroll(target: android.view.View, type: Int) {
        return getMotionLayout().onStopNestedScroll(target, type)
    }

    override fun onNestedScroll(
        target: android.view.View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        return getMotionLayout().onNestedScroll(
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            type
        )
    }

    override fun onNestedPreScroll(target: android.view.View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        return getMotionLayout().onNestedPreScroll(target, dx, dy, consumed, type)
    }

}
