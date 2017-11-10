package es.aolivafaura.flexiblecoachmarkview

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.support.annotation.Nullable
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.View

class SpotView : AppCompatImageView {

    private var circleRect: RectF? = null
    private var radius: Float = 0f
    private val paint = Paint(ANTI_ALIAS_FLAG)
    private val potterDuffClear = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private val backgroundColor = R.color.black_70

    constructor(context: Context) : super(context) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    constructor(context: Context, @Nullable attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    fun drawSpot(rect: RectF, radius: Float) {

        this.circleRect = rect
        this.radius = radius

        //Redraw after defining circle
        invalidate()

        // Capture touch events
        setupTouchListener()
    }

    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)
        if (circleRect != null) {
            paint.color = ContextCompat.getColor(context, backgroundColor)
            paint.style = Paint.Style.FILL
            canvas.drawPaint(paint)

            paint.xfermode = potterDuffClear
            canvas.drawRoundRect(circleRect, radius, radius, paint)
        }
    }

    /**
     * Component touch listener
     */
    private fun setupTouchListener() {
        setOnTouchListener { _, _ -> true }
    }
}