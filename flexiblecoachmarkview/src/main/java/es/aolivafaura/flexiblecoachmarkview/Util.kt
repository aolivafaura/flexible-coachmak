package es.aolivafaura.flexiblecoachmarkview

import android.content.Context
import android.util.DisplayMetrics

/**
 * Created by antoniojoseolivafaura on 10/11/2017.
 */

fun dpToPixels(context: Context, dp: Int) = Math.round(dp * (context.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
