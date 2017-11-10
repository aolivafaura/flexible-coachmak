package es.aolivafaura.flexiblecoachmarkview

/**
 * Created by antoniojoseolivafaura on 10/11/2017.
 */

data class Coachmark<T> constructor(internal val spotDiameter: Int, internal val targetId: Int, internal val relatedSpotView: T,
                                    internal val position: Int, internal val alignment: Int) {

    // ---------------------------------------------------------------------------------------------
    // CONSTANTS
    // ---------------------------------------------------------------------------------------------

    companion object {

        /**
         * Indicates that related view will have its top aligned with the spot position chosen
         */
        val ALIGNMENT_TOP = 1
        /**
         * Indicates that related view will have its bottom aligned with the spot position chosen
         */
        val ALIGNMENT_BOTTOM = 2
        /**
         * Indicates that related view will have its left side aligned with the spot position chosen
         */
        val ALIGNMENT_LEFT = 3
        /**
         * Indicates that related view will have its right side aligned with the spot position chosen
         */
        val ALIGNMENT_RIGHT = 4
        /**
         * Indicates that related view will have its center aligned with the spot position chosen
         */
        val ALIGNMENT_CENTER = 5
        /**
         * Indicates that related view will be aligned on the top of spot
         */
        val POSITION_TOP = 6
        /**
         * Indicates that related view will be aligned on the bottom of spot
         */
        val POSITION_BOTTOM = 7
        /**
         * Indicates that related view will be aligned on the left side of spot
         */
        val POSITION_LEFT = 8
        /**
         * Indicates that related view will be aligned on the right side of spot
         */
        val POSITION_RIGHT = 9
    }

    // ---------------------------------------------------------------------------------------------
    // VARIABLES
    // ---------------------------------------------------------------------------------------------

    internal var maxWdith = 200
    internal val paddings = intArrayOf(0, 0, 0, 0)

    /**
     * Defined paddings will be applied on related spot view
     *
     * @param top    top padding
     * @param left   left padding
     * @param right  right padding
     * @param bottom bottom padding
     */
    fun setPaddings(top: Int, left: Int, right: Int, bottom: Int) {

        this.paddings[0] = top
        this.paddings[1] = left
        this.paddings[2] = right
        this.paddings[3] = bottom
    }
}
