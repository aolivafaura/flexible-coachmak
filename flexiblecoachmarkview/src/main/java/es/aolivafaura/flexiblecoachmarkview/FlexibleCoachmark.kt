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


class FlexibleCoachmark<T : View> : RelativeLayout {

    // ---------------------------------------------------------------------------------------------
    // VARIABLES
    // ---------------------------------------------------------------------------------------------

    private var steps: List<Coachmark<T>>? = null
    private var currentStep = 0

    private val RELATED_VIEW_ID = R.id.view_id

    var dismissListener: OnCoackmarkDismissedListener? = null
    var initialDelay = 200L

    /**
     * Notifies when coachmark view is dismissed
     */
    interface OnCoackmarkDismissedListener {
        fun onCoachmarkDismissed()
    }

    // ---------------------------------------------------------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        steps?.let {
            drawStep(steps!![currentStep])
        } ?: Log.e("FLEXIBLE COACH MARK", "Please set desired steps before invoke show method")
    }

    // ---------------------------------------------------------------------------------------------
    // PUBLIC METHODS
    // ---------------------------------------------------------------------------------------------

    /**
     * Steps will be executed in list order
     *
     * @param steps
     */
    fun setSteps(steps: List<Coachmark<T>>) {

        this.steps = steps
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
            fadeIn()
        }, initialDelay)
    }

    // ---------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    // ---------------------------------------------------------------------------------------------

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

    private fun drawStep(item: Coachmark<T>) {

        removeAllViews()

        val focusView = findOnViewForId(null, item.targetId)

        if (focusView == null) {
            Log.w("CUSTOM COACH MARK",
                    "There is no view detected with given Id: " + item.targetId)
            close()
        }

        val center = calculateCenter(focusView)
        val radius = item.spotDiameter / 2
        val anchorPoint = calculateAnchorPoint(center, item)

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

    private fun calculateAnchorPoint(center: IntArray, item: Coachmark<T>): IntArray {

        val radius = item.spotDiameter / 2
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

        val spotView = SpotView(context)

        val topCoordinate = coordinates[1] - radius
        val bottomCoordinate = coordinates[1] + radius
        val leftCoordinate = coordinates[0] - radius
        val rightCoordinate = coordinates[0] + radius

        val rect = RectF(leftCoordinate.toFloat(), topCoordinate.toFloat(), rightCoordinate.toFloat(),
                bottomCoordinate.toFloat())

        val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT)
        addView(spotView, params)

        spotView.drawSpot(rect, radius.toFloat())
    }

    private fun drawRelatedView(item: Coachmark<T>, anchorPoint: IntArray) {

        val contentLayout = ConstraintLayout(context)
        addView(contentLayout, RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT))

        val relatedView = item.relatedSpotView
        relatedView.id = RELATED_VIEW_ID
        relatedView.visibility = View.INVISIBLE

        val maxWidth = dpToPixels(context, item.maxWdith)

        relatedView.parent?.let {
            (relatedView.parent as ViewGroup).removeView(relatedView)
        }

        contentLayout.addView(relatedView, ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT))

        relatedView.viewTreeObserver
                .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {

                    override fun onGlobalLayout() {

                        if (maxWidth > 0 && relatedView.getWidth() > maxWidth) {
                            requestLayout()
                            relatedView.getLayoutParams().width = maxWidth
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

    private fun setConstraints(item: Coachmark<T>, contentLayout: ConstraintLayout,
                               anchorPoint: IntArray) {

        val VERTICAL_GUIDE_ID = R.id.vertical_guide
        val HORIZONTAL_GUIDE_ID = R.id.horizontal_guide

        val constraintSet = ConstraintSet()
        constraintSet.clone(contentLayout)
        constraintSet.create(HORIZONTAL_GUIDE_ID, ConstraintSet.HORIZONTAL_GUIDELINE)
        constraintSet.create(VERTICAL_GUIDE_ID, ConstraintSet.VERTICAL_GUIDELINE)

        constraintSet.setGuidelineBegin(HORIZONTAL_GUIDE_ID, anchorPoint[1])
        constraintSet.setGuidelineBegin(VERTICAL_GUIDE_ID, anchorPoint[0])
        constraintSet.applyTo(contentLayout)

        when (item.position) {
            Coachmark.POSITION_TOP -> {
                constraintSet.connect(RELATED_VIEW_ID, ConstraintSet.BOTTOM, HORIZONTAL_GUIDE_ID,
                        ConstraintSet.TOP, 0)
                constraintSet.setVerticalBias(RELATED_VIEW_ID, 100f)
            }
            Coachmark.POSITION_BOTTOM -> {
                constraintSet.connect(RELATED_VIEW_ID, ConstraintSet.TOP, HORIZONTAL_GUIDE_ID,
                        ConstraintSet.BOTTOM)
                constraintSet.setVerticalBias(RELATED_VIEW_ID, 0f)
            }
            Coachmark.POSITION_LEFT -> {
                constraintSet.connect(RELATED_VIEW_ID, ConstraintSet.RIGHT, VERTICAL_GUIDE_ID,
                        ConstraintSet.LEFT)
                constraintSet.setHorizontalBias(RELATED_VIEW_ID, 100f)
            }
            Coachmark.POSITION_RIGHT -> {
                constraintSet.connect(RELATED_VIEW_ID, ConstraintSet.LEFT, VERTICAL_GUIDE_ID,
                        ConstraintSet.RIGHT)
                constraintSet.setHorizontalBias(RELATED_VIEW_ID, 0f)
            }
        }

        when (item.alignment) {
            Coachmark.ALIGNMENT_CENTER ->
                if (item.position == Coachmark.POSITION_TOP || item.position == Coachmark.POSITION_BOTTOM) {
                    constraintSet.connect(RELATED_VIEW_ID, ConstraintSet.LEFT, VERTICAL_GUIDE_ID,
                            ConstraintSet.LEFT)
                    constraintSet.connect(RELATED_VIEW_ID, ConstraintSet.RIGHT, VERTICAL_GUIDE_ID,
                            ConstraintSet.RIGHT)
                } else {
                    constraintSet.connect(RELATED_VIEW_ID, ConstraintSet.TOP, HORIZONTAL_GUIDE_ID,
                            ConstraintSet.TOP)
                    constraintSet.connect(RELATED_VIEW_ID, ConstraintSet.BOTTOM,
                            HORIZONTAL_GUIDE_ID, ConstraintSet.BOTTOM)
                }
            Coachmark.ALIGNMENT_TOP -> {
                constraintSet.connect(RELATED_VIEW_ID, ConstraintSet.BOTTOM, HORIZONTAL_GUIDE_ID,
                        ConstraintSet.TOP)
                constraintSet.setVerticalBias(RELATED_VIEW_ID, 100f)
            }
            Coachmark.ALIGNMENT_BOTTOM -> {
                constraintSet.connect(RELATED_VIEW_ID, ConstraintSet.TOP, HORIZONTAL_GUIDE_ID,
                        ConstraintSet.BOTTOM)
                constraintSet.setVerticalBias(RELATED_VIEW_ID, 0f)
            }
            Coachmark.ALIGNMENT_LEFT -> {
                constraintSet.connect(RELATED_VIEW_ID, ConstraintSet.RIGHT, VERTICAL_GUIDE_ID,
                        ConstraintSet.LEFT)
                constraintSet.setVerticalBias(RELATED_VIEW_ID, 100f)
            }
            Coachmark.ALIGNMENT_RIGHT -> constraintSet.connect(RELATED_VIEW_ID, ConstraintSet.LEFT, VERTICAL_GUIDE_ID,
                    ConstraintSet.RIGHT)
        }

        constraintSet.applyTo(contentLayout)
        contentLayout.post { item.relatedSpotView.visibility = View.VISIBLE }
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
