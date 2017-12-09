package es.aolivafaura.flexiblecoachmarkview

import android.content.Context
import android.graphics.RectF
import android.os.Build
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import android.content.ContextWrapper
import android.app.Activity
import android.os.Handler


class FlexibleCoachmark : RelativeLayout {

    // VARIABLES -----------------------------------------------------------------------------------
    private var spotView: SpotView? = null

    private var steps: List<Coachmark<View>>? = null
    private var currentStep = 0

    private val relatedViewId = R.id.view_id

    var dismissListener: OnCoackmarkDismissedListener? = null
    var initialDelay = 200L

    /**
     * Notifies when coachmark view is dismissed
     */
    interface OnCoackmarkDismissedListener {
        fun onCoachmarkDismissed()
    }

    // CONSTRUCTOR ---------------------------------------------------------------------------------
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        steps?.let {
            drawStep(steps!![currentStep])
        } ?: Log.e("FLEXIBLE COACH MARK", "Please set desired steps before invoke show method")
    }

    // PUBLIC METHODS ------------------------------------------------------------------------------
    /**
     * Steps will be executed in list order
     *
     * @param steps
     */
    fun <T: View> setSteps(steps: List<Coachmark<T>>) {
        this.steps = steps as List<Coachmark<View>>
    }

    /**
     * @return `true` whether there is another step left, `false` if isn't
     */
    fun hasNextStep(): Boolean {

        return currentStep < steps!!.size - 1
    }

    /**
     * Advance to next step
     */
    fun nextStep() {
        if (!hasNextStep()) {
            close()
        } else {
            drawStep(steps!![++currentStep])
        }
    }

    /**
     * Close view
     */
    fun close() {
        fadeOut {
            (parent as ViewGroup).removeView(this)
            dismissListener?.onCoachmarkDismissed()
        }
    }

    fun getCurrentStepView() {
        steps?.let {
            steps!![currentStep]
        } ?: Log.e("FLEXIBLE COACH MARK", "There is no steps defined")
    }

    fun show() {
        Handler().postDelayed({
            val vg = getActivity()?.window?.decorView?.rootView as ViewGroup
            val params = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

            layoutParams = params
            visibility = View.INVISIBLE
            vg.addView(this@FlexibleCoachmark)
            fadeIn(1)
        }, initialDelay)
    }

    // PRIVATE METHODS -----------------------------------------------------------------------------
    private fun getActivity(): Activity? {
        var context = context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

    private fun drawStep(item: Coachmark<View>) {

        removeView(findViewById(relatedViewId))

        val focusView: View? =
                if (item.target != null) item.target
                else findOnViewForId(null, item.targetId)


        if (focusView == null) {
            Log.w("CUSTOM COACH MARK",
                    "There is no view detected with given Id: " + item.targetId)
            close()
            return
        }

        val spotDiameter =
                when {
                    item.spotDiameterDp > 0 -> item.spotDiameterDp
                    item.spotDiameterPercetage > 0 -> {
                        pixelsToDp(context, (focusView.width * (item.spotDiameterPercetage / 100)).toInt()).toInt()
                    }
                    else -> 0
                }

        val center = calculateCenter(focusView)
        val radius = dpToPixels(context, spotDiameter / 2)
        val anchorPoint = calculateAnchorPoint(center, item, radius)

        calculateRelatedViewMaxWidth(center, item, spotDiameter)
        drawSpot(center, radius)
        drawRelatedView(item, anchorPoint)
    }

    private fun calculateCenter(focusView: View?): IntArray {

        val coordinates = IntArray(2)
        focusView!!.getLocationInWindow(coordinates)

        coordinates[0] = coordinates[0] + focusView.width / 2
        coordinates[1] = coordinates[1] + focusView.height / 2

        return coordinates
    }

    private fun calculateAnchorPoint(center: IntArray, item: Coachmark<View>, radius: Int): IntArray {
        val anchorPoint = intArrayOf(center[0], center[1])

        when (item.position) {
            Coachmark.POSITION_BOTTOM -> anchorPoint[1] = anchorPoint[1] + radius
            Coachmark.POSITION_TOP -> anchorPoint[1] = anchorPoint[1] - radius
            Coachmark.POSITION_LEFT -> anchorPoint[0] = anchorPoint[0] - radius
            Coachmark.POSITION_RIGHT -> anchorPoint[0] = anchorPoint[0] + radius
        }

        val padding = item.paddings

        if (padding[0] > 0) {
            anchorPoint[1] = anchorPoint[1] + dpToPixels(context, padding[0])
        }

        if (padding[1] > 0) {
            anchorPoint[0] = anchorPoint[0] + dpToPixels(context, padding[1])
        }

        if (padding[2] > 0) {
            anchorPoint[0] = anchorPoint[0] - dpToPixels(context, padding[2])
        }

        if (padding[3] > 0) {
            anchorPoint[1] = anchorPoint[1] - dpToPixels(context, padding[3])
        }

        return anchorPoint
    }

    private fun drawSpot(coordinates: IntArray, radius: Int) {

        if (spotView == null) {
            spotView = SpotView(context)
            val params = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT)
            addView(spotView, params)
        }

        val topCoordinate = coordinates[1] - radius
        val bottomCoordinate = coordinates[1] + radius
        val leftCoordinate = coordinates[0] - radius
        val rightCoordinate = coordinates[0] + radius

        val rect = RectF(leftCoordinate.toFloat(), topCoordinate.toFloat(), rightCoordinate.toFloat(),
                bottomCoordinate.toFloat())

        val spot = Spot(rect, radius.toFloat(), true)
        spot.direction = EXPAND

        spotView!!.removeLastSpot()
        spotView!!.addSpot(spot)
        spotView!!.startSequence()
    }

    private fun drawRelatedView(item: Coachmark<View>, anchorPoint: IntArray) {

        val contentLayout = ConstraintLayout(context)
        addView(contentLayout, RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT))

        val relatedView = item.relatedSpotView
        relatedView?.id = relatedViewId
        relatedView?.visibility = View.INVISIBLE

        val maxWidth = dpToPixels(context, item.maxWidth)

        relatedView?.parent?.let {
            (relatedView.parent as ViewGroup).removeView(relatedView)
        }

        contentLayout.addView(relatedView, ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT))

        relatedView?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (maxWidth > 0 && relatedView.width > maxWidth) {
                    requestLayout()
                    relatedView.layoutParams.width = maxWidth
                }

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    relatedView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                } else {
                    relatedView.viewTreeObserver.removeGlobalOnLayoutListener(this)
                }

                setConstraints(item, contentLayout, anchorPoint)
            }
        })
    }

    private fun setConstraints(item: Coachmark<View>, contentLayout: ConstraintLayout,
                               anchorPoint: IntArray) {

        val verticalGuideId = R.id.vertical_guide
        val horizontalGuideId = R.id.horizontal_guide

        val constraintSet = ConstraintSet()
        constraintSet.clone(contentLayout)
        constraintSet.create(horizontalGuideId, ConstraintSet.HORIZONTAL_GUIDELINE)
        constraintSet.create(verticalGuideId, ConstraintSet.VERTICAL_GUIDELINE)

        constraintSet.setGuidelineBegin(horizontalGuideId, anchorPoint[1])
        constraintSet.setGuidelineBegin(verticalGuideId, anchorPoint[0])
        constraintSet.applyTo(contentLayout)

        when (item.position) {
            Coachmark.POSITION_TOP -> {
                constraintSet.connect(relatedViewId, ConstraintSet.BOTTOM, horizontalGuideId,
                        ConstraintSet.TOP, 0)
                constraintSet.setVerticalBias(relatedViewId, 100f)
            }
            Coachmark.POSITION_BOTTOM -> {
                constraintSet.connect(relatedViewId, ConstraintSet.TOP, horizontalGuideId,
                        ConstraintSet.BOTTOM)
                constraintSet.setVerticalBias(relatedViewId, 0f)
            }
            Coachmark.POSITION_LEFT -> {
                constraintSet.connect(relatedViewId, ConstraintSet.RIGHT, verticalGuideId,
                        ConstraintSet.LEFT)
                constraintSet.setHorizontalBias(relatedViewId, 100f)
            }
            Coachmark.POSITION_RIGHT -> {
                constraintSet.connect(relatedViewId, ConstraintSet.LEFT, verticalGuideId,
                        ConstraintSet.RIGHT)
                constraintSet.setHorizontalBias(relatedViewId, 0f)
            }
        }

        when (item.alignment) {
            Coachmark.ALIGNMENT_CENTER ->
                if (item.position == Coachmark.POSITION_TOP || item.position == Coachmark.POSITION_BOTTOM) {
                    constraintSet.connect(relatedViewId, ConstraintSet.LEFT, verticalGuideId,
                            ConstraintSet.LEFT)
                    constraintSet.connect(relatedViewId, ConstraintSet.RIGHT, verticalGuideId,
                            ConstraintSet.RIGHT)
                } else {
                    constraintSet.connect(relatedViewId, ConstraintSet.TOP, horizontalGuideId,
                            ConstraintSet.TOP)
                    constraintSet.connect(relatedViewId, ConstraintSet.BOTTOM,
                            horizontalGuideId, ConstraintSet.BOTTOM)
                }
            Coachmark.ALIGNMENT_TOP -> {
                constraintSet.connect(relatedViewId, ConstraintSet.BOTTOM, horizontalGuideId,
                        ConstraintSet.TOP)
                constraintSet.setVerticalBias(relatedViewId, 100f)
            }
            Coachmark.ALIGNMENT_BOTTOM -> {
                constraintSet.connect(relatedViewId, ConstraintSet.TOP, horizontalGuideId,
                        ConstraintSet.BOTTOM)
                constraintSet.setVerticalBias(relatedViewId, 0f)
            }
            Coachmark.ALIGNMENT_LEFT -> {
                constraintSet.connect(relatedViewId, ConstraintSet.RIGHT, verticalGuideId,
                        ConstraintSet.LEFT)
                constraintSet.setVerticalBias(relatedViewId, 100f)
            }
            Coachmark.ALIGNMENT_RIGHT -> constraintSet.connect(relatedViewId, ConstraintSet.LEFT, verticalGuideId,
                    ConstraintSet.RIGHT)
        }

        constraintSet.applyTo(contentLayout)
        contentLayout.post { item.relatedSpotView?.visibility = View.VISIBLE }
    }

    private fun calculateRelatedViewMaxWidth(center: IntArray, item: Coachmark<View>, spotDiameter: Int) {

        val screenWidth = getDisplayWidhtPx(context)
        val margin = dpToPixels(context, 24)

        var width = screenWidth

        when (item.position) {
            Coachmark.POSITION_TOP, Coachmark.POSITION_BOTTOM -> if (item.alignment == Coachmark.ALIGNMENT_CENTER) {
                width -= margin * 2
            } else {
                width -= margin
                width -= dpToPixels(context, spotDiameter / 2)

                if (item.alignment == Coachmark.ALIGNMENT_LEFT) {
                    if (item.paddings[2] != 0) {
                        width -= dpToPixels(context, item.paddings[2])
                    }
                    width -= (screenWidth - center[0])
                } else if (item.alignment == Coachmark.ALIGNMENT_RIGHT) {
                    if (item.paddings[1] != 0) {
                        width -= dpToPixels(context, item.paddings[1])
                    }
                    width -= center[0]
                }
            }
            Coachmark.POSITION_LEFT, Coachmark.POSITION_RIGHT -> {
                width -= margin
                if (item.position == Coachmark.POSITION_LEFT) {
                    if (item.paddings[2] != 0) {
                        width -= dpToPixels(context, item.paddings[2])
                    }
                    width -= screenWidth - (center[0] - dpToPixels(context, spotDiameter / 2).toFloat()).toInt()
                } else if (item.position == Coachmark.POSITION_RIGHT) {
                    if (item.paddings[1] != 0) {
                        width -= dpToPixels(context, item.paddings[1])
                    }
                    width -= dpToPixels(context, spotDiameter) / 2
                    width -= center[0]
                }
            }
        }

        item.maxWidth = pixelsToDp(context, width).toInt()
    }

    private fun findOnViewForId(viewGroup: ViewGroup?, resId: Int): View? {
        var localViewGroup = viewGroup

        if (localViewGroup == null) {
            localViewGroup = parent as ViewGroup
        }

        val childCount = localViewGroup.childCount
        var auxView: View

        for (i in 0 until childCount) {
            auxView = localViewGroup.getChildAt(i)

            if (auxView.id == resId) {
                return auxView
            } else if (auxView is ViewGroup) {
                val view = findOnViewForId(auxView, resId)
                if (view != null) {
                    return view
                }
            }
        }
        return null
    }
}
