package com.egiwon.example.motionlayoutexample

import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import kotlin.math.abs
import kotlin.math.max

class CalendarSnapHelper : RecyclerView.OnFlingListener() {

    private var recyclerView: RecyclerView? = null
    private var horizontalHelper: OrientationHelper? = null

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        private var scrolled = false

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                if (scrolled) {
//                    scrolled = false
//                    snapToTargetExistingView()
//                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//            if (dx != 0 || dy != 0) {
//                scrolled = true
//            }
        }
    }

    override fun onFling(velocityX: Int, velocityY: Int): Boolean {
        val layoutManager = recyclerView?.layoutManager ?: return false
        val minFlingVelocity: Int = recyclerView?.minFlingVelocity ?: 0
        return (abs(velocityY) > minFlingVelocity || abs(velocityX) > minFlingVelocity)
                && snapFromFling(layoutManager, velocityX, velocityY)
    }

    fun attachToRecyclerView(recyclerView: RecyclerView?) {
        if (this.recyclerView == recyclerView) {
            return
        }

        if (this.recyclerView != null) {
            destroyCallbacks()
        }
        this.recyclerView = recyclerView
        if (recyclerView != null) {
            setupCallbacks()
//            snapToTargetExistingView()
        }
    }

    private fun setupCallbacks() {
        if (recyclerView?.onFlingListener != null) {
            return
        }

//        recyclerView?.addOnScrollListener(scrollListener)
        recyclerView?.onFlingListener = this
    }

    fun snapToTargetExistingView() {
        if (recyclerView == null) {
            return
        }

        val layoutManager: LayoutManager = recyclerView?.layoutManager ?: return

        val snapView = findSnapView(layoutManager) ?: return
        val snapDistance = calculateDistanceToFinalSnap(layoutManager, snapView)
        if (snapDistance[0] != 0 || snapDistance[1] != 0) {
            recyclerView?.smoothScrollBy(snapDistance[0], snapDistance[1])
        }
    }

    private fun destroyCallbacks() {
        recyclerView?.removeOnScrollListener(scrollListener)
        recyclerView?.onFlingListener = null
    }

    private fun findSnapView(layoutManager: LayoutManager): View? {
        return this.findCenterView(layoutManager, this.getHorizontalHelper(layoutManager))
    }

    private fun findCenterView(layoutManager: LayoutManager, helper: OrientationHelper): View? {
        val childCount = layoutManager.childCount

        if (childCount == 0) {
            return null
        }

        var closestChild: View? = null
        val center = helper.startAfterPadding + helper.totalSpace / 2
        var absClosest = Int.MAX_VALUE

        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i)
            val childCenter =
                helper.getDecoratedStart(child) + helper.getDecoratedMeasurement(child) / 2
            val absDistance = abs(childCenter - center)

            if (absDistance < absClosest) {
                absClosest = absDistance
                closestChild = child
            }
        }

        return closestChild
    }

    private fun getHorizontalHelper(layoutManager: LayoutManager): OrientationHelper {
        if (horizontalHelper == null || horizontalHelper?.layoutManager !== layoutManager) {
            horizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return OrientationHelper.createHorizontalHelper(layoutManager)
    }

    fun calculateDistanceToFinalSnap(
        layoutManager: LayoutManager,
        targetView: View
    ): IntArray {
        val out = IntArray(2)
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToCenter(targetView, getHorizontalHelper(layoutManager))
        } else {
            out[0] = 0
        }
        return out
    }

    private fun distanceToCenter(
        targetView: View,
        helper: OrientationHelper
    ): Int {
        val childCenter =
            helper.getDecoratedStart(targetView) + helper.getDecoratedMeasurement(targetView) / 2
        val containerCenter = helper.startAfterPadding + helper.totalSpace / 2
        return childCenter - containerCenter
    }

    private fun snapFromFling(
        layoutManager: LayoutManager,
        velocityX: Int,
        velocityY: Int
    ): Boolean {
        if (layoutManager !is RecyclerView.SmoothScroller.ScrollVectorProvider) {
            return false
        }

        val smoothScroller = createSnapScroller(layoutManager)
        val targetPosition = findTargetSnapPosition(layoutManager, velocityX, velocityY)
        if (targetPosition == RecyclerView.NO_POSITION) {
            return false
        }

        smoothScroller.targetPosition = targetPosition
        layoutManager.startSmoothScroll(smoothScroller)
        return true
    }

    private fun createSnapScroller(layoutManager: LayoutManager): LinearSmoothScroller {
        return object : LinearSmoothScroller(recyclerView?.context) {
            override fun onTargetFound(
                targetView: View,
                state: RecyclerView.State,
                action: Action
            ) {
                val snapDistances = calculateDistanceToFinalSnap(layoutManager, targetView)
                val dx = snapDistances[0]
                val dy = snapDistances[1]
                val time = calculateTimeForDeceleration(max(abs(dx), abs(dy)))
                if (time > 0) {
                    action.update(dx, dy, time, mDecelerateInterpolator)
                }
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                return super.calculateSpeedPerPixel(displayMetrics)
            }
        }
    }

    private fun findTargetSnapPosition(
        layoutManager: LayoutManager,
        velocityX: Int,
        velocityY: Int
    ): Int {
        val itemCount = layoutManager.itemCount
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION
        }

        val orientationHelper = this.getHorizontalHelper(layoutManager)
        var closestChildBeforeCenter: View? = null
        var distanceBefore = Int.MIN_VALUE
        var closestChildAfterCenter: View? = null
        var distanceAfter = Int.MAX_VALUE
        val childCount = layoutManager.childCount

        var visibleView: View? = null
        var visiblePosition: Int
        for (i in 0 until childCount) {
            visibleView = layoutManager.getChildAt(i)
            visibleView?.let { nonNullVisibleView ->
                visiblePosition = this.distanceToCenter(nonNullVisibleView, orientationHelper)

                if (visiblePosition in (distanceBefore + 1)..0) {
                    distanceBefore = visiblePosition
                    closestChildBeforeCenter = visibleView
                }

                if (visiblePosition in 0..<distanceAfter) {
                    distanceAfter = visiblePosition
                    closestChildAfterCenter = visibleView
                }
            }
        }

        val forwardDirection: Boolean = velocityX > 0
        if (forwardDirection && closestChildAfterCenter != null) {
            return layoutManager.getPosition(
                closestChildAfterCenter ?: return RecyclerView.NO_POSITION
            )
        } else if (!forwardDirection && closestChildBeforeCenter != null) {
            return layoutManager.getPosition(
                closestChildBeforeCenter ?: return RecyclerView.NO_POSITION
            )
        } else {
            visiblePosition =
                layoutManager.getPosition(visibleView ?: return RecyclerView.NO_POSITION)

            val snapToPosition = visiblePosition + 1
            return if (snapToPosition in 0..<itemCount) {
                snapToPosition
            } else {
                RecyclerView.NO_POSITION
            }
        }

    }
}
