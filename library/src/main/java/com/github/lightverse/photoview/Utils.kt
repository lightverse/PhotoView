package com.github.lightverse.photoview

import android.graphics.Matrix

/**
 * Created by lightverse on 2018/5/2.
 */

object MatrixUtil{
    private val values = FloatArray(9)


    fun getScale(matrix: Matrix):Float{
        matrix.getValues(values)
        return values[0]
    }

    fun getTransX(matrix: Matrix):Float{
        matrix.getValues(values)
        return values[2]
    }

    fun getTransY(matrix: Matrix):Float{
        matrix.getValues(values)
        return values[5]
    }

}