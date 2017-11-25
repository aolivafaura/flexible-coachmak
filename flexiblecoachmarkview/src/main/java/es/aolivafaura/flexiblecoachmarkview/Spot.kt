package es.aolivafaura.flexiblecoachmarkview

import android.graphics.RectF

const val EXPAND = 1
const val COLLAPSE = 2

data class Spot(val rectF: RectF, val radius: Float, val animate: Boolean) {

    internal var direction: Int = 0
    internal var currentRect: RectF? = null
}